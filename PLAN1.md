- POST /api/pedidos pasa a ser pública — no requiere JWT. Si hay token, se vincula al Cliente; si no, pedido.cliente = null.
- Pedido tiene campos propios del comprador — nombre, apellido, cédula (opcional), teléfono WhatsApp, modalidad de entrega (OFICINA/DOMICILIO), dirección, municipio, departamento.
- El email no es obligatorio en el checkout — el seguimiento post-venta es por WhatsApp.
- Los campos de historial del cliente (/api/pedidos/mis-pedidos) siguen requiriendo login.

● User approved Claude's plan
Plan saved to: ~\.claude\plans\voy-onway.md · /plan to edit

● User approved Claude's plan
Plan saved to: ~\.claude\plans\voy-a-trabajar-sobre-sparkling-conway.md · /plan to edit
Plan: Backend Spring Boot — Quality Sports

Context

El proyecto es un e-commerce de calzado deportivo para Quality Sports (Cúcuta). El backend usa Spring
Boot 4.0.6 / Java 21 / PostgreSQL (implementados:
- Jerarquía de usuarios con herencia JPA (User → Cliente, Administrador, AsesorVentas)
- Autenticación JWT completa (AuthController, AuthService, JwtUtil, JwtFilter, SecurityConfig)
- Endpoints: POST /api/auth/registe
- Spring Security con rutas protegidas por rol (/api/admin/**, /api/asesor/**)

Lo que falta implementar son todos los módulos de negocio: productos, pedidos, lógica de descuentos por
 volumen, asignación equitativa de

---
Stack

- C:\.API\backend\ — Spring Boot (Maven)
- Paquete base: com.qualitysports.b
- DB: PostgreSQL en Supabase, ddl-auto=update
- Autenticación: JWT en header Authorization: Bearer <token>

---
Estructura de paquetes a crear

com.qualitysports.backend
├── auth/            ✅ EXISTENTE
├── user/            ✅ EXISTENTE   telefono — ver §1.0)
├── producto/        ❌ CREAR
│   ├── entity/      Producto, Categoria  (tallas = @ElementCollection en Producto)
│   ├── repository/  ProductoReposi
│   ├── service/     ProductoService
│   ├── controller/  ProductoController (rutas públicas y admin)
│   └── dto/         ProductoDTO, C
├── pedido/          ❌ CREAR
│   ├── entity/      Pedido, PedidoDetalle, EstadoPedido, HistorialEstado
│   ├── repository/  PedidoReposito etc.
│   ├── service/     PedidoService (lógica de negocio central)
│   ├── controller/  PedidoController (cliente, asesor, admin)
│   └── dto/         CheckoutReques
└── admin/           ❌ CREAR
    ├── entity/      ReglaPaquete
    ├── repository/  ReglaPaqueteRe
    ├── controller/  AdminController (KPIs, clientes, descuentos, gestión)
    └── dto/         KpiResponse, ClienteAdminDTO, ReglaPaqueteDTO

---
Fase 1: Entidades JPA

1.0a Modificar AsesorVentas existente

// user/AsesorVentas.java — agregar campo:
private String telefono;  // requerido para generar URL de WhatsApp
Registrar el asesor con número inte34567).

1.0b Nueva entidad ReglaPaquete

// admin/entity/ReglaPaquete.java
@Entity @Table(name="reglas_paquete
id (Long, PK)
cantidadPares (Integer)         //
precioTotalPaquete (BigDecimal) // ej: 190000, 280000, 360000, 450000
activo (boolean = true)
Defaults al iniciar (seeder): 2→190000, 3→280000, 4→360000, 5→450000.
El admin puede crear, editar o desa/descuentos.

Lógica de aplicación:
- Se cuenta el total de pares en ele todos los ítems)
- Se busca una ReglaPaquete activa con cantidadPares == totalPares
- Si hay match: totalNeto = regla.getPrecioTotalPaquete(); descuentoAplicado = subtotal - totalNeto
- Sin match: totalNeto = subtotal (

1.1 Categoria

// pedido/entity/Categoria.java
@Entity @Table(name="categorias")
id (Long, PK), nombreCategoria (Str

1.2 Producto

// producto/entity/Producto.java
@Entity @Table(name="productos")
id (Long), nombre (String), descripcion (String), precioBase (BigDecimal),
imagenUrl (String), categoria (@ManyToOne Categoria), activo (boolean=true)

// Tallas como colección simple — sin entidad separada:
@ElementCollection
@CollectionTable(name="producto_taln(name="producto_id"))
@Column(name="talla")
Set<Integer> tallasDisponibles;
Esto genera la tabla producto_tallaticamente vía Hibernate.

1.3 EstadoPedido

// pedido/entity/EstadoPedido.java
@Entity @Table(name="estados_pedido")
id (Long), nombreEstado (String)
// Seed (en orden): "Por confirmar"", "Entregado", "Devuelto"

1.4 Pedido

// pedido/entity/Pedido.java
@Entity @Table(name="pedidos")
id (Long)
cliente       (@ManyToOne(optional=s compra de invitado
asesor        (@ManyToOne AsesorVentas)
fecha         (LocalDateTime)
subtotal      (BigDecimal)
descuentoAplicado (BigDecimal)
totalNeto     (BigDecimal)
estadoActual  (@ManyToOne EstadoPedido)

// Datos del comprador (requeridos para invitados; para clientes se copian del perfil)
compradorNombre    (String)
compradorApellido  (String)
compradorCedula    (String, nullable)
compradorTelefono  (String)              // número WhatsApp del comprador

// Datos de entrega
modalidadEntrega   (String)              // "OFICINA" | "DOMICILIO"
direccionEnvio     (String, nullabllidadEntrega = "DOMICILIO"
municipio          (String)
departamento       (String)

1.5 PedidoDetalle

// pedido/entity/PedidoDetalle.java
@Entity @Table(name="historial_estados")
id (Long), pedido (@ManyToOne), asesor (@ManyToOne AsesorVentas),
estadoAnterior (@ManyToOne EstadoPedido), estadoNuevo (@ManyToOne EstadoPedido),
fechaCambio (LocalDateTime), observaciones (String)

---
Fase 2: Repositorios

Todos extienden JpaRepository. Métodos custom clave:

// ProductoRepository
List<Producto> findByActivoTrue();
List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String q);
List<Producto> findByCategoria_Id(Long categoriaId);

// PedidoRepository
List<Pedido> findByCliente_Id(Long clienteId);
List<Pedido> findByAsesor_Id(Long asesorId);

// ReglaPaqueteRepository  (nuevo)
Optional<ReglaPaquete> findByCantidadParesAndActivoTrue(Integer cantidadPares);
List<ReglaPaquete> findAllByActivoTrue();

// UserRepository (existente — agregar método):
List<AsesorVentas> findAllByRoleAndActivoTrue(Role role);  // traer asesores activos para round-robin

---
Fase 3: PedidoService — Lógica de negocio central

3.1 Cálculo de precio neto con reglas de paquete configurables (RF6)

// Se inyecta ReglaPaqueteRepositor
private BigDecimal[] calcularTotales(BigDecimal subtotal, int totalPares) {
    // Buscar regla activa para exa
    Optional<ReglaPaquete> regla = reglaPaqueteRepository
        .findByCantidadParesAndActivoTrue(totalPares);
    if (regla.isPresent()) {
        BigDecimal totalNeto = regla.get().getPrecioTotalPaquete();
        BigDecimal descuento = subtotal.subtract(totalNeto);
        return new BigDecimal[]{ descuento.max(BigDecimal.ZERO), totalNeto };
    }
    return new BigDecimal[]{ BigDec
}
Retorna [descuentoAplicado, totalNee número de pares, el pedido paga elsubtotal completo.

3.2 Asignación equitativa de asesor — Round-Robin (RF7, RF19)

Estrategia: contador interno AtomicInteger que cicla sobre la lista dinámica de asesores activos.
// En PedidoService como campo del bean (@Service = singleton):
private final AtomicInteger roundRo(0);

private AsesorVentas asignarAsesor() {
    List<AsesorVentas> asesores = undActivoTrue(Role.ASESOR_VENTAS);
    if (asesores.isEmpty()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No hay asesores disponibles");
    int idx = roundRobinIndex.getAn();
    return asesores.get(idx);
}
- La lista se recarga en cada asignctivado, desaparece automáticamente.
- El índice se reinicia al reiniciar el servidor (aceptable para un proyecto de clase).

3.3 Generación de URL WhatsApp (RF7

private String generarUrlWhatsApp(AsesorVentas asesor, Pedido pedido) {
    String msg = "Hola, acabo de hatId() + ...;

1. Validar que el carrito no esté vacío
2. Validar campos obligatorios: compradorNombre, compradorApellido, compradorTelefono, modalidadEntrega, municipio, departamento; si modalidadEntrega=DOMICILIO también direccionEnvio
3. Calcular totalPares y subtotal desde los ítems (precio base del producto × cantidad)
4. Aplicar calcularTotales(subtotal, totalPares) → descuentoAplicado, totalNeto
5. Asignar asesor con round-robin asignarAsesor()
6. Crear y persistir Pedido (cliente nullable, datos del comprador copiados del request)
7. Persistir cada PedidoDetalle
8. Registrar en HistorialEstado
9. Retornar CheckoutResponse:
{
  "pedidoId": 42,
  "compradorNombre": "Juan Pérez",
  "totalNeto": 280000,
  "descuentoAplicado": 20000,
  "whatsappUrl": "https://wa.me/573001234567?text=..."
}
9. El mensaje de WhatsApp incluye: número de pedido, total, datos de entrega → asesor lo usa para contactar al cliente.

3.5 Cambio de estado (RF22) — solo avanzar, nunca retroceder

Por confirmar → Confirmado → En des
- Cadena validada: List.of("Por confirmar","Confirmado","En despacho","Entregado","Devuelto")
- El siguiente estado válido es el inmediato siguiente en la lista.
- "Devuelto" es estado terminal (no" solo es alcanzable desde "Entregado".
- Si el estado pedido no es el inmediato anterior → lanzar excepción 400.
- Registrar en HistorialEstado con asesorId (o adminId), fecha y hora.
- Quién puede cambiar estado: asesoid}/estado) y admin (vía/api/admin/pedidos/{id}/estado).

3.6 Reasignación de asesor (RF19) — solo admin, solo estados "Por confirmar" o "Confirmado"

if (!List.of("Por confirmar","ConfistadoActual().getNombreEstado())) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede reasignar en este estado");
}

---
Fase 4: Controladores y endpoints

ProductoController — /api/productosuctos (admin)

┌────────┬──────────────────────────────────────────────────────────┐
│ Método │               Ruta               │  Auth   │               Descripción               │
├────────┼──────────────────────────────────┼─────────┼─────────────────────────────────────────┤
│ GET    │ /api/productos           de productos activos (paginado) │
├────────┼──────────────────────────────────┼─────────┼─────────────────────────────────────────┤
│ GET    │ /api/productos/{id}              │ Pública │ Detalle de producto                     │
├────────┼──────────────────────────────────────────────────────────┤
│ GET    │ /api/productos/search?q=         │ Pública │ Búsqueda por nombre/modelo              │
├────────┼──────────────────────────────────┼─────────┼─────────────────────────────────────────┤
│ GET    │ /api/categorias         e categorías                     │
├────────┼──────────────────────────────────┼─────────┼─────────────────────────────────────────┤
│ POST   │ /api/admin/productos             │ ADMIN   │ Crear producto                          │
├────────┼──────────────────────────────────────────────────────────┤
│ PUT    │ /api/admin/productos/{id}        │ ADMIN   │ Editar producto                         │
├────────┼──────────────────────────────────┼─────────┼─────────────────────────────────────────┤
│ PATCH  │ /api/admin/productos/{id/inactivar                       │
└────────┴──────────────────────────────────┴─────────┴─────────────────────────────────────────┘

PedidoController — rutas por rol

┌────────┬─────────────────────────────────┬─────────┬─────────────────────────────────────────────────┐
│ Método │              Ruta               │  Auth   │                   Descripción                   │
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────┤
│ POST   │ /api/pedidos                    │ Pública │ Checkout (invitado o cliente autenticado) →     │
│        │                         whatsappUrl│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────
┤
│ GET    │ /api/pedidos/mis-pedidos        │ CLIENTE │ Historial del cliente autenticado               │
├────────┼─────────────────────────────────────────────────────────────────┤
│ GET    │ /api/pedidos/{id}       de pedido propio│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────
┤
│ GET    │ /api/asesor/pedidos             │ ASESOR  │ Pedidos asignados al asesor autenticado         │
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────
┤
│ GET    │ /api/asesor/pedidos/{id}        │ ASESOR  │ Detalle de pedido asignado
│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────
┤
│ PATCH  │ /api/asesor/pedidos/{id}/estado │ ASESOR  │ Cambiar estado (avanzar)
│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────
┤
│ GET    │ /api/admin/pedidos              │ ADMIN   │ Todos los pedidos (con filtros opcionales)      │
├────────┼─────────────────────────────────────────────────────────────────┤
│ GET    │ /api/admin/pedidos/{id}         │ ADMIN   │ Detalle de cualquier pedido
│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────┤
│ PATCH  │ /api/admin/pedidos/{id}/estado (mismas reglas que asesor,│
│        │                                 │         │ incluyendo Devuelto)
│
├────────┼─────────────────────────────────┼─────────┼─────────────────────────────────────────────────┤
│ PATCH  │ /api/admin/pedidos/{id}/r asesor│
└────────┴─────────────────────────────────┴─────────┴─────────────────────────────────────────────────
┘

AdminController — /api/admin

┌────────┬────────────────────────────────────┬───────┬────────────────────────────────────────────────┐
│ Método │                Ruta                │ Auth  │                  Descripción                   │
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────
┤
│ GET    │ /api/admin/clientes                │ ADMIN │ Lista de clientes registrados                  │
├────────┼─────────────────────────────────────────────────────────────────┤
│ GET    │ /api/admin/clientes/{id}/historial │ ADMIN │ Historial de pedidos de un cliente
│
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────
┤
│ GET    │ /api/admin/asesores                │ ADMIN │ Lista de asesores activos                      │
├────────┼─────────────────────────────────────────────────────────────────┤
│ GET    │ /api/admin/kpis                    │ ADMIN │ Panel: total pedidos, ventas, clientes,        │
│        │                                    │       │ pedidos por estado
│
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────
┤
│ GET    │ /api/admin/descuentos              │ ADMIN │ Lista de reglas de paquete activas
│
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────┤
│ POST   │ /api/admin/descuentos   ueva regla (cantidadPares,│
│        │                                    │       │ precioTotalPaquete)
│
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────┤
│ PUT    │ /api/admin/descuentos/{iprecio de una regla│
├────────┼────────────────────────────────────┼───────┼────────────────────────────────────────────────
┤
│ DELETE │ /api/admin/descuentos/{id}         │ ADMIN │ Desactivar regla (soft delete: activo=false)   │
└────────┴─────────────────────────────────────────────────────────────────┘

KPI response:
{
  "totalPedidos": 1284,
  "ventasRealizadas": 84978793.0,
  "clientesRegistrados": 476,
  "pedidosPendientes": 32,
  "pedidosPorEstado": { "Por confir, "En despacho": 203, "Entregado": 320,"Devuelto": 32 }
}
ventasRealizadas = suma de totalNetevuelto".

---
Fase 5: Seeders / Data de prueba

Crear un CommandLineRunner o @PostConstruct que inicialice:
1. Los 5 estados de pedido si no existen (incluyendo "Devuelto" como último)
2. Un usuario Administrador y al me (con telefono)
3. Categorías base: Deportivo, Casual, Running
4. Reglas de paquete por defecto si no existen:
  - 2 pares → $190.000
  - 3 pares → $280.000
  - 4 pares → $360.000
  - 5 pares → $450.000

---
Orden de implementación recomendado

1. Modificar AsesorVentas.java para agregar campo telefono
2. Entidades (Categoria → Producto sDisponibles → EstadoPedido → Pedido →PedidoDetalle → HistorialEstado)
3. Repositorios (uno por entidad + método findAllByRoleAndActivoTrue en UserRepository)
4. ProductoService + ProductoContro simple, sirve de calentamiento)
5. PedidoService (descuentos, round-robin, WhatsApp URL, máquina de estados)
6. PedidoController (3 niveles de acceso: cliente, asesor, admin)
7. AdminController (KPIs y gestión
8. Seeders (datos de prueba)
9. SecurityConfig (actualizar rutas públicas: /api/productos/**, /api/categorias, POST /api/pedidos)

---
Verificación

1. Compilar con mvn clean install desde C:\.API\backend\
2. Verificar que Supabase crea las n ddl-auto=update)
3. Probar flujo completo con Postman/Insomnia:
  - Registrar cliente → login → ver catálogo → hacer checkout → verificar pedido creado y URL de WhatsApp retornada
  - Login como asesor → ver pedido asignado → cambiar estado
  - Login como admin → ver KPIs → r
4. Probar restricciones: asesor no puede ver pedidos de otro asesor, cliente no puede cambiar estados

El plan está aprobado. Empiezo la implementación. Primero leo el código existente para no romper nada.
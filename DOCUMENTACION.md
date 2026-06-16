# Quality Sports — Documentación Backend

Backend REST desarrollado con Spring Boot para la tienda de calzado deportivo Quality Sports (Cúcuta). Gestiona catálogo de productos, checkout con o sin registro, asignación de asesores y seguimiento de pedidos.

---

## Stack tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework | Spring Boot 4.0.6 / Java 21 |
| Base de datos | PostgreSQL en Supabase |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + JJWT 0.13.0 |
| Generación de código | Lombok |
| Build | Maven Wrapper (`mvnw`) |

---

## Configuración inicial

1. Copiar el archivo de ejemplo y completar los valores:

```
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

2. Editar `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://<host>.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0
spring.datasource.username=postgres.<project-ref>
spring.datasource.password=TU_PASSWORD

# Generar con: openssl rand -base64 32
jwt.secret=TU_JWT_SECRET_BASE64
jwt.expiration-ms=21600000
```

3. Crear el bucket de imágenes en **Supabase Dashboard → Storage → New bucket**:
   - Nombre: `productos`
   - Marcar como **Public** (para que las URLs sean accesibles sin autenticación)

4. Obtener la `service_role` key en **Supabase → Settings → API** (no es la `anon` key).

5. Levantar la aplicación:

```
.\mvnw.cmd spring-boot:run
```

Al iniciar, el `DataSeeder` crea automáticamente los estados, categorías, reglas de paquete y usuarios por defecto si no existen.

> **Importante:** `application.properties` está en `.gitignore` — nunca se sube al repositorio.

---

## Autenticación

La API usa **JWT Bearer tokens**. Para endpoints protegidos, incluir en cada request:

```
Authorization: Bearer <token>
```

- El token expira en **6 horas**.
- Se obtiene en `POST /api/auth/login` o `POST /api/auth/register`.
- El payload del token contiene el email (subject) y el rol (claim `role`).

---

## Roles y permisos

| Rol | Clase | Puede hacer |
|-----|-------|-------------|
| `CLIENTE` | `Cliente` | Registrarse, ver sus pedidos, hacer checkout autenticado |
| `ASESOR_VENTAS` | `AsesorVentas` | Ver y avanzar estado de sus pedidos asignados |
| `ADMINISTRADOR` | `Administrador` | Gestión completa: productos, pedidos, asesores, clientes, KPIs, descuentos |

### Credenciales por defecto (seed)

| Usuario | Email | Contraseña | Rol |
|---------|-------|-----------|-----|
| Administrador | `admin@qualitysports.com` | `Admin2026*` | ADMINISTRADOR |
| Asesor Uno | `asesor1@qualitysports.com` | `Asesor2026*` | ASESOR_VENTAS |
| Asesor Dos | `asesor2@qualitysports.com` | `Asesor2026*` | ASESOR_VENTAS |

---

## Referencia de API

### Autenticación

#### `POST /api/auth/register`
Registra un nuevo cliente y retorna un JWT.

**Auth:** Pública

**Body:**
```json
{
  "nombre": "Carlos Pérez",
  "email": "carlos@email.com",
  "password": "MiPassword123"
}
```

**Response `201`:**
```json
{
  "token": "eyJhbGci...",
  "email": "carlos@email.com",
  "nombre": "Carlos Pérez",
  "role": "CLIENTE"
}
```

**Errores:** `400` validación · `409` email ya registrado

---

#### `POST /api/auth/login`
Autentica un usuario y retorna un JWT.

**Auth:** Pública

**Body:**
```json
{
  "email": "admin@qualitysports.com",
  "password": "Admin2026*"
}
```

**Response `200`:**
```json
{
  "token": "eyJhbGci...",
  "email": "admin@qualitysports.com",
  "nombre": "Administrador",
  "role": "ADMINISTRADOR"
}
```

**Errores:** `401` credenciales incorrectas

---

### Productos (acceso público)

#### `GET /api/productos`
Lista productos activos con paginación.

**Auth:** Pública  
**Query params:** `page` (default 0) · `size` (default 20) · `sort`

**Response `200`:**
```json
{
  "content": [
    {
      "id": 1,
      "nombre": "Nike Air Max",
      "descripcion": "Zapatilla deportiva de alto rendimiento",
      "precioBase": 120000.00,
      "imagenUrl": "https://...",
      "categoria": { "id": 1, "nombreCategoria": "Deportivo" },
      "activo": true,
      "tallasDisponibles": [38, 39, 40, 41, 42]
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "number": 0
}
```

---

#### `GET /api/productos/search?q={termino}`
Busca productos activos por nombre (sin distinción de mayúsculas).

**Auth:** Pública

**Response `200`:** `List<ProductoDTO>` (misma estructura que arriba)

---

#### `GET /api/productos/{id}`
Retorna el detalle de un producto activo.

**Auth:** Pública

**Response `200`:** `ProductoDTO`  
**Errores:** `404` no encontrado o inactivo

---

#### `GET /api/categorias`
Lista todas las categorías disponibles.

**Auth:** Pública

**Response `200`:**
```json
[
  { "id": 1, "nombreCategoria": "Deportivo" },
  { "id": 2, "nombreCategoria": "Casual" },
  { "id": 3, "nombreCategoria": "Running" }
]
```

---

### Productos (administración)

#### `GET /api/admin/productos`
Lista **todos** los productos (activos e inactivos).

**Auth:** `ADMINISTRADOR`

**Response `200`:** `Page<ProductoDTO>` — igual que la pública pero incluye `activo: false`

---

#### `GET /api/admin/productos/{id}`
Retorna el detalle de cualquier producto (incluso inactivo).

**Auth:** `ADMINISTRADOR`

---

#### `POST /api/admin/productos`
Crea un nuevo producto.

**Auth:** `ADMINISTRADOR`

**Body:**
```json
{
  "nombre": "Adidas Ultraboost",
  "descripcion": "Zapatilla de running profesional",
  "precioBase": 135000.00,
  "imagenUrl": "https://...",
  "categoriaId": 3,
  "tallasDisponibles": [38, 39, 40, 41, 42, 43]
}
```

**Response `201`:** `ProductoDTO`

---

#### `PUT /api/admin/productos/{id}`
Actualiza un producto. Todos los campos son opcionales (actualización parcial).

**Auth:** `ADMINISTRADOR`

**Body:** Misma estructura que el POST, todos opcionales.

**Response `200`:** `ProductoDTO`

---

#### `POST /api/admin/productos/{id}/imagen`
Sube una imagen al bucket de Supabase Storage y actualiza el campo `imagenUrl` del producto.

**Auth:** `ADMINISTRADOR`

**Content-Type:** `multipart/form-data`

**Form field:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `file` | File | Imagen del producto (jpeg, png, webp, gif — máx. 5 MB) |

**Ejemplo con Postman:**
- Body → form-data → Key: `file` (tipo File) → seleccionar imagen local

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/api/admin/productos/1/imagen \
  -H "Authorization: Bearer <token>" \
  -F "file=@/ruta/imagen.jpg"
```

**Response `200`:** `ProductoDTO` con `imagenUrl` actualizada a la URL pública de Supabase:
```json
{
  "id": 1,
  "nombre": "Nike Air Max",
  "imagenUrl": "https://zwbavambpscnwkgwpgmx.supabase.co/storage/v1/object/public/productos/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "activo": true,
  "tallasDisponibles": [38, 39, 40, 41, 42]
}
```

**Errores:**
- `400` archivo vacío
- `404` producto no encontrado
- `415 Unsupported Media Type` el archivo no es una imagen
- `502 Bad Gateway` error al conectar con Supabase Storage

> La imagen también puede establecerse manualmente pegando una URL en `PUT /api/admin/productos/{id}` — ambos flujos coexisten.

---

#### `PATCH /api/admin/productos/{id}/estado`
Activa o desactiva un producto (soft delete).

**Auth:** `ADMINISTRADOR`

**Body:**
```json
{ "activo": false }
```

**Response `200`:** `ProductoDTO`

---

### Checkout (pedidos)

#### `POST /api/pedidos`
Crea un pedido. Funciona sin autenticación (compra anónima) o con JWT de cliente.

**Auth:** Pública (JWT opcional — si se envía, el pedido queda asociado al cliente)

**Body:**
```json
{
  "compradorNombre": "María",
  "compradorApellido": "González",
  "compradorCedula": "1234567890",
  "compradorTelefono": "3001234567",
  "modalidadEntrega": "DOMICILIO",
  "direccionEnvio": "Calle 5 # 10-20, Barrio Centro",
  "municipio": "Cúcuta",
  "departamento": "Norte de Santander",
  "items": [
    { "productoId": 1, "talla": 40, "cantidad": 2 },
    { "productoId": 3, "talla": 38, "cantidad": 1 }
  ]
}
```

> `direccionEnvio` es obligatorio solo cuando `modalidadEntrega` es `DOMICILIO`.  
> `modalidadEntrega` acepta: `OFICINA` o `DOMICILIO`.

**Response `201`:**
```json
{
  "pedidoId": 42,
  "compradorNombre": "María",
  "compradorApellido": "González",
  "subtotal": 375000.00,
  "descuentoAplicado": 95000.00,
  "totalNeto": 280000.00,
  "whatsappUrl": "https://wa.me/573001000001?text=Hola%2C+soy+Mar%C3%ADa..."
}
```

El asesor se asigna automáticamente por **turno rotativo (round-robin)**. La URL de WhatsApp dirige al chat del asesor asignado con el resumen del pedido prellenado.

---

### Pedidos (cliente autenticado)

#### `GET /api/pedidos/mis-pedidos`
Lista todos los pedidos del cliente autenticado.

**Auth:** Cualquier rol autenticado

**Response `200`:** `List<PedidoResponse>`

---

#### `GET /api/pedidos/{id}`
Detalle de un pedido propio. Solo el dueño puede verlo.

**Auth:** Cualquier rol autenticado

**Response `200`:** `PedidoResponse`  
**Errores:** `404` no encontrado o no pertenece al usuario

---

### Pedidos (asesor)

#### `GET /api/asesor/pedidos`
Lista los pedidos asignados al asesor autenticado.

**Auth:** `ASESOR_VENTAS`

---

#### `GET /api/asesor/pedidos/{id}`
Detalle de un pedido asignado. Solo si el pedido le pertenece al asesor.

**Auth:** `ASESOR_VENTAS`

---

#### `PATCH /api/asesor/pedidos/{id}/estado`
Avanza el estado del pedido al siguiente en la cadena.

**Auth:** `ASESOR_VENTAS`

**Body:**
```json
{ "observaciones": "El cliente confirmó el pago" }
```

**Response `200`:** `PedidoResponse`

---

### Pedidos (administrador)

#### `GET /api/admin/pedidos`
Lista todos los pedidos del sistema.

**Auth:** `ADMINISTRADOR`

---

#### `GET /api/admin/pedidos/{id}`
Detalle de cualquier pedido.

**Auth:** `ADMINISTRADOR`

---

#### `PATCH /api/admin/pedidos/{id}/estado`
Avanza el estado de cualquier pedido.

**Auth:** `ADMINISTRADOR`

**Body:** `{ "observaciones": "..." }`

---

#### `PATCH /api/admin/pedidos/{id}/asesor`
Reasigna el asesor de un pedido. Solo disponible en estados `Por confirmar` o `Confirmado`.

**Auth:** `ADMINISTRADOR`

**Body:**
```json
{ "asesorId": 3 }
```

**Response `200`:** `PedidoResponse`

---

### Panel de administración

#### `GET /api/admin/clientes`
Lista todos los clientes registrados.

**Auth:** `ADMINISTRADOR`

**Response `200`:**
```json
[
  {
    "id": 1,
    "nombre": "Carlos Pérez",
    "email": "carlos@email.com",
    "telefono": "3001234567",
    "direccionEnvio": "Calle 5 # 10-20",
    "activo": true,
    "createdAt": "2026-06-15T10:30:00"
  }
]
```

---

#### `GET /api/admin/clientes/{id}/historial`
Historial de pedidos de un cliente específico.

**Auth:** `ADMINISTRADOR`

**Response `200`:** `List<PedidoResponse>`

---

#### `GET /api/admin/asesores`
Lista los asesores activos.

**Auth:** `ADMINISTRADOR`

**Response `200`:**
```json
[
  {
    "id": 2,
    "nombre": "Asesor Uno",
    "email": "asesor1@qualitysports.com",
    "telefono": "573001000001",
    "zonaAsignada": "Norte"
  }
]
```

---

#### `GET /api/admin/kpis`
Métricas del negocio.

**Auth:** `ADMINISTRADOR`

**Response `200`:**
```json
{
  "totalPedidos": 120,
  "ventasRealizadas": 18500000.00,
  "clientesRegistrados": 45,
  "pedidosPendientes": 8,
  "pedidosPorEstado": {
    "Por confirmar": 5,
    "Confirmado": 3,
    "En despacho": 12,
    "Entregado": 95,
    "Devuelto": 5
  }
}
```

> `ventasRealizadas` excluye pedidos en estado `Devuelto`.

---

#### `GET /api/admin/descuentos`
Lista las reglas de descuento por paquete activas.

**Auth:** `ADMINISTRADOR`

**Response `200`:**
```json
[
  { "id": 1, "cantidadPares": 2, "precioTotalPaquete": 190000.00 },
  { "id": 2, "cantidadPares": 3, "precioTotalPaquete": 280000.00 },
  { "id": 3, "cantidadPares": 4, "precioTotalPaquete": 360000.00 },
  { "id": 4, "cantidadPares": 5, "precioTotalPaquete": 450000.00 }
]
```

---

#### `POST /api/admin/descuentos`
Crea una nueva regla de descuento.

**Auth:** `ADMINISTRADOR`

**Body:**
```json
{ "cantidadPares": 6, "precioTotalPaquete": 530000.00 }
```

---

#### `PUT /api/admin/descuentos/{id}`
Actualiza una regla existente.

**Auth:** `ADMINISTRADOR`

**Body:** Misma estructura que el POST.

---

#### `DELETE /api/admin/descuentos/{id}`
Desactiva una regla (soft delete — no se elimina de la base de datos).

**Auth:** `ADMINISTRADOR`

**Response:** `204 No Content`

---

## Estructura de PedidoResponse

Respuesta completa de un pedido (usada en múltiples endpoints):

```json
{
  "id": 42,
  "compradorNombre": "María",
  "compradorApellido": "González",
  "compradorTelefono": "3001234567",
  "modalidadEntrega": "DOMICILIO",
  "direccionEnvio": "Calle 5 # 10-20, Barrio Centro",
  "municipio": "Cúcuta",
  "departamento": "Norte de Santander",
  "estadoActual": "Confirmado",
  "asesorNombre": "Asesor Uno",
  "fecha": "2026-06-15T14:30:00",
  "subtotal": 375000.00,
  "descuentoAplicado": 95000.00,
  "totalNeto": 280000.00,
  "detalles": [
    {
      "productoId": 1,
      "productoNombre": "Nike Air Max",
      "talla": 40,
      "cantidad": 2,
      "precioUnitario": 120000.00,
      "subtotalItem": 240000.00
    },
    {
      "productoId": 3,
      "productoNombre": "Puma RS-X",
      "talla": 38,
      "cantidad": 1,
      "precioUnitario": 135000.00,
      "subtotalItem": 135000.00
    }
  ]
}
```

---

## Reglas de negocio

### Máquina de estados de pedidos

Los pedidos avanzan en un solo sentido. No se puede retroceder ni saltar estados.

```
Por confirmar → Confirmado → En despacho → Entregado → Devuelto
```

- **Devuelto** solo es alcanzable desde **Entregado**.
- Cada cambio queda registrado en `historial_estados` con quién lo hizo y cuándo.

### Descuentos por paquete

Cuando el total de pares en un pedido coincide con una regla activa, el `totalNeto` se fija al precio del paquete:

| Pares | Precio fijo (COP) | Ahorro aprox. |
|-------|-------------------|---------------|
| 2 | $190.000 | $10.000 (si precio base = $100.000/par) |
| 3 | $280.000 | — |
| 4 | $360.000 | — |
| 5 | $450.000 | — |

Si el total de pares no coincide con ninguna regla, no se aplica descuento (`totalNeto = subtotal`).

El administrador puede crear, modificar o desactivar reglas en cualquier momento vía `/api/admin/descuentos`.

### Asignación de asesores (round-robin)

Al crear un pedido, el sistema asigna automáticamente un asesor disponible en orden circular entre todos los asesores activos. El contador es seguro ante concurrencia (`AtomicInteger`) y desbordamiento (`Math.floorMod`).

### Checkout anónimo

`POST /api/pedidos` no requiere autenticación. Si el cliente envía un JWT válido, el pedido queda vinculado a su cuenta (visible en `GET /api/pedidos/mis-pedidos`). Si no hay token, el pedido se crea sin cliente asociado y el seguimiento es exclusivamente por WhatsApp.

---

## Esquema de base de datos

| Tabla | Descripción |
|-------|-------------|
| `users` | Base de usuarios (herencia JOINED) |
| `clientes` | Extiende `users` — clientes registrados |
| `asesores_ventas` | Extiende `users` — asesores de venta |
| `administradores` | Extiende `users` — administradores |
| `productos` | Catálogo de calzado |
| `producto_tallas` | Tallas disponibles por producto (ElementCollection) |
| `categorias` | Categorías de productos |
| `pedidos` | Pedidos de clientes |
| `pedido_detalles` | Líneas de detalle de cada pedido |
| `estados_pedido` | Catálogo de estados (5 registros fijos) |
| `historial_estados` | Auditoría de cambios de estado por pedido |
| `reglas_paquete` | Reglas de descuento por cantidad de pares |

### Relaciones clave

- `pedidos.cliente_id` → `users.id` (nullable — pedidos anónimos)
- `pedidos.asesor_id` → `users.id` (obligatorio)
- `pedidos.estado_actual_id` → `estados_pedido.id`
- `pedido_detalles.pedido_id` → `pedidos.id`
- `pedido_detalles.producto_id` → `productos.id`
- `historial_estados.modificado_por_id` → `users.id` (nullable — primer estado)
- `productos.categoria_id` → `categorias.id`

---

## Datos seed (al iniciar la aplicación)

### Estados de pedido
`Por confirmar` · `Confirmado` · `En despacho` · `Entregado` · `Devuelto`

### Categorías
`Deportivo` · `Casual` · `Running`

### Reglas de paquete
2 pares → $190.000 · 3 pares → $280.000 · 4 pares → $360.000 · 5 pares → $450.000

### Usuarios
Ver tabla de **Credenciales por defecto** más arriba.

---

## CORS

La API acepta peticiones desde:
- `http://localhost:5173` (Vite / Vue / React dev)
- `http://localhost:3000` (Create React App / Next.js dev)

Métodos permitidos: `GET · POST · PUT · DELETE · PATCH · OPTIONS`  
Credenciales: habilitadas (`allowCredentials: true`)

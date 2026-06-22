# Quality Sports — Backend

API REST del e-commerce de calzado deportivo **Quality Sports** (Cúcuta, Colombia).

## Stack

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Security + JWT (JJWT) | 0.13.0 |
| Spring Data JPA + Hibernate | — |
| PostgreSQL (Supabase) | — |
| Lombok | — |
| Maven | 3.9+ |

## Estructura de paquetes

```
com.qualitysports.backend/
├── auth/           # Login, registro, JWT filter
├── config/         # CORS, Security config, beans globales
├── admin/          # Gestión de asesores, clientes, descuentos (solo ADMINISTRADOR)
├── pedido/         # Ciclo de vida del pedido (creación → despacho → entregado)
├── producto/       # Catálogo, modelos, marcas
├── user/           # Perfil del cliente, cambio de contraseña
└── heka/           # Integración HekaEntrega (cotización y generación de guías)
```

## Roles

| Rol | Descripción |
|---|---|
| `CLIENTE` | Comprador autenticado |
| `ASESOR_VENTAS` | Asesor asignado por round-robin al crear pedido |
| `ADMINISTRADOR` | Acceso total al panel admin |
| Anónimo | Puede ver catálogo y añadir al carrito |

## Endpoints principales

### Auth
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/register` | Público |
| POST | `/api/auth/login` | Público |

### Productos
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/productos` | Público |
| GET | `/api/productos/{id}` | Público |
| POST/PUT/DELETE | `/api/productos/**` | ADMINISTRADOR |

### Pedidos
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/pedidos` | CLIENTE |
| GET | `/api/pedidos/mis-pedidos` | CLIENTE |
| GET | `/api/pedidos/{id}` | CLIENTE / ASESOR_VENTAS / ADMINISTRADOR |
| PUT | `/api/pedidos/{id}/estado` | ASESOR_VENTAS / ADMINISTRADOR |

### Cliente (perfil)
| Método | Ruta | Acceso |
|---|---|---|
| GET/PUT | `/api/cliente/perfil` | CLIENTE |
| PUT | `/api/cliente/password` | CLIENTE |

### Admin
| Método | Ruta | Acceso |
|---|---|---|
| GET/PUT/DELETE | `/api/admin/**` | ADMINISTRADOR |

## Variables de entorno

Crea un archivo `src/main/resources/application.properties` o configura estas variables de entorno en producción:

```properties
# Base de datos (PostgreSQL / Supabase)
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>

# JWT
JWT_SECRET=<clave-secreta-minimo-256-bits>
JWT_EXPIRATION_MS=86400000
```

## Reglas de paquetes (descuentos)

Los precios de paquete se leen de la tabla `reglas_paquete` — no están hardcodeados. Valores semilla:

| Pares | Precio COP |
|---|---|
| 2 | $190.000 |
| 3 | $280.000 |
| 4 | $360.000 |
| 5 | $450.000 |

## Setup desde cero

### Prerequisitos

- Java 21 (recomendado: [Eclipse Temurin](https://adoptium.net/))
- Maven 3.9+ (o usar el wrapper incluido `./mvnw`)
- PostgreSQL 15+ o una instancia Supabase

### Pasos

1. **Clona el repositorio y entra al directorio `backend/`**

2. **Crea la base de datos**
   ```sql
   CREATE DATABASE qualitysports;
   ```
   Hibernate generará las tablas automáticamente al iniciar (`spring.jpa.hibernate.ddl-auto=update`).

3. **Configura las variables de entorno** (ver sección anterior) o crea `src/main/resources/application-local.properties` con tus credenciales.

4. **Ejecuta el seed de datos iniciales** (opcional) — si existe un script SQL en `src/main/resources/data.sql`, se ejecutará automáticamente.

5. **Inicia el servidor**
   ```bash
   ./mvnw spring-boot:run
   ```
   La API queda disponible en `http://localhost:8080`.

### Verificar que funciona

```bash
curl http://localhost:8080/api/productos
```

Debe devolver un array JSON (vacío si no hay productos aún).

## Docker

```bash
# Construir imagen
docker build -t qualitysports-backend .

# Ejecutar
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=... \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e JWT_SECRET=... \
  qualitysports-backend
```

# ferreteria-auth

Servicio de autenticación de Ferretería Los Maestros (Spring Boot). Se encarga del inicio de sesión, el registro de clientes, la gestión de usuarios (solo administrador) y de emitir el token JWT que usan el frontend (`ferreteria-front`) y el servicio de la tienda (`ferreteria-back`).

Sigue el contrato de integración del proyecto (puerto **8082**, rutas bajo `/api`, errores como `{ "error": "mensaje" }`).

## Requisitos

- Java 17 o superior
- MySQL en ejecución (la base `ferreteria_auth` se crea sola)

## Configuración

Todo se puede cambiar con variables de entorno; los valores por defecto sirven para desarrollo.

| Variable | Qué es | Valor por defecto |
|---|---|---|
| `AUTH_DB_URL` | Dirección de la base de datos | `jdbc:mysql://localhost:3306/ferreteria_auth?createDatabaseIfNotExist=true&serverTimezone=UTC` |
| `AUTH_DB_USER` | Usuario de la base | `root` |
| `AUTH_DB_PASSWORD` | Contraseña de la base | (vacía) |
| `JWT_SECRET` | Secreto para firmar los tokens (Base64, mínimo 256 bits) | un valor de desarrollo |

**Importante:** `ferreteria-back` debe usar el mismo `JWT_SECRET` para poder validar los tokens. En cualquier entorno distinto del desarrollo hay que definir uno propio (por ejemplo, con `openssl rand -base64 48`).

## Cómo ejecutarlo

    ./mvnw spring-boot:run

En Windows: `mvnw.cmd spring-boot:run`. El servicio queda en `http://localhost:8082` y la documentación interactiva (Swagger) en `http://localhost:8082/swagger-ui.html`.

## Usuarios iniciales

Se crean al arrancar si no existen. La contraseña inicial de ambos es `Cambiar123!`.

| Correo | Rol |
|---|---|
| `admin@profesor.duoc.cl` | `ADMIN` |
| `vendedor@duoc.cl` | `VENDEDOR` |

## Rutas

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /api/auth/login` | público | Recibe `{ correo, password }` y devuelve `{ token, correo, rol }`. Credenciales incorrectas: 401 |
| `POST /api/auth/registro` | público | Crea un cliente (siempre rol `CLIENTE`). 201 con el usuario, sin contraseña; 409 si el correo o el RUN ya existen |
| `GET /api/usuarios` | ADMIN | Lista los usuarios |
| `GET /api/usuarios/{id}` | ADMIN | Un usuario; 404 si no existe |
| `POST /api/usuarios` | ADMIN | Crea un usuario (201) |
| `PUT /api/usuarios/{id}` | ADMIN | Edita un usuario; si no se envía `password` se conserva la actual |
| `DELETE /api/usuarios/{id}` | ADMIN | Elimina (204). No se puede eliminar ni quitar el rol al último administrador (409) |

Reglas principales:

- **Token:** HS256, emisor `ferreteria-auth`, `sub` = correo, claim `rol`, vence a la hora. Un token inválido o vencido en una ruta pública se ignora (no provoca 401).
- **Permisos:** sin token o con token inválido, 401; con un rol que no es `ADMIN` en `/api/usuarios`, 403 con `{ "error": "Acceso denegado" }`.
- **Cliente:** el rol `CLIENTE` exige RUN válido (con dígito verificador; se guarda como `12345678-5`), tipo de cliente (`particular` o `contratista`), región, comuna y dirección. Para `ADMIN` y `VENDEDOR` esos datos se guardan como `null`.
- **Contraseña:** de 4 a 72 caracteres; se guarda con BCrypt y nunca se devuelve.
- **Errores de validación:** 400 con `{ "error": "campo: detalle" }`.

## Pruebas

    ./mvnw test

Usan una base de datos en memoria (H2), así que no necesitan MySQL. Cubren el login, el registro, los permisos por rol, la gestión de usuarios, la protección del último administrador y CORS.

## Estructura

| Paquete | Contenido |
|---|---|
| `config` | Seguridad y CORS, OpenAPI y usuarios iniciales |
| `controller` | `AuthController` y `UsuarioController` |
| `dto` | Datos de entrada y salida (nunca incluyen la contraseña) |
| `exception` | Errores propios y el manejador global |
| `model` y `repository` | La entidad `Usuario` y su repositorio |
| `security` | Filtro que lee el token de cada petición |
| `service` | Login y registro, gestión de usuarios, JWT y validación del RUN |

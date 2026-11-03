# Tostado Cafeteria

Sistema web para la gestion de una cafeteria. Permite administrar clientes, empleados, mesas, productos, inventario, pedidos, pagos y reportes mediante un frontend HTML/CSS/JavaScript y una API REST desarrollada con Node.js, Express y MySQL.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Tecnologias](#tecnologias)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuracion inicial](#configuracion-inicial)
- [Ejecucion de la aplicacion](#ejecucion-de-la-aplicacion)
- [Acceso al sistema](#acceso-al-sistema)
- [Funcionalidades CRUD](#funcionalidades-crud)
- [API principal](#api-principal)
- [Estandares de codificacion](#estandares-de-codificacion)
- [Pruebas](#pruebas)
- [Copias de seguridad](#copias-de-seguridad)
- [Solucion de problemas](#solucion-de-problemas)

## Requisitos

- Windows 10 o Windows 11.
- Node.js 18 o superior.
- MySQL Server 8.
- MySQL Workbench o acceso por terminal a MySQL.
- Navegador web moderno.

Verificar Node.js:

```powershell
node --version
npm.cmd --version
```

## Tecnologias

### Frontend

- HTML5.
- CSS3.
- JavaScript moderno.
- `sessionStorage` para el token de autenticacion.

### Backend

- Node.js.
- Express 5.
- MySQL2.
- bcryptjs.
- JSON Web Token.
- dotenv.
- CORS.

### Base de datos

- MySQL 8.
- Motor InnoDB.
- Relaciones con claves foraneas.
- Restricciones de integridad y valores predeterminados.

## Estructura del proyecto

```text
tostado_cafeteria_/
├── database.sql              Base de datos, tablas y datos iniciales
├── index.html                Inicio y acceso del personal
├── pedido.html               Catalogo y registro de pedidos
├── admin.html                Dashboard ejecutivo
├── inventario.html           Administracion del inventario
├── menu.html                 Administracion del menu
├── mesas.html                Administracion de mesas
├── pos.html                  Punto de venta
├── reportes.html             Reportes
├── empleados.html            Administracion de empleados
├── clientes.html             Administracion de clientes
├── css/
│   └── styles.css            Estilos compartidos
├── js/
│   └── app.js                Navegacion y configuracion compartida
├── README.md                 Documentacion del proyecto
└── server/
    ├── server.js             Configuracion Express y rutas REST
    ├── db.js                 Conexion con MySQL
    ├── backup.js             Creacion de respaldos
    ├── package.json          Dependencias y comandos
    ├── package-lock.json     Versiones bloqueadas de dependencias
    ├── .env                  Variables privadas locales
    ├── node_modules/         Dependencias instaladas
    ├── backups/              Respaldos generados
    └── test/
        └── api.test.js       Pruebas de autenticacion y rutas
```

## Configuracion inicial

### 1. Instalar dependencias

Desde la carpeta principal:

```powershell

coloca la ubicación de la carpeta
ejemplo
cd C:\tostado_cafeteria_\server
npm.cmd install
```

### 2. Crear la base de datos

Desde MySQL Workbench:

1. Conectarse al servidor MySQL local.
2. Abrir el archivo `database.sql`.
3. Ejecutar todo el script.

El script crea la base de datos `tostado_cafe`, las tablas, las relaciones, las restricciones y los datos de ejemplo.

O usar una terminal, reemplazando la ruta si MySQL esta instalado en otra ubicacion:

```powershell
cd C:\Users\DELL\Desktop\tostado_cafeteria_
Get-Content .\database.sql -Raw |
  & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Verificar la instalacion:

```sql
USE tostado_cafe;
SHOW TABLES;
SELECT * FROM cafeteria_producto;
```

### 3. Configurar las variables de entorno

Modificar el archivo `server/.env`:

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=TU_CONTRASENA_MYSQL
DB_NAME=tostado_cafe
PORT=3000
```

Variables adicionales soportadas por la API:

```env
JWT_SECRET=CAMBIA_ESTA_CLAVE_SECRETA
JWT_EXPIRES_IN=8h
CORS_ORIGINS=http://localhost:8000,http://127.0.0.1:8000,http://localhost:5500,http://127.0.0.1:5500
MYSQLDUMP_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe
```

`server/.env` contiene informacion privada y no debe agregarse al repositorio ni compartirse.

## Ejecucion de la aplicacion

### Opcion recomendada: un solo servidor Node

Desde una terminal de PowerShell:

```powershell
cd C:\Users\DELL\Desktop\tostado_cafeteria_\server
node server.js
```

Mantener la terminal abierta mientras se usa la aplicacion.

Abrir el frontend:

```text
http://localhost:3000/
```

El servidor Express sirve la API y los archivos HTML, CSS y JavaScript.

### Opcion alternativa: frontend y API por separado

Terminal 1, API:

```powershell
cd C:\Users\DELL\Desktop\tostado_cafeteria_\server
node server.js
```

Terminal 2, frontend:

```powershell
cd C:\Users\DELL\Desktop\tostado_cafeteria_
python -m http.server 8000
```

Abrir:

```text
http://127.0.0.1:8000/index.html
```

### Comprobar la API

Estado principal:

```text
http://localhost:3000/
```

Estado de MySQL:

```text
http://localhost:3000/api/health
```

Una respuesta correcta contiene:

```json
{
  "ok": true,
  "conexion": true,
  "base": "tostado_cafe"
}
```

## Acceso al sistema

Credenciales de desarrollo:

```text
Usuario: admin
Contraseña: admin123
```

La primera vez que se inicia sesion se cifra la contraseña temporal y se guarda su hash en la base de datos. El usuario debe cambiarse antes de un despliegue real.

## Funcionalidades CRUD

El sistema implementa operaciones de insercion, consulta, actualizacion y eliminacion sobre varias entidades.

### Clientes

- Crear clientes desde la interfaz.
- Consultar y buscar clientes por nombre, telefono o correo.
- Editar nombre, telefono y correo.
- Eliminar clientes.
- Agregar puntos de fidelizacion.

Rutas principales:

```text
POST   /api/clientes
GET    /api/clientes
PATCH  /api/clientes/:id
DELETE /api/clientes/:id
PATCH  /api/clientes/:id/puntos
```

### Productos

- Crear productos del menu.
- Consultar todos los productos o solo los disponibles.
- Actualizar nombre, categoria, precio, descripcion, imagen y disponibilidad.
- Desactivar un producto del menu mediante una eliminacion logica.

```text
GET    /api/admin/productos
POST   /api/productos
PATCH  /api/productos/:id
DELETE /api/productos/:id
```

### Inventario

- Crear insumos.
- Consultar existencias y estados.
- Actualizar cantidades y niveles minimos.
- Desactivar insumos mediante una eliminacion logica.

```text
GET    /api/inventario
POST   /api/inventario
PATCH  /api/inventario/:id
DELETE /api/inventario/:id
```

### Empleados

- Crear empleados con usuario, rol y turno.
- Consultar empleados.
- Actualizar datos, contrasena, estado y permisos.
- Eliminar empleados.

```text
GET    /api/empleados
POST   /api/empleados
PATCH  /api/empleados/:id
DELETE /api/empleados/:id
```

### Mesas

- Crear mesas.
- Consultar y actualizar su estado.
- Eliminar mesas.

```text
GET    /api/mesas
POST   /api/mesas
PATCH  /api/mesas/:id
DELETE /api/mesas/:id
```

## API principal

### Ruta publica

```text
GET  /api/health
GET  /api/productos
GET  /api/mesas
POST /api/auth/login
POST /api/pedidos
```

### Rutas protegidas por JWT

Las rutas protegidas requieren el encabezado:

```http
Authorization: Bearer <TOKEN_JWT>
Content-Type: application/json
```

Roles disponibles:

- `administrador`
- `cajero`
- `barista`
- `mesero`

Ejemplos de rutas protegidas:

```text
GET   /api/pedidos
PATCH /api/pedidos/:id/estado
POST  /api/pagos
GET   /api/clientes
GET   /api/empleados
GET   /api/inventario
```

### Crear un pedido

El frontend envia identificadores y cantidades. Los precios finales se consultan en MySQL desde el backend.

Ejemplo con PowerShell:

```powershell
$body = @{
    tipo = "local"
    nombreCliente = "Juan Perez"
    mesaId = 1
    productos = @(
        @{
            productoId = 1
            cantidad = 2
        }
    )
} | ConvertTo-Json -Depth 4

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:3000/api/pedidos" `
    -ContentType "application/json" `
    -Body $body
```

### Registrar un pago

Primero se obtiene el token mediante el login:

```powershell
$login = @{
    usuario = "admin"
    password = "admin123"
} | ConvertTo-Json

$session = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:3000/api/auth/login" `
    -ContentType "application/json" `
    -Body $login

$headers = @{
    Authorization = "Bearer $($session.token)"
}
```

Luego se registra el pago:

```powershell
$payment = @{
    ordenId = 1
    metodo = "efectivo"
    monto = 20
    cambio = 0
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:3000/api/pagos" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $payment
```

Metodos de pago admitidos:

- `efectivo`
- `tarjeta`
- `billetera`
- `qr`

## Estandares de codificacion

### Variables

- JavaScript usa nombres en `camelCase`.
- SQL usa identificadores descriptivos en minusculas.
- Las variables deben indicar su contenido o comportamiento.

Ejemplo:

```javascript
const selectedClientId = 10;
const clientsResponse = await window.tostadoApiFetch("/api/clientes");
```

Evitar nombres sin contexto como `x`, `dato` o `lista2`.

### Metodos y funciones

- JavaScript usa verbos descriptivos en `camelCase`.
- Las rutas Express usan nombres REST y parámetros claramente definidos.

Ejemplos:

```javascript
async function loadClients() {}
function renderClientTable() {}
function validateClientData() {}
```

### Clases

- Las clases SQL utilizan nombres descriptivos que representan entidades.
- Las clases CSS usan nombres en minusculas, separados por guiones cuando sea necesario.

Ejemplos:

```css
.client-page
.clients-table
.client-modal
```

### Paquetes y carpetas

- El paquete principal del backend se encuentra en `server/`.
- Las rutas web se mantienen separadas por archivos HTML.
- Los recursos compartidos se organizan en `css/` y `js/`.

### HTML y SQL

- Los formularios deben usar `label` vinculados a cada control.
- Los IDs de control deben ser unicos dentro de la pagina.
- Las consultas SQL deben usar parametros `?`; no concatenar valores del usuario.
- Las operaciones criticas deben comprobar permisos y datos enviados.

## Pruebas

Desde la carpeta `server`:

```powershell
npm.cmd test
```

Las pruebas actuales verifican:

- La ruta principal de la API.
- La autenticacion requerida en pedidos.
- La autenticacion requerida en pagos.

## Copias de seguridad

Crear una copia:

```powershell
cd C:\Users\DELL\Desktop\tostado_cafeteria_\server
npm.cmd run backup
```

El respaldo se crea dentro de:

```text
server/backups/
```

Restaurar un respaldo:

```powershell
Get-Content .\server\backups\ARCHIVO.sql -Raw |
  & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

## Solucion de problemas

### PowerShell bloquea npm

Usar los comandos `.cmd`:

```powershell
npm.cmd install
npm.cmd start
npm.cmd test
```

Tambien es posible permitir scripts solo durante la sesion actual:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
npm start
```

### Error de conexion con MySQL

Comprobar:

- El servicio MySQL esta iniciado.
- `DB_HOST` y `DB_PORT` son correctos.
- `DB_USER` y `DB_PASSWORD` coinciden con MySQL.
- La base `tostado_cafe` fue creada.
- El puerto MySQL es accesible.

### Error ER_ACCESS_DENIED_ERROR

Actualizar `server/.env` con la contraseña real de MySQL y reiniciar el servidor Node.

### El puerto 3000 esta ocupado

Cambiar temporalmente el puerto de la API en `server/.env`:

```env
PORT=3001
```

Si se cambia el puerto, actualizar las referencias de la API en el frontend.

### El origen esta bloqueado por CORS

Agregar el origen exacto del frontend:

```env
CORS_ORIGINS=http://localhost:8000,http://127.0.0.1:8000
```

### MySQL devuelve ETIMEDOUT

MySQL puede estar detenido, bloqueado por un firewall o escuchando en otro host. Verificar el puerto con:

```powershell
netstat -ano | findstr ":3306"
```

## Seguridad

Antes de publicar el sistema:

- Cambiar `JWT_SECRET` por una clave aleatoria.
- Cambiar la contraseña temporal `admin123`.
- Crear un usuario MySQL sin permisos administrativos.
- Restringir `CORS_ORIGINS` a los dominios reales.
- No subir `.env` ni respaldos SQL.
- Usar HTTPS en un entorno de produccion.

# Tostado Cafeteria - Version Java

Aplicacion web desarrollada con Java 17, Jakarta Servlet 6, JSP, JSTL, Maven, MySQL y Apache Tomcat 10.

La aplicacion conserva el estilo visual de la interfaz original y agrega mejoras en la conexion a MySQL, la navegacion administrativa, el Dashboard, el carrito de pedidos y la gestion de cuentas.

## Tecnologias utilizadas

- Java 17
- Maven
- Jakarta Servlet 6
- JSP y JSTL
- MySQL 8
- MySQL Connector/J 8.4
- Apache Tomcat 10.1.31
- JBCrypt para contrasenas
- JUnit 5

## Requisitos

- JDK 17.
- Maven 3.8 o superior.
- MySQL Server 8.
- Apache Tomcat 10.
- Un navegador web moderno.

## Activar los programas necesarios

### 1. Activar Java y Maven

Compruebe que Java 17 y Maven estan disponibles antes de compilar:

```powershell
java -version
javac -version
mvn -version
```

El proyecto requiere Java 17. Si `java` o `mvn` no se reconocen, configure las variables de entorno de Windows:

```text
JAVA_HOME=C:\Program Files\Java\jdk-17
MAVEN_HOME=C:\Tools\apache-maven-3.9.11
```

Agregue a `Path`:

```text
%JAVA_HOME%\bin
%MAVEN_HOME%\bin
```

Cierre y vuelva a abrir la terminal despues de modificar las variables.

### 2. Activar MySQL

Inicie el servicio desde una terminal de PowerShell ejecutada como administrador:

```powershell
net start MySQL80
```

El nombre del servicio puede cambiar segun la instalacion. Puede consultar los servicios disponibles con:

```powershell
Get-Service *mysql*
```

Si el servicio aparece detenido, activelo:

```powershell
Start-Service NombreDelServicioMySQL
```

### 3. Activar la politica de PowerShell

Si Windows impide ejecutar scripts o comandos de Maven, npm o archivos `.ps1`, active temporalmente la ejecucion para la sesion actual:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Esta opcion termina al cerrar la terminal y no modifica permanentemente las politicas del equipo.

Para comprobar la politica activa:

```powershell
Get-ExecutionPolicy -List
```

### 4. Activar Tomcat

Configure primero las variables de conexion MySQL si son necesarias. Luego puede iniciar Tomcat desde su carpeta:

```powershell
cd C:\Tools\apache-tomcat-10.1.31\bin
.\startup.bat
```

Para detener Tomcat:

```powershell
cd C:\Tools\apache-tomcat-10.1.31\bin
.\shutdown.bat
```

Si el puerto `8080` esta ocupado, cierre el proceso anterior o configure otro puerto en:

```text
C:\Tools\apache-tomcat-10.1.31\conf\server.xml
```

### 5. Activar el proyecto desde VS Code

Desde la raiz del proyecto:

1. Instale las extensiones **Extension Pack for Java** y **Community Server Connectors** o **Debugger for Java** con soporte de Tomcat.
2. Abra la carpeta del proyecto.
3. Compile con:

```powershell
mvn clean package
```

4. Espere a que VS Code importe el proyecto Maven.
5. Agregue el WAR `target/tostado-cafeteria.war` o el proyecto Maven al servidor Tomcat configurado en la extension.
6. Inicie el servidor desde el panel de servidores o con el comando `Java: Run`.
7. Abra la aplicacion:

```text
http://localhost:8080/tostado-cafeteria/
```

La configuracion [web.xml](src/main/webapp/WEB-INF/web.xml) establece el servlet de bienvenida como `login`. Por eso la pagina inicial muestra el acceso a la cafeteria y al personal.

## Base de datos

La configuracion predeterminada utiliza:

- Host: `localhost`
- Puerto: `3306`
- Base de datos: `tostado_cafe`
- Usuario: `root`
- Contrasena: vacia

El proyecto lee estas variables de entorno antes de usar los valores predeterminados:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

La cadena JDBC se construye en:

[DatabaseConfig.java](src/main/java/com/tostado/cafeteria/config/DatabaseConfig.java)

Contiene el controlador `com.mysql.cj.jdbc.Driver`, la codificacion UTF-8 y `allowPublicKeyRetrieval=true` para permitir la conexion con MySQL cuando la autenticacion lo requiere.

### Crear la base de datos

1. Iniciar el servicio MySQL.
2. Abrir MySQL Workbench o una terminal.
3. Ejecutar el archivo:

```text
database.sql
```

Tambien se puede importar desde PowerShell:

```powershell
Get-Content .\database.sql -Raw |
  & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Verificar las tablas:

```sql
USE tostado_cafe;
SHOW TABLES;
SELECT * FROM cafeteria_producto;
```

### Configurar credenciales en Windows

Ejecutar los siguientes comandos en la terminal antes de iniciar Tomcat si MySQL usa credenciales distintas:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "tostado_cafe"
$env:DB_USER = "root"
$env:DB_PASSWORD = "TU_CONTRASENA_MYSQL"
```

Las credenciales no se guardan dentro de las clases Java ni se deben subir al repositorio.

Si aparecen errores de autenticacion, comprobar primero que el usuario y la contrasena funcionan con MySQL Workbench o desde la terminal. El archivo [database.sql](database.sql) crea la estructura, pero no reemplaza una contrasena administrativa configurada directamente en MySQL.

### Diferencia entre una contrasena de MySQL y una contrasena de empleado

- `DB_PASSWORD` es la clave que Java usa para conectarse al servidor MySQL.
- Las claves de empleados se almacenan como hashes BCrypt en `cafeteria_empleado.contrasena_hash`.
- No se debe almacenar la contrasena original de un empleado en texto plano.

[Passwords.java](src/main/java/com/tostado/cafeteria/config/Passwords.java) valida hashes nulos, incompletos o incompatibles antes de llamar a BCrypt. Esto evita errores como `Invalid salt revision`.

## Ejecutar el proyecto

### Compilar y ejecutar pruebas

Desde la raiz del proyecto:

```powershell
mvn clean test
```

### Generar el archivo WAR

```powershell
mvn clean package
```

El archivo generado queda en:

```text
target\tostado-cafeteria.war
```

### Desplegar en Tomcat

1. Detener Tomcat antes de reemplazar una version anterior.
2. Copiar el WAR al directorio `webapps`:

```powershell
Copy-Item .\target\tostado-cafeteria.war `
  "C:\Tools\apache-tomcat-10.1.31\webapps\tostado-cafeteria.war" -Force
```

3. Iniciar Tomcat.
4. Abrir:

```text
http://localhost:8080/tostado-cafeteria/
```

## Acceso administrativo

Credenciales de demostracion:

```text
Usuario: admin
Contrasena: admin123
```

El ingreso se realiza desde el boton **Acceso del personal** en la pagina principal.

## Funcionalidades CRUD

El sistema implementa insercion, consulta, actualizacion y eliminacion para recursos administrables:

- Clientes.
- Productos.
- Insumos.
- Proveedores.
- Empleados y cuentas.
- Mesas.
- Pedidos.
- Pagos.
- Recetas.
- Compras de insumos.

Las operaciones son procesadas por servlets Jakarta y el repositorio:

- [AppServlet.java](src/main/java/com/tostado/cafeteria/controller/AppServlet.java)
- [CrudRepository.java](src/main/java/com/tostado/cafeteria/repository/CrudRepository.java)

Las operaciones de cuentas utilizan:

- `POST` para crear o actualizar una cuenta.
- `DELETE` mediante `_method=DELETE` para eliminar una cuenta.
- Consultas preparadas con `PreparedStatement`.

## Mejoras implementadas

### Conexion y seguridad

- Conexion centralizada con MySQL Connector/J.
- Configuracion mediante variables de entorno.
- Uso de consultas preparadas.
- Codificacion UTF-8.
- Validacion segura de hashes BCrypt.
- Proteccion ante hashes nulos o invalidos.
- Contrasenas de cuentas almacenadas mediante hashes.

### Interfaz

- Se reconstruyo la aplicacion en Java sin conservar la dependencia del frontend Node.js.
- Se corrigieron errores de 404, rutas, codificacion y navegacion.
- Se mantuvo el estilo original de Tostado Cafeteria.
- La navegacion administrativa utiliza una barra blanca, limpia y consistente.
- El Dashboard muestra ventas, pedidos, ticket promedio, productos y estados de pedidos.
- Se agrego la pantalla de Gestion de Cuentas con tarjetas por usuario y resumen por rol.
- Las paginas administrativas comparten la misma barra de navegacion y estilos.

### Cuentas

- Crear cuentas con usuario, rol, turno y contrasena.
- Editar cuentas existentes.
- Eliminar cuentas desde la tarjeta correspondiente.
- Conservar la contrasena actual cuando se edita una cuenta sin ingresar una nueva.
- Mostrar resumenes de administradores, cajeros, baristas y otros roles.
- Redirigir las operaciones correctamente a `/cuentas`.

### Carrito y pedidos

- El catalogo publico conserva el diseño original.
- Los productos se cargan desde MySQL.
- Se puede agregar y quitar productos del carrito.
- El carrito puede mostrar la cantidad, precio unitario y total.
- Los pedidos pueden enviarse al backend Java.

## Estructura principal

```text
tostado_cafeteria_/
├── database.sql
├── pom.xml
├── README.md
├── src/main/java/com/tostado/cafeteria/
│   ├── config/
│   ├── controller/
│   ├── filter/
│   ├── model/
│   └── repository/
└── src/main/webapp/
    ├── WEB-INF/views/
    ├── assets/
    ├── css/
    └── pedido.html
```

## Estandares de codificacion

- Clases en `PascalCase`: por ejemplo, `DatabaseConfig`, `CrudRepository`.
- Metodos en `camelCase`: por ejemplo, `getConnection`, `dashboard`.
- Variables en `camelCase`: por ejemplo, `database`, `passwordHash`.
- Constantes en `UPPER_Snake_Case`: por ejemplo, `TABLES`.
- Paquetes Java en minusculas y separados por puntos.
- Los recursos de base de datos utilizan nombres permitidos para evitar SQL construido con entrada externa.
- Las vistas JSP respetan codificacion UTF-8.

## Pruebas

Ejecutar todas las pruebas:

```powershell
mvn test
```

Las pruebas actuales verifican:

- Hashes y validacion BCrypt.
- Creacion y propiedades basicas de empleados.

## Solucion de problemas

### `Invalid salt revision`

El hash guardado en la base de datos no tiene un formato BCrypt valido.

La clase de validacion verifica el prefijo:

```text
$2a$
$2b$
$2y$
```

Si una cuenta tiene un hash invalido, genere uno nuevo desde Java o restablezca la cuenta administrador.

### `Unknown database tostado_cafe`

Importar nuevamente `database.sql`.

```sql
CREATE DATABASE IF NOT EXISTS tostado_cafe;
USE tostado_cafe;
```

### `Communications link failure`

Comprobar que:

- El servicio MySQL este iniciado.
- `DB_HOST` y `DB_PORT` sean correctos.
- El usuario tenga acceso a la base.
- El firewall permita el puerto `3306`.

### `HTTP 404`

Verificar que el proyecto este desplegado con el nombre:

```text
tostado-cafeteria.war
```

La ruta esperada es:

```text
http://localhost:8080/tostado-cafeteria/dashboard
```

### Caracteres extraños

La base, las tablas y las vistas deben usar UTF-8:

- Juego de caracteres: `utf8mb4`.
- Colacion: `utf8mb4_unicode_ci`.
- JSP con `charset=UTF-8`.
- MySQL URL con `characterEncoding=UTF-8`.

# Tostado Cafeteria - Inicio rapido Java

Aplicacion web Java 17 con Jakarta Servlet, JSP, Maven, MySQL y Apache Tomcat 10.

## Requisitos

- Java 17 o superior.
- Apache Maven.
- MySQL 8.
- Apache Tomcat 10.1.
- Windows PowerShell.

## 1. Crear la base de datos

Ejecuta el archivo `database.sql`:

```powershell
Get-Content .\database.sql -Raw |
  & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

La base creada se llama `tostado_cafe`.

## 2. Configurar la conexion a MySQL

Edita `C:\Tools\apache-tomcat-10.1.31\bin\setenv.bat`:

```bat
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=tostado_cafe"
set "DB_USER=root"
set "DB_PASSWORD=TU_CONTRASENA"
```

No incluyas la contraseña real dentro del repositorio.

## 3. Compilar el proyecto

Desde la carpeta del proyecto:

```powershell
mvn clean package
```

El sistema genera:

```text
target\tostado-cafeteria.war
```

## 4. Instalar en Tomcat

Copia el WAR a Tomcat:

```powershell
Copy-Item `
  .\target\tostado-cafeteria.war `
  C:\Tools\apache-tomcat-10.1.31\webapps\tostado-cafeteria.war `
  -Force
```

## 5. Iniciar el sistema

Configura Java y inicia Tomcat:

```powershell
$env:CATALINA_HOME = "C:\Tools\apache-tomcat-10.1.31"
$env:CATALINA_BASE = "C:\Tools\apache-tomcat-10.1.31"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot"
$env:JRE_HOME = $env:JAVA_HOME

& "C:\Tools\apache-tomcat-10.1.31\bin\startup.bat"
```

Abre:

- Inicio de sesion: http://localhost:8080/tostado-cafeteria/login
- Pedidos publicos: http://localhost:8080/tostado-cafeteria/pedido.html
- Dashboard: http://localhost:8080/tostado-cafeteria/dashboard

## Detener el sistema

```powershell
& "C:\Tools\apache-tomcat-10.1.31\bin\shutdown.bat"
```

## Ejecutar pruebas

```powershell
mvn clean test
```

## Solucion rapida

- **Sin productos:** verifica MySQL, ejecuta `database.sql` y confirma que `cafeteria_producto.disponible` sea `1`.
- **Access denied:** corrige `DB_PASSWORD` en `setenv.bat` y reinicia Tomcat.
- **404 login:** confirma que exista el archivo `target\tostado-cafeteria.war` desplegado en Tomcat.
- **MySQL desconocida:** ejecuta nuevamente `database.sql`.

# Jakarta War Foundation

Use explicit Java 17 and Jakarta Servlet 6.0 Maven configuration for Tomcat 10.1 deployments.

## What Happened
The host defaulted to Java 25, but the modernization target requires Java 17. The Maven compiler was pinned to `maven.compiler.release=17` and the POM was built successfully with Temurin 17.21.

## Takeaway
Keep Servlet API dependencies at `provided` scope, package with `maven-war-plugin`, configure database credentials only through environment variables, and use allowlisted identifiers plus `PreparedStatement` parameters for CRUD repositories.

## History
- 2026-11-03 (tostado-cafeteria/001-java17-maven-tomcat-foundation): initial

# 001-java17-maven-tomcat-foundation

## Summary
Created the Java 17 Maven WAR foundation for Tomcat 10.1, Jakarta Servlet/JSP application packages, authentication/session infrastructure, MVC routes, role checks, JDBC repositories with `PreparedStatement`, CRUD/POS/report views, shared assets, tests, and setup documentation while preserving all existing HTML, CSS, JavaScript, Node.js, and `database.sql` files.

## Upstream Artifacts Consumed
- `.github/modernize/java-jakarta-tomcat-modernization/plan.md` — migration scope, target stack, delivery sequence.
- `.github/modernize/java-jakarta-tomcat-modernization/.metadata/tasks.json` — six task dependencies and success criteria.
- `database.sql` — MySQL schema and seed entities.

## Evidence Mapping
- plan.md Technical Framework Java 17/Jakarta/Tomcat 10.1/MySQL → `pom.xml`, `src/main/webapp/WEB-INF/web.xml`, `DatabaseConfig.java`
- tasks.json 001 requirements → `pom.xml` WAR packaging and Java `src/main/java` layout
- tasks.json 002-006 requirements → controllers/filter/repositories/views/tests/README
- `database.sql` entities → `CrudRepository` allowlisted tables and dashboard queries

## Deliverables
- Maven WAR build configured with Java 17 release compilation.
- Jakarta Servlet 6.0, JSP API 3.1, JSTL 3.0, MySQL Connector/J, BCrypt, and JUnit dependencies.
- Packages for `config`, `controller`, `filter`, `model`, `repository`, and `service`.
- Session login/logout, role protection, MySQL environment configuration, and PreparedStatement operations.
- Dashboard, reports, customer/product/inventory/employee/mesa/POS views and shared CSS/JavaScript.
- Unit tests for password hashing and employee role data.
- README Java/Tomcat/MySQL setup and deployment instructions.

## Test Results
- Command: `mvn clean verify`
- Passed: 3
- Failed: 0
- Skipped: 0
- Result: BUILD SUCCESS; `target/tostado-cafeteria.war` produced.
- Active toolchain: Java 17.0.21; Maven 3.9.11.

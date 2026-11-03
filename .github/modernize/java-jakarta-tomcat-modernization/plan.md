# Modernization Plan: Java 17 Jakarta Servlet/JSP Cafeteria Modernization

**Project**: Tostado Cafetería

## Technical Framework

- **Current language**: HTML, CSS, JavaScript, Node.js, SQL
- **Target language**: Java 17
- **Target framework**: Jakarta Servlet API 6.0+ and Jakarta JSP, compatible with Apache Tomcat 10.1
- **Build tool**: Maven
- **Database**: MySQL, using JDBC and the preserved `database.sql` schema
- **Architecture**: MVC-style controllers, models, services, repositories, configuration, and JSP views

## Overview

Modernize the complete cafeteria application without replacing the existing source files. The result will preserve the current business workflows while introducing:

- A deployable Java 17 Maven web application for Tomcat 10.1
- Session-based authentication and role-based authorization
- Full CRUD workflows for the primary cafeteria entities
- Server-rendered Jakarta JSP pages with shared navigation and styles
- JDBC access that uses parameterized statements
- Maven build and test verification with setup documentation

The existing static HTML, JavaScript, CSS, Node.js server code, and `database.sql` remain part of the original workspace and will not be overwritten as implementation prerequisites; the modernization adds the Java web application alongside them.

## Migration Impact Summary

| Application | Original Service | New Service | Authentication | Comments |
|-------------|------------------|-------------|----------------|----------|
| Cafeteria web app | Static HTML and browser JavaScript | Jakarta Servlet/JSP on Tomcat 10.1 | HTTP session with role checks | Preserve existing UI scope |
| Node.js API | Express routes and MySQL access | Java controllers, services, and repositories | Session-based login | Replace server behavior through the Java stack |
| MySQL data | `database.sql` schema and existing tables | Same MySQL schema through JDBC | Database credentials outside source | Preserve schema and never expose passwords |
| Shared UI | Existing styles and navigation | Shared JSP layout, CSS, and JavaScript | Authorized server routes | Keep existing files and add webapp assets |

## Scope

The implementation will cover login, dashboard, customers, menu/products, inventory and supplies, employees, tables, orders/POS, payments, reports, and shared UI navigation. Administrative mutations will require the administrator role; role-specific read and operational permissions will follow the application's existing role model.

The migration must preserve `database.sql`, keep database credentials configurable and outside source control, store employee passwords only as hashes, and use parameterized `PreparedStatement` operations throughout JDBC repositories.

## Delivery Approach

1. Establish the Java 17 and Maven/Tomcat project foundation.
2. Build session authentication, role authorization, JDBC configuration, and shared MVC infrastructure.
3. Implement entity CRUD and operational workflows in a single sequential implementation chain.
4. Complete dashboard, reports, shared navigation, styles, scripts, setup documentation, and Maven verification.
5. Scan dependencies for vulnerabilities and remediate findings.

## Open Questions & Questionnaire

- [x] Q: Target Java version? → A: Java 17
- [x] Q: Target server framework? → A: Jakarta Servlet/JSP compatible with Tomcat 10.1
- [x] Q: Database? → A: Preserve MySQL and `database.sql`
- [x] Q: Build tool? → A: Maven
- [x] Q: Authentication model? → A: HTTP sessions and role checks
- [x] Q: Deployment target? → A: No Azure deployment or infrastructure requested
- [x] Q: Integration testing environment? → A: Build and test with Maven; no separate integration-test service or infrastructure requested

## [001-java17-maven-tomcat-foundation] Added Jakarta WAR foundation
- Java 17 compiler release must be explicit because the host initially resolved to Java 25.
- Use environment variables in DatabaseConfig; no MySQL credentials belong in pom.xml or source.
- Table and column allowlists keep dynamic CRUD SQL from becoming user-controlled SQL identifiers.
- PreparedStatement binding is the consistent repository pattern.
- Learnings consumed: devops/(none)

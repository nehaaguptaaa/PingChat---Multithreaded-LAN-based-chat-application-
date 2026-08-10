### 1. Non-Existent Spring Boot Version (4.1.0)

• Your pom.xml was set to Spring Boot version 4.1.0 (which does not exist).
• Because the parent configuration failed, Spring Boot could not read your MongoDB URI from application.properties and defaulted to
localhost:27017—causing the initial Connection Refused error.

### 2. Invalid Dependency Artifact Names

• Your pom.xml contained non-existent dependencies (spring-boot-starter-webmvc, spring-boot-starter-data-mongodb-test, spring-boot-starter-webmvc-test).
Maven didn't know where to download them or what versions to assign.

### 3. JDK 25 Incompatibility with Lombok

• IntelliJ was compiling the project using JDK 25 (openjdk-25.0.1).
• Lombok does not support JDK 25 yet and crashed during compilation (TypeTag :: UNKNOWN). Because the compiler failed, no .class files were produced,
which caused the ClassNotFoundException.
──────
### Summary of Fixes Applied:

1. Spring Boot: Updated parent version to 3.4.2 in pom.xml.
2. Dependencies: Replaced invalid starters with standard spring-boot-starter-web and spring-boot-starter-test.
3. JDK Runtime: Configured the build to use Java 21 (temurin-21.0.9).
# Java Runtime Configuration Summary

## Current Environment

### Java Installation
- **Java Version**: Java 21.0.1 LTS
- **Java Home**: C:\Program Files\Java\jdk-21
- **Java Executable**: java.exe

### Configuration Status

#### ✅ Completed
1. **VS Code Extension Pack for Java** - Installed
   - Language Support for Java (Red Hat)
   - Debugger for Java
   - Test Runner for Java
   - Maven for Java
   - Project Manager for Java
   - Visual Studio IntelliCode

2. **Workspace Configuration Files** - Created
   - `.vscode/settings.json` - Java language server and Maven settings
   - `.vscode/launch.json` - Debug configurations for all three services
   - `.vscode/extensions.json` - Workspace extension recommendations

3. **Environment Variables** - Set
   - `JAVA_HOME` = C:\Program Files\Java\jdk-21
   - `MAVEN_HOME` = C:\Users\User\AppData\Local\Maven\apache-maven-3.9.6

4. **Maven Wrapper Scripts** - Created
   - `mvnw.cmd` - Windows Maven wrapper
   - `mvnw` - Unix Maven wrapper (for WSL compatibility)

## Project Structure

### Services
1. **Order Service** (order-service/)
   - Main Class: com.bit313.OrderServiceApplication
   - Java Version: 21
   - Spring Boot 3.2.0

2. **Inventory Service** (inventory-service/)
   - Main Class: com.bit313.InventoryServiceApplication
   - Java Version: 21
   - Spring Boot 3.2.0

3. **Payment Service** (payment-service/)
   - Main Class: com.bit313.PaymentServiceApplication
   - Java Version: 21
   - Spring Boot 3.2.0

## How to Use

### Debug Individual Services
In VS Code:
1. Open the Debug view (Ctrl+Shift+D)
2. Select one of these configurations:
   - "Order Service - Debug"
   - "Inventory Service - Debug"
   - "Payment Service - Debug"
3. Press F5 to start debugging

### Build Services
In VS Code Terminal:
```bash
# Using Maven wrapper (if Maven CLI is unavailable)
# The Extension Pack includes Maven integration

# Build individual service
mvn clean install -f order-service/pom.xml

# Build all services
mvn clean install
```

### Run Tests
```bash
mvn test -f <service>/pom.xml
```

## Next Steps

1. **Install Maven CLI (Optional)**
   - If Maven commands are needed in terminal, download from:
     https://maven.apache.org/download.cgi
   - Extract to a location and add to PATH

2. **Configure IDE Settings** (Already Done)
   - Java formatting preferences
   - Test runner configuration
   - Maven auto-import

3. **Install Recommended Extensions** (Optional)
   - SonarLint (for code quality)
   - Docker (for containerization)
   - Makefile Tools

## Verification

To verify the setup:
1. Open any .java file - syntax highlighting and IntelliCode should work
2. Open Debug view - all three service configurations should be visible
3. Try setting a breakpoint and pressing F5 - debugging should work

## Configuration Files Location

- `.vscode/settings.json` - Global workspace settings
- `.vscode/launch.json` - Debug/run configurations
- `.vscode/extensions.json` - Extension recommendations
- `.mvn/extensions.xml` - Maven extensions (if needed)
- `*/pom.xml` - Individual service build files

## Java Version Compatibility

All services require:
- **Java 21** or later
- **Maven 3.6.0** or later
- **Spring Boot 3.2.0**

Current environment meets all requirements.

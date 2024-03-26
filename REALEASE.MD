
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This page is mean to help developer to release this the SDK.

## Run the Test

### Prerequisites

To run the tests you need 3 different environments: Dev, PROD and a local installation. For Astra DEV and production you should have the following environment variables sets: `ASTRA_DB_APPLICATION_TOKEN_DEV` and `ASTRA_DB_APPLICATION_TOKEN`.

- Generate Jacoco Report
```json
mvn clean test jacoco:report 
```

- Push to SONAR
```json
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-db-java
```



## Run against Local HCD Instance 
(_to run locally your must have the data api running locally on op of HCD or DSE_)

```bash
mvn clean test -Ptest_local
```

> **Why it works ?**
> 
> - In `pom.xml` the environment variable `ASTRA_DB_JAVA_TEST_ENV` is set to `local` 
> - Tests are annotated with
> 
> ```java
> @EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
> @DisabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "(?!local)")
> public class MyTest {
> }
> ```

- To run with IDE make sure to set the environment variable `ASTRA_DB_JAVA_TEST_ENV` to `local` in your Run configuration


## Run against Astra DEV

- `ASTRA_DB_APPLICATION_TOKEN_DEV` should be defined in your environment variables, it is not in the different POM for privacy reasons.

- To run with Maven (AWS,`eu-west-2`)

```bash
mvn clean test -Ptest_astra_dev
```

- To run with Maven on One particular region

```java
mvn clean test -Ptest_astra_dev \
  -DASTRA_CLOUD_PROVIDER_DEV=GCP \
  -DASTRA_CLOUD_REGION_DEV=us-central-1
```

- To run overriding also the token

```java
mvn clean test \
  -Ptest_astra_dev \
  -DASTRA_CLOUD_PROVIDER_DEV=GCP \
  -DASTRA_CLOUD_REGION_DEV=us-central-1 \
  -ASTRA_DB_APPLICATION_TOKEN_DEV=AstraCS:... \
  -Dtest="com.datastax.astra.test.integration.*.*Test"
```


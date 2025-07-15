## Work with local HCD

### Installation

1. Prerequisites: 

- Docker
- Docker Compose
- Java 17+
- Maven

2. Make sure you have the `DATA_API_FOLDER` environment variable set to the root of your local Data API project. 
 
```
export DATA_API_FOLDER=/Users/cedricklunven/dev/datastax/JAVA/data-api
```

### Start  

- Start HCD

Locate the `docker-compose` folder and start with `-d`, you do not need the API

```bash
cd $DATA_API_FOLDER/docker-compose` && start_hcd.sh -d
```

- Start the Data API

```bash
cd $DATA_API_FOLDER
STARGATE_DATA_STORE_SAI_ENABLED=true \
STARGATE_DATA_STORE_VECTOR_SEARCH_ENABLED=true \
STARGATE_JSONAPI_OPERATIONS_VECTORIZE_ENABLED=true \
STARGATE_DATA_STORE_IGNORE_BRIDGE=true \
STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_LOCAL_DATACENTER=dc1 \
STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_CASSANDRA_END_POINTS=localhost \
QUARKUS_HTTP_ACCESS_LOG_ENABLED=FALSE \
QUARKUS_LOG_LEVEL=INFO \
JAVA_MAX_MEM_RATIO=75 \
JAVA_INITIAL_MEM_RATIO=50 \
GC_CONTAINER_OPTIONS="-XX:+UseG1GC" \
JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager" \
mvn quarkus:dev -Dstargate.data-store.ignore-bridge=true -Dstargate.jsonapi.operations.vectorize-enabled=true -Dstargate.jsonapi.operations.database-config.local-datacenter=dc1 -Dquarkus.log.console.darken=2 -Dstargate.feature.flags.tables=true -Dstargate.jsonapi.operations.extend-error=true -Dstargate.feature.flags.reranking=true
```

- Access HCD with cqlh 

```asciidoc
docker run -it --rm --network container:$(docker ps | grep hcd | cut -b 1-12) cassandra:latest cqlsh -u cassandra -p cassandra
```

```asciidoc
podman exec -it $(podman ps | grep hcd | cut -b 1-12) cqlsh -u cassandra -p cassandra
```

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


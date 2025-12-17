
# Java Client for Data API

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.datastax.astra/astra-db-java)](https://search.maven.org/artifact/com.datastax.astra/astra-db-java)

This client library provides a simplified way to interact with the Data API for Astra DB Serverless, Hyper-Converged Database (HCD), or local instances.

For client libraries in other languages, see [Get started with the Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## Using the client

For Astra DB Serverless:

- [Quickstart for collections](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart.html)
- [Quickstart for tables](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart-tables.html)
- [Get started with the Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html)

For Hyper-Converged Database (HCD):

- [Quickstart for collections](https://docs.datastax.com/en/hyper-converged-database/1.2/api-reference/quickstart.html)
- [Quickstart for tables](https://docs.datastax.com/en/hyper-converged-database/1.2/api-reference/quickstart-tables.html)
- [Get started with the Data API](https://docs.datastax.com/en/hyper-converged-database/1.2/api-reference/dataapiclient.html)

For local instances, see [Using the Java client with a local instance](#using-the-java-client-with-a-local-instance) below.

## Contributing

### Prerequisites for contributing

#### 📦 Java Development Kit (JDK) 17

- Use the [reference documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) to install a **Java Development Kit**
- Validate your installation with:

   ```bash
   java --version
   ```

#### 📦 Apache Maven

- Use the [reference documentation](https://maven.apache.org/install.html) to install **Apache Maven**
- Validate your installation with:

   ```bash
   mvn -version
   ```

#### 📦 Docker (local Installation)

Docker is an open-source project that automates the deployment of software applications inside containers by providing an additional layer of abstraction and automation of OS-level virtualization on Linux.

### Packaging

- Clone the repository:

   ```console
   git clone git@github.com:datastax/astra-db-java.git
   ```

- Build the project (Java 11 and Maven is required)

> Note: You should skip the tests if you want to speed up the build. To run the test you need a bit of setup:
>
> - An environment variable `ASTRA_DB_APPLICATION_TOKEN` with your an Organization Administrator Astra token (PROD)
> - An environment variable `ASTRA_DB_APPLICATION_TOKEN_DEV` with your an Organization Administrator Astra token (DEV)
> - A running Data API locally with docker (see the `docker-compose.yml` in the root of the project)

```console
mvn clean install -Dtest.skipped=true
```

### Using the Java client with a local instance

> Prerequisite: You need HCD, DSE, or CASSANDRA running on your machine and listening on `9042`. One good way is to run HCD as a docker image following [these instructions](https://github.com/stargate/data-api/tree/main/docker-compose).

#### Run the Data API locally

- Clone the Data API repository:

   ```
   git clone git@github.com:stargate/data-api.git
   ```

- Access the folder and start the Data API. Note the cassandra endpoint is `localhost` and the datacenter is `dc1`.

   ```
   cd data-api

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
   JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.   logmanager.LogManager" \
   mvn quarkus:dev -Dstargate.data-store.ignore-bridge=true -Dstargate.jsonapi.operations.vectorize-enabled=true -Dstargate.jsonapi.operations.database-config.local-datacenter=dc1 -Dquarkus.log.console.darken=2 -Dstargate.feature.flags.tables=true -Dstargate.jsonapi.operations.extend-error=true -Dstargate.feature.flags.reranking=true
   ```

- Check that the Data API is running:

| Field                                      | Description                                                                             |
|--------------------------------------------|-----------------------------------------------------------------------------------------|
| **Data API Spec**                          | http://localhost:8181/swagger-ui/#/                                                     |
| **Data API Endpoint**                      | http://localhost:8181                                                                   |
| **Token Header Key**                       | `Token`                                                                                      |
| **Token Header Value**               | `Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh` (aka `Cassandra:Base64(userName):Base64(password)`) |
| **Authentication API Spec (before 1.0.6)** | http://localhost:8081/swagger-ui/#/                                                     |

- The API will have 3 resources:

| Field                 | Url                            | Description                                       |
|-----------------------|--------------------------------|---------------------------------------------------|
| **Namespace**         | `/v1/`                         | Interact with namespaces (not available in Astra) |
| **Data API Endpoint** | `/v1/{namespace}`              | Interact with collections of a namespace |
| **Token Header Key**  | `/v1/{namespace}/{collection}` |Interact with documents of a collection  |

#### Use the Java client with a local instance

To authenticate, use `cassandra` as the username and password to get a token.
Use `http://localhost:8181` as the endpoint.

```java
public class QuickStartLocal {

    public static void main(String[] args) {

        // Create a token
        String token = new UsernamePasswordTokenProvider("cassandra", "cassandra").getToken();
        System.out.println("Token: " + token);

        // Initialize the client
        DataAPIClient client = new DataAPIClient(token, builder().withDestination(CASSANDRA).build());
        System.out.println("Connected to Data API");

       // Create a default keyspace
       ((DataAPIDatabaseAdmin) client
               .getDatabase(dataApiUrl)
               .getDatabaseAdmin()).createNamespace(keyspaceName, NamespaceOptions.simpleStrategy(1));
        System.out.println("Keyspace created ");

        Database db = client.getDatabase("http://localhost:8181", "default_keyspace");
        System.out.println("Connected to Database");

        // Create a collection. The default similarity metric is cosine.
        Collection<Document> collection = db.createCollection("simple", 5, COSINE);
        System.out.println("Created a Collection simple");

       // Create a collection with Vector embeddings OPEN AI
       Collection<Document> collectionLyrics =  db.createCollection("vector", CollectionOptions.builder()
                       .vectorSimilarity(SimilarityMetric.COSINE)
                       .vectorDimension(1536)
                       .vectorize("openai", "text-embedding-3-small")
                       .build(),
               new CommandOptions<>().embeddingAPIKey("sk-....."));
    }
}
```

### Running integration tests

The clients can target a distribution of Data API installed locally or Astra DB.
In case of Astra DB they can also target different environments (DEV, TEST, PROD).
When we run a test suite we can specify the target with the following properties:

| Property                        | Description                                                          |
|---------------------------------|----------------------------------------------------------------------|
| `ASTRA_DB_JAVA_TEST_ENV`        | `astra_dev`, `astra_test` or `astra_prod` (default)                  |
| `ASTRA_DB_APPLICATION_TOKEN`    | the token to use for authentication leverage an env var like `${var}` |
| `ASTRA_CLOUD_PROVIDER`          | `AWS`, `GCP` or  `AZURE`                                               |
| `ASTRA_CLOUD_REGION`            | A valid region is selected provider us-east-1, us-east-2|

> [!NOTE]
> No database is required, the test suite will create and delete databases as needed.
> To Know a valid region for a provider you can use the `[Astra CLI]`
>
> ```
> $ astra db list-regions-vector
> +-----------------+-------------------------+------------------------------------+
> | Cloud Provider  | Region                  | Full Name                          |
> +-----------------+-------------------------+------------------------------------+
> | aws             | ap-south-1              | Asia Pacific (Mumbai)              |
> | aws             | ap-southeast-2          | Asia Pacific (Sydney)              |
> | aws             | eu-west-1               | Europe (Ireland)                   |
> | aws             | us-east-1               | US East (N. Virginia)              |
> | aws             | us-east-2               | US East (Ohio)                     |
> | azure           | australiaeast           | Australia East                     |
> | azure           | centralindia            | Central India (Pune)               |
> | azure           | southcentralus          | South Central US                   |
> | azure           | westeurope              | West Europe                        |
> | azure           | westus3                 | US West 3                          |
> | gcp             | europe-west2            | West Europe2 (London, England, UK) |
> | gcp             | northamerica-northeast1 | Montreal, Quebec                   |
> | gcp (free-tier) | us-east1                | Moncks Corner, South Carolina      |
> | gcp             | us-east4                | Ashburn, Virginia                  |
> +-----------------+-------------------------+------------------------------------+
> ```

#### Prerequisites for testing

> [!IMPORTANT]
>
> - To run the maven command you need to build the project first:
>
>    ```
>    mvn clean install -Dtest.skipped=true
>    ```
>
> - You also need to position yourself in folder `astra-db-java`:
>
>    ```
>    cd astra-db-java
>    ```

#### Astra DB Admin test

Test operation to create/delete databases we need to use an Organization Administrator token.

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.astra_admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

#### DatabaseAdmin test

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.database_admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

#### Database test

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.database.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

#### Running multiple tests

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.astra_admin.AstraDBAdmin_01_DatabaseDDL_ITTest,com.datastax.astra.test.integration.astra.database_admin.DatabaseAdminITTest" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

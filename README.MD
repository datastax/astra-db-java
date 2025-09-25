
# Java Client for Data API

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.datastax.astra/astra-db-java)](https://search.maven.org/artifact/com.datastax.astra/astra-db-java)

This client library provides a simplified way to interact with Data API for AstraDB or local instances. For detailed documentation, each operation comes with a detailed description and examples.

- [`astra-db-ts`](https://github.com/datastax/astra-db-ts) is the equivalent for typescript
- [`astrapy`](https://github.com/datastax/astrapy) is the equivalent in python

This library is under development and is available in Maven Central.
You can build it locally and install it in your local repository.

## 📋 Table Of Content

1. [Installation](#1-installation)
   1. [Prerequisites](#11-prerequisites)
   2. [Packaging](#12-packaging)
2. [QuickStart with Astra DB](#2-quickstart-with-astra-db)
   1. [Sign up for Astra DB](#21-sign-up-for-astra-db)
   2. [Create a Database](#22-create-a-database)
   3. [Get your credentials](#23-get-your-credentials)
   4. [Create a new project and add the dependency](#24-create-a-new-project-and-add-the-dependency)
3. [QuickStart with Local Instances](#3-quickstart-with-local-instances)
4. [What's Next](#4-whats-next)
   
## 1. Installation

### 1.1 Prerequisites

#### 📦 Java Development Kit (JDK) 17
- Use the [reference documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) to install a **Java Development Kit**
- Validate your installation with

```bash
java --version
```

#### 📦 Apache Maven
- Use the [reference documentation](https://maven.apache.org/install.html) to install **Apache Maven**
- Validate your installation with

```bash
mvn -version
```

#### 📦 Docker (local Installation)

Docker is an open-source project that automates the deployment of software applications inside containers by providing an additional layer of abstraction and automation of OS-level virtualization on Linux.

### 1.2 Packaging

- Clone the repository

```console
git clone git@github.com:datastax/astra-db-java.git
```

- Build the project (java 11 and Maven is required)

> Note: You should skip the tests if you want to speed up the build, to run the test you need to have a bit of setup:
> - An environment variable `ASTRA_DB_APPLICATION_TOKEN` with your an Organization Administrator Astra token (PROD)
> - An environment variable `ASTRA_DB_APPLICATION_TOKEN_DEV` with your an Organization Administrator Astra token (DEV)
> - A running Data API locally with docker (see the `docker-compose.yml` in the root of the project)

```console
mvn clean install -Dtest.skipped=true
```

## 2. QuickStart with Astra DB

### 2.1. Sign up for Astra DB

- Access [https://astra.datastax.com](https://astra.datastax.com) and register with `Google` or `Github` account. It is free to use. There is free forever tiers of up to 25$ of consumption every month.

![](https://awesome-astra.github.io/docs/img/astra/astra-signin-github-0.png)

### 2.2. Create a Database

> If you are creating a new account, you will be brought to the DB-creation form directly.

- Get to the databases dashboard (by clicking on Databases in the left-hand navigation bar, expanding it if necessary), and click the `[Create Database]` button on the right.

![](https://datastaxdevs.github.io/langchain4j/langchain4j-1.png)

- **ℹ️ Fields Description**

| Field                                      | Description                                                                                                                                                                                                                                   |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Vector Database vs Serverless Database** | Choose `Vector Database` In june 2023, Cassandra introduced the support of vector search to enable Generative AI use cases.                                                                                                                   |
| **Database name**                          | It does not need to be unique, is not used to initialize a connection, and is only a label (keep it between 2 and 50 characters). It is recommended to have a database for each of your applications. The free tier is limited to 5 databases. |
| **Cloud Provider**                         | Choose whatever you like. Click a cloud provider logo, pick an Area in the list and finally pick a region. We recommend choosing a region that is closest to you to reduce latency. In free tier, there is very little difference.            |
| **Cloud Region**                           | Pick region close to you available for selected cloud provider and your plan.                                                                                                                                                                 |

If all fields are filled properly, clicking the "Create Database" button will start the process.

![](https://datastaxdevs.github.io/langchain4j/langchain4j-2.png)

It should take a couple of minutes for your database to become `Active`.

![](https://datastaxdevs.github.io/langchain4j/langchain4j-3.png)

### 2.3. Get your credentials

To connect to your database, you need the API Endpoint and a token. The api endpoint is available on the database screen, there is a little icon to copy the URL in your clipboard. (it should look like `https://<db-id>-<db-region>.apps.astra.datastax.com`).

![](https://datastaxdevs.github.io/langchain4j/langchain4j-4.png)

To get a token click the `[Generate Token]` button on the right. It will generate a token that you can copy to your clipboard.

### 2.4 Create a new project and add the dependency

Add the following dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>com.datastax.astra</groupId>
  <artifactId>astra-db-java</artifactId>
  <version>1.3.0</version>
</dependency>
```

Here is a sample class that demonstrates how to use the library:

```java
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.paging.FindIterable;

import java.util.List;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.vector.SimilarityMetric.cosine;

public class GettingStarted {
   public static void main(String[] args) {
      // Initializing client with a token
      DataAPIClient client = new DataAPIClient("my_token");

      // Accessing the Database through the HTTP endpoint
      Database db = client.getDatabase("http://db-region.apps.astra.datastax.com");

      // Create collection with vector support
      Collection<Document> col = db.createCollection("demo", 2, cosine);

      // Insert records
      col.insertMany(List.of(
              new Document("doc1").vector(new float[]{.1f, 0.2f}).append("key", "value1"),
              new Document().id("doc2").vector(new float[]{.2f, 0.4f}).append("hello", "world"),
              new Document("doc3").vector(new float[]{.5f, 0.6f}).append("key", "value1"))
      );

      // Search
      FindIterable<Document> docs = col.find(
              eq("key", "value1"), // metadata filter
              new float[]{.5f, .5f},              //vector
              10);                                 // maxRecord

      // Iterate and print your results
      for (Document doc : docs) System.out.println(doc);
   }
}
```

## 3. QuickStart with Local Instances

> Prequisite. You need HCD, DSE or CASSANDRA running on your machine and listening on `9042`. One good way is to run HCD as a docker image following the instruction [here](https://github.com/stargate/data-api/tree/main/docker-compose).

### 3.1 Start Data API

- Clone the repository
```
git clone git@github.com:stargate/data-api.git
```

- Access the folder and start the data API. Note that we position cassandra endpoint to `localhost` and datacenter to `dc1`.

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
JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager" \
mvn quarkus:dev -Dstargate.data-store.ignore-bridge=true -Dstargate.jsonapi.operations.vectorize-enabled=true -Dstargate.jsonapi.operations.database-config.local-datacenter=dc1 -Dquarkus.log.console.darken=2 -Dstargate.feature.flags.tables=true -Dstargate.jsonapi.operations.extend-error=true -Dstargate.feature.flags.reranking=true
```

- To check the Data API is running

| Field                                      | Description                                                                             |
|--------------------------------------------|-----------------------------------------------------------------------------------------|
| **Data API Spec**                          | http://localhost:8181/swagger-ui/#/                                                     |
| **Data API Endpoint**                      | http://localhost:8181                                                                   |
| **Token Header Key**                       | `Token`                                                                                      |
| **Token Header Value**               | `Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh` (aka `Cassandra:Base64(userName):Base64(password)`) |
| **Authentication API Spec (before 1.0.6)** | http://localhost:8081/swagger-ui/#/                                                     |

- The API will have 3 resources

| Field                 | Url                            | Description                                       |
|-----------------------|--------------------------------|---------------------------------------------------|
| **Namespace**         | `/v1/`                         | Interact with namespaces (not available in Astra) |
| **Data API Endpoint** | `/v1/{namespace}`              | Interact with collections of a namespace |
| **Token Header Key**  | `/v1/{namespace}/{collection}` |Interact with documents of a collection  |

- Create a keyspace called `default_keyspace`

```console
curl --location 'http://localhost:8181//v1' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data '{"createKeyspace":{"name":"default_keyspace"}}'
```

- Create a collection `person` with CURL (indexing)

```console
curl --location 'http://localhost:8181//v1/default_keyspace' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data '{
  "createCollection": {
    "name": "collection_person",
    "options": {
    "indexing": {
        "allow": [
          "firstname",
          "lastname"
        ]
      }
    }
  }
}'
```

- List my collection it should be there

```console
curl --location 'http://localhost:8181//v1/default_keyspace' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data '{
  "findCollections": {}
}'
```
- Inserts a few documents

```console
curl --location 'http://localhost:8181/v1/default_keyspace/collection_person' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data-raw '{
    "insertMany": {
        "documents": [
            {
                "_id": 1,
                "firstname": "Lucas",
                "lastname": "Hernandez",
                "age": 22
            },
            {
                "_id": 2,
                "firstname": "Antoine",
                "lastname": "Griezmann",
                "age": 25
            },
            {
                "_id": 3,
                "firstname": "N'\''Golo",
                "lastname": "Kanté",
                "age": 29
            },
            {
                "_id": 4,
                "firstname": "Tanguy",
                "lastname": "Ndombele",
                "age": 24
            },
            {
                "_id": 5,
                "firstname": "Raphaël",
                "lastname": "Varane",
                "age": 10
            },
            {
                "_id": 6,
                "firstname": "Hugo",
                "lastname": "Lloris",
                "age": 41
            },
            {
                "_id": 7,
                "firstname": "Olivier",
                "lastname": "Giroud",
                "age": 36
            }
        ]
    }
}'

```

- Search on Valid property

```console
curl --location 'http://localhost:8181/v1/default_keyspace/collection_person' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data-raw '{
    "find": {
         "filter": {
           "lastname": "Varane"
        },
        "projection": {
            "lastname": true,
            "firstname": true
        },
        "sort": {"lastname":-1}
    }
}'
```

- Search on inValid property (exception expected)

```console
curl --location 'http://localhost:8181/v1/default_keyspace/collection_person' \
--header 'token: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh' \
--header 'Content-Type: application/json' \
--data-raw '{
    "find": {
         "filter": {
           "age": 24
        },
        "projection": {
            "lastname": true,
            "firstname": true
        },
        "sort": {"lastname":-1}
    }
}'
```


### Using Java client with Local Instance

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

## 4. Running Integration tests

### 4.1 ASTRA DB

The clients can target a distribution of Data API installed locally or Astra DB. 
In case of Astra DB they can also target different environments (DEV, TEST, PROD).
When we run a test suite we can specify the target with the following properties:


| Property                        | Description                                                                 |
|---------------------------------|-----------------------------------------------------------------------------|
| `ASTRA_DB_JAVA_TEST_ENV`        | local, hcd, astra_dev, astra_prod                                          |
| `ASTRA_DB_APPLICATION_TOKEN`    | the token to use for authentication                                        |
| `ASTRA_CLOUD_PROVIDER`          | AWS, GCP, AZURE                                                            |
| `ASTRA_CLOUD_REGION`            | us-east-1, us-east-2

> [!IMPORTANT]
> No database is required, the test suite will create and delete databases as needed.

### 4.2 HCD

- Values for TEST_ENV are:
  `local`, `astra_dev`, `astra_test`, `astra_prod`

- Create a DB `astra_gcp_us_east1` in a environment PROD

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

- Will Create db `astra_aws_us_east_2` in a environment PROD

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=AWS \
 -DASTRA_CLOUD_REGION=us-east-2  \
 -Dtest.skipped=false
```








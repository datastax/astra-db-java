# Contributing

When contributing to this repository, please first discuss the change you wish to make via issue,
email, or any other method with the owners of this repository before making a change.

Please note we have a [Code of Conduct](CODE_OF_CONDUCT.md), please follow it in all your interactions with the project.

## <a name="issue"></a> Found an Issue?
If you find a bug in the source code or a mistake in the documentation, you can help us by
[submitting an issue](#submit-issue) to the GitHub Repository. Even better, you can
[submit a Pull Request](#submit-pr) with a fix.

## <a name="feature"></a> Want a Feature?
You can *request* a new feature by [submitting an issue](#submit-issue) to the GitHub
Repository. If you would like to *implement* a new feature, please submit an issue with
a proposal for your work first, to be sure that we can use it.

* **Small Features** can be crafted and directly [submitted as a Pull Request](#submit-pr).

## <a name="submit"></a> Contribution Guidelines

### <a name="submit-issue"></a> Submitting an Issue
Before you submit an issue, search the archive, maybe your question was already answered.

If your issue appears to be a bug, and hasn't been reported, open a new issue.
Help us to maximize the effort we can spend fixing issues and adding new
features, by not reporting duplicate issues.  Providing the following information will increase the
chances of your issue being dealt with quickly:

* **Overview of the Issue** - if an error is being thrown a non-minified stack trace helps
* **Motivation for or Use Case** - explain what are you trying to do and why the current behavior is a bug for you
* **Reproduce the Error** - provide a live example or a unambiguous set of steps
* **Suggest a Fix** - if you can't fix the bug yourself, perhaps you can point to what might be
  causing the problem (line of code or commit)

### <a name="submit-pr"></a> Submitting a Pull Request (PR)
Before you submit your Pull Request (PR) consider the following guidelines:

* Search the repository (https://github.com/stargate/stargate-sdk-java/pulls) for an open or closed PR that relates to your submission. You don't want to duplicate effort.

* Create a fork of the repo
	* Navigate to the repo you want to fork
	* In the top right corner of the page click **Fork**:
	![](https://help.github.com/assets/images/help/repository/fork_button.jpg)

* Make your changes in the forked repo
* Commit your changes using a descriptive commit message
* In GitHub, create a pull request: https://help.github.com/en/articles/creating-a-pull-request-from-a-fork
* If we suggest changes then:
  * Make the required updates.
  * Rebase your fork and force push to your GitHub repository (this will update your Pull Request):

    ```shell
    git rebase master -i
    git push -f
    ```

That's it! Thank you for your contribution!

## Developer tips

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

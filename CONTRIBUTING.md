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

* Search the repository (https://github.com/datastax/astra-db-java/pulls) for an open or closed PR that relates to your submission. You don't want to duplicate effort.

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

## Developer Setup

### Prerequisites

#### Java Development Kit (JDK) 17+

- Use the [reference documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) to install a **Java Development Kit**
- Validate your installation with:

   ```bash
   java --version
   ```

#### Apache Maven 3.6.3+

- Use the [reference documentation](https://maven.apache.org/install.html) to install **Apache Maven**
- Validate your installation with:

   ```bash
   mvn -version
   ```

#### Docker (for local testing)

Docker is required to run a local HCD/DSE instance for integration tests.

### Building the Project

Clone the repository and build:

```bash
git clone git@github.com:datastax/astra-db-java.git
cd astra-db-java
mvn clean install -DskipTests
```

## Running Tests

### Test Configuration System

Tests are configured through a layered properties system. Values are resolved in priority order:

1. **Environment variables** (e.g., `ASTRA_DB_APPLICATION_TOKEN`)
2. **System properties** (e.g., `-Dastra.token=...`)
3. **Config files** (loaded in order, later files override earlier ones):
   - `test-config.properties` — defaults (committed)
   - `test-config-local.properties` — local HCD/DSE settings (committed)
   - `test-config-astra.properties` — Astra credentials (**gitignored**)
   - `test-config-embedding-providers.properties` — embedding API keys (**gitignored**)

#### Setting Up Config Files

For **Astra** testing, copy the template and fill in your credentials:

```bash
cp astra-db-java/src/test/resources/test-config-astra.properties.template \
   astra-db-java/src/test/resources/test-config-astra.properties
```

Then edit `test-config-astra.properties`:

```properties
test.environment=astra_prod
astra.token=AstraCS:YOUR_TOKEN_HERE
astra.cloud.provider=AWS
astra.cloud.region=us-east-2
```

For **embedding provider** tests (vectorize), copy and fill in API keys:

```bash
cp astra-db-java/src/test/resources/test-config-embedding-providers.properties.template \
   astra-db-java/src/test/resources/test-config-embedding-providers.properties
```

```properties
openai.api.key=sk-...
cohere.api.key=...
mistral.api.key=...
```

#### Overriding a Single Property

You can override any property without editing config files:

```bash
# Via environment variable
export ASTRA_DB_APPLICATION_TOKEN=AstraCS:...
mvn clean test -pl astra-db-java -Pastra-prod

# Via system property
mvn clean test -pl astra-db-java -Pastra-prod -Dastra.cloud.region=eu-west-1

# Override the test environment
mvn clean test -pl astra-db-java -DASTRA_DB_JAVA_TEST_ENV=astra_dev
```

### Maven Commands

```bash
# Run all tests against Astra PROD (with coverage report)
mvn clean verify -pl astra-db-java -Pastra-prod

# Run all tests against Astra DEV (with coverage report)
mvn clean verify -pl astra-db-java -Pastra-dev

# Run all tests against local HCD/DSE (with coverage report)
mvn clean verify -pl astra-db-java -Plocal

# Run tests and generate JaCoCo report explicitly
mvn clean test jacoco:report -pl astra-db-java -Pastra-prod

# Run a specific test class
mvn test -pl astra-db-java -Pastra-prod -Dtest="Astra_Collections_01_IT"

# Build without tests
mvn clean install -DskipTests

# Skip all tests via profile
mvn clean install -pl astra-db-java -Pskip-tests
```

The JaCoCo coverage report is generated at `astra-db-java/target/site/jacoco/index.html`.

### Maven Profiles

| Profile | `test.environment` | Cloud Provider | Region | Description |
|---------|--------------------|----------------|--------|-------------|
| `local` | `local` | GCP | us-east1 | Local HCD/DSE instance |
| `astra-dev` | `astra_dev` | GCP | us-central1 | Astra development environment |
| `astra-prod` | `astra_prod` | AWS | us-east-2 | Astra production environment |
| `skip-tests` | - | - | - | Skip all tests |

Profile values are passed as system properties, which take priority over config file values but not environment variables.

### Running Tests in the IDE

Tests work out of the box in IDEs (IntelliJ, Eclipse) with no environment variables required:

1. **Astra tests:** Create `test-config-astra.properties` from the template with your token and region. Right-click any `Astra_*IT` test class and run.
2. **Local tests:** Start HCD with `docker-compose up -d`, then right-click any `Local_*IT` test class and run. Tests auto-skip if local HCD is not available.

### Test Annotations

Test classes use JUnit 5 conditional annotations to run only in the appropriate environment:

| Annotation | Behavior |
|------------|----------|
| `@EnabledIfAstra` | Runs only when `test.environment` is `astra_dev`, `astra_prod`, or `astra_test` **and** a valid Astra token is available. Skipped otherwise. |
| `@EnabledIfLocalAvailable` | Runs only when a local HCD/DSE instance is reachable at the configured endpoint. Skipped otherwise. |

This means you can safely run the entire test suite: tests for unavailable environments are **skipped** (not failed).

### Local HCD Setup

#### Prerequisites

- Docker and Docker Compose
- Java 17+
- Maven
- (Optional) A local clone of the [Data API](https://github.com/stargate/data-api) repository

#### 1. Start HCD

Using the `docker-compose.yml` included in this repository:

```bash
docker-compose up -d
```

Or, if you have the Data API repository cloned:

```bash
export DATA_API_FOLDER=/path/to/data-api
cd $DATA_API_FOLDER/docker-compose && ./start_hcd.sh -d
```

#### 2. Start the Data API

Clone and start the Data API from source:

```bash
git clone git@github.com:stargate/data-api.git
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
mvn quarkus:dev \
  -Dstargate.data-store.ignore-bridge=true \
  -Dstargate.jsonapi.operations.vectorize-enabled=true \
  -Dstargate.jsonapi.operations.database-config.local-datacenter=dc1 \
  -Dquarkus.log.console.darken=2 \
  -Dstargate.feature.flags.tables=true \
  -Dstargate.jsonapi.operations.extend-error=true \
  -Dstargate.feature.flags.reranking=true
```

#### 3. Verify the Data API is running

| Field | Value |
|-------|-------|
| **API Spec** | http://localhost:8181/swagger-ui/#/ |
| **Endpoint** | http://localhost:8181 |
| **Token Header Key** | `Token` |
| **Token Header Value** | `Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh` (`Cassandra:Base64(username):Base64(password)`) |

#### 4. Access CQL shell (optional)

Connect to the running HCD instance with `cqlsh`:

```bash
# Docker
docker run -it --rm --network container:$(docker ps | grep hcd | cut -b 1-12) \
  cassandra:latest cqlsh -u cassandra -p cassandra

# Podman
podman exec -it $(podman ps | grep hcd | cut -b 1-12) \
  cqlsh -u cassandra -p cassandra
```

## Release

```bash
# Prepare + perform release (core + langchain4j modules)
mvn -pl astra-db-java,langchain4j-astradb -am release:prepare -DskipTests=true
mvn -pl astra-db-java,langchain4j-astradb -am release:perform -DskipTests=true
```

Artifacts are published to Maven Central via Sonatype Central Publishing with GPG signing.

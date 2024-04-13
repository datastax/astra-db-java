
# Java Client for Data API

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.datastax.astra/astra-db-java)](https://search.maven.org/artifact/com.datastax.astra/astra-db-java)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=clun_db-java)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=clun_astra-db-java)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-db-java)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=bugs)](https://sonarcloud.io/summary/new_code?id=clun_astra-db-java)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=clun_astra-db-java)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=clun_astra-db-java)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=clun_astra-db-java)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-db-java&metric=coverage)](https://sonarcloud.io/summary/new_code?id=clun_astra-db-java)


This client library provides a simplified way to interact with Data API for AstraDB or local instances. For detailed documentation, each operation comes with a detailed description and examples.

- [`astra-db-ts`](https://github.com/datastax/astra-db-ts) is the equivalent for typescript
- [`astrapy`](https://github.com/datastax/astrapy) is the equivalent in python

This library is under development and is available in Maven Central.
You can build it locally and install it in your local repository.

## 1. Local Installation

### 1.1 Prerequisites

#### 📦 Java Development Kit (JDK) 11
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

### 1.3 Installation

Add the following dependency to your `pom.xml` file:

```xml

<dependency>
  <groupId>com.datastax.astra</groupId>
  <artifactId>astra-db-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 2. QuickStart

After creating a new java Project and adding the dependency to your `pom.xml` file, you can start using the library.

Here is a sample class that demonstrates how to use the library:

```java
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindIterable;
import java.util.List;
import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.SimilarityMetric.cosine;

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
    new float[] {.5f, .5f},              //vector
    10);                                 // maxRecord

  // Iterate and print your results
  for (Document doc : docs) System.out.println(doc);
 }
}
```

### 3. What's Next

This is an the organization of the different classes of the framework.

![overview](https://datastaxdevs.github.io/astra-db-java/overview.png)

- For more information use the [JAVADOC  documentation](https://datastaxdevs.github.io/astra-db-java/latest/)

- The `examples` directory contains more examples on how to use the library.
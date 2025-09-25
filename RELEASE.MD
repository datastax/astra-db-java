
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This page is mean to help developer to release this the SDK.

## CQLSH

```console
# Get the latest compose with DSE
curl -o docker-compose-dse.yml https://raw.githubusercontent.com/datastax/astra-db-java/refs/heads/main/docker-compose.yml
# Start docker
docker-compose -f ./docker-compose-dse.yml up -d   
```
```
docker run -it --rm --network container:$(docker ps | grep dse | cut -b 1-12) cassandra:latest cqlsh -u cassandra -p cassandra
```

## Run the Test

### Prerequisites

To run the tests you need 3 different environments: Dev, PROD and a local installation. For Astra DEV and production you should have the following environment variables sets: `ASTRA_DB_APPLICATION_TOKEN_DEV` and `ASTRA_DB_APPLICATION_TOKEN`.

- Generate Jacoco Report
```json
mvn clean test jacoco:report 
```

- Push to SONAR
```json
mvn clean test jacoco:report  verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-db-java
```

- [x] Start the `ssh-agent`

```console
eval "$(ssh-agent -s)"
```
- [x] Add the ssh key to the agent

```console
cd ~/.ssh
ssh-add githubff4j
```

- [x] Deploy

```console
mvn release:prepare -DskipTests=true -Dmaven.test.skip=true
mvn release:perform -DskipTests=true -Dmaven.test.skip=true
```



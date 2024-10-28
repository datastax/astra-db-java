
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


```sql
  insert into default_keyspace.table_all_returns (
    p_ascii,
    p_bigint,
    p_blob,
    p_boolean,
    p_date,
    p_decimal,
    p_double,
    p_double_minf,
    p_double_pinf,
    p_duration,
    p_float,
    p_float_nan,
    p_inet,
    p_int,
    p_smallint,
    p_text,
    p_text_nulled,
    p_time,
    p_timestamp,
    p_tinyint,
    p_uuid,
    p_varint,
    p_vector,
    p_list_int,
    // p_map_text_int,
    p_set_int
) values (
             'abc',
             10000,
             0xabcdef0123456789,
             true,
             '2015-05-03',
             123.45,
             987.6543,
             -Infinity,
             Infinity,
             12h48m,
             66.55,
             NaN,
             '12.34.56.78',
             2024,
             200,
             'måndag',
             null,
             '13:30:54.234',
             '2015-05-03 13:30:54.234',
             104,
             9c5b94b1-35ad-49bb-b118-8e8fc24abf80,
             123456789,
             [0.1, 0.2, 0.3],
             [4, 17, 34],
             // {'a': 101, 'b': 102},
             {9, 81}
         );
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




### Testing Clients


#### Astra DB ADMIN

- Values for TEST_ENV are:
`local`, `astra_dev`, `astra_test`, `astra_prod`

- Create a DB `astra_gcp_us_east1` in a environment PROD

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.astra_admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=GCP \
 -DASTRA_CLOUD_REGION=us-east1 \
 -Dtest.skipped=false
```

- Will Create db `astra_aws_us_east_2` in a environment PROD

```
mvn test \
 -Dtest="com.datastax.astra.test.integration.astra.astra_admin.*Test" \
 -DASTRA_DB_JAVA_TEST_ENV=astra_prod \
 -DASTRA_DB_APPLICATION_TOKEN=${ASTRA_DB_APPLICATION_TOKEN} \
 -DASTRA_CLOUD_PROVIDER=AWS \
 -DASTRA_CLOUD_REGION=us-east-2  \
 -Dtest.skipped=false
```







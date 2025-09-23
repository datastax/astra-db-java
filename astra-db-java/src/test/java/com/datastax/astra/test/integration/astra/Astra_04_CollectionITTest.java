package com.datastax.astra.test.integration.astra;

import com.datastax.astra.test.integration.AbstractCollectionITTest;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_ASTRA_TOKEN;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_CLOUD_PROVIDER;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_CLOUD_REGION;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_DESTINATION;

//@EnabledIfSystemProperty(named = ENV_VAR_ASTRA_TOKEN,     matches = ".*")
//@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_PROVIDER,  matches = ".*")
//@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_REGION,    matches = ".*")
//@EnabledIfSystemProperty(named = ENV_VAR_DESTINATION,     matches = "astra_prod")
class Astra_04_CollectionITTest extends AbstractCollectionITTest {}

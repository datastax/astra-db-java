package com.datastax.astra.test.astra_admin;

import com.datastax.astra.test.AbstractAstraDBAdminTest;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.datastax.astra.test.AbstractDataAPITest.ENV_VAR_ASTRA_TOKEN;
import static com.datastax.astra.test.AbstractDataAPITest.ENV_VAR_CLOUD_PROVIDER;
import static com.datastax.astra.test.AbstractDataAPITest.ENV_VAR_CLOUD_REGION;
import static com.datastax.astra.test.AbstractDataAPITest.ENV_VAR_DESTINATION;

@EnabledIfSystemProperty(named = ENV_VAR_ASTRA_TOKEN,     matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_PROVIDER,  matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_REGION,    matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_DESTINATION, matches = "astra_(dev|prod|test)")
public class AstraDBAdmin_01_DatabaseDDL_ITTest extends AbstractAstraDBAdminTest {}
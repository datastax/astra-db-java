package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.AbstractDevopsApiTest;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.pcu.domain.PCUCapacityWorkloadType;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUInstanceType;
import com.dtsx.astra.sdk.pcu.domain.PCUProvisionType;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import com.dtsx.astra.sdk.pcu.domain.PCUTypeLocationFilter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.dtsx.astra.sdk.utils.TestUtils;
import com.dtsx.astra.sdk.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

// Unpredictable so re;oving fro; automated tests
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PCUGroupClientDevTest extends AbstractDevopsApiTest {

    public static final String            DEV_REGION   = "us-west-2";
    public static final CloudProviderType DEV_PROVIDER = CloudProviderType.AWS;

    public static final String DEV_TOKEN = Utils
            .readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
            .orElseThrow(() -> new IllegalStateException("Please set env var 'ASTRA_DB_APPLICATION_TOKEN_DEV' with a dev token"));

    protected static PCUGroupsOpsClient PCUGroupsOpsClient;

    @BeforeAll
    public static void beforeAll() {
        apiDevopsClient = new AstraOpsClient(DEV_TOKEN, AstraEnvironment.DEV);
        PCUGroupsOpsClient = apiDevopsClient.pcus();
    }

    @Test
    @Order(1)
    public void shouldAccessOrganizationInDev() {
        Organization org = getApiDevopsClient().getOrganization();
        Assertions.assertNotNull(org);
        Assertions.assertNotNull(org.getId());
        Assertions.assertNotNull(org.getName());
        System.out.println("[test-pcu] - You are connected to organization: " + org.getName() + "(" + org.getId() + ")");
    }

    @Test
    @Order(2)
    public void shouldListPcuGroupsType() {
        // Full List
        System.out.println("[test-pcu] - Listing PCU Types (no filters)");
        Stream<PCUType> types = PCUGroupsOpsClient.listPcuTypes();
        System.out.println("[test-pcu] - Items " + types.toList().size());

        // Filtered by DC
        PCUTypeLocationFilter location = new PCUTypeLocationFilter(DEV_PROVIDER.getCode().toLowerCase(), DEV_REGION);
        System.out.println("[test-pcu] - Listing PCU Types on " + JsonUtils.marshall(location));
        List<PCUType> filtered = getApiDevopsClient().pcus().listPcuTypes(location).toList();
        Assertions.assertNotNull(filtered);
        System.out.println("[test-pcu] - Items " + filtered.size());
        System.out.println("[test-pcu] - Items " +  JsonUtils.marshall(filtered));
        Assertions.assertTrue(filtered.stream().anyMatch(pcu -> pcu.getType().equals("small")));
    }

    @Test
    @Order(2)
    public void shouldListPcuGroups() {
        List<PCUGroup> allGroups = PCUGroupsOpsClient.findAll().toList();
        System.out.println("[test-pcu] - Listing pcu groups " + JsonUtils.marshall(allGroups));
        System.out.println("[test-pcu] - Items " + allGroups.size());
    }

    @Test
    @Order(3)
    public void shouldCreatePcuGroupStandard() {

        UUID pcuGroupStandardId = null;
        try {
            PCUGroupCreationRequest createPcu = PCUGroupCreationRequest
                    .builder()
                    .title("pcu_group_from_java")
                    .description("my first PCU group")
                    .instanceType("standard")
                    .cloudProvider(CloudProviderType.AWS)
                    .region(DEV_REGION)
                    .workloadType(PCUCapacityWorkloadType.flexible.name())
                    .provisionType(PCUProvisionType.shared.name())
                    .reserved(1)
                    .min(1)
                    .max(1)
                    .build();

            // Creating standard pcu Group
            PCUGroup group = PCUGroupsOpsClient.create(createPcu);
            Assertions.assertNotNull(group);
            pcuGroupStandardId = pcuGroupStandardId = group.getId();
            System.out.println("[test-pcu] - Created PCU group: " + group.getId());
            Assertions.assertTrue(getApiDevopsClient().pcus().findById(group.getId()).isPresent());

            // Create DB
            DatabaseCreationRequest dbCreation = DatabaseCreationRequest
                    .builder()
                    .name("vector_db_in_standard_pcu")
                    .keyspace(SDK_TEST_KEYSPACE)
                    .cloudProvider(CloudProviderType.AWS)
                    .cloudRegion(DEV_REGION)
                    .withVector()
                    .assignToPCUGroup(pcuGroupStandardId)
                    .build();

            String dbId = getApiDevopsClient().db().create(dbCreation);
            TestUtils.waitForDbStatus(getDatabasesClient().database(dbId), DatabaseStatusType.ACTIVE, 500);
            Assertions.assertTrue(getDatabasesClient().findById(dbId).isPresent());
            Assertions.assertNotNull(getDatabasesClient().database(dbId).get());
            Assertions.assertTrue(getDatabasesClient().findByName("vector_db_in_standard_pcu").findAny().isPresent());

        } catch (RuntimeException e) {

        } finally {
            if (pcuGroupStandardId != null) {
                PCUGroupsOpsClient.group(pcuGroupStandardId).delete();
            }
        }
    }

    @Test
    @Order(2)
    public void shouldCreateMiniPcu() {
        PCUGroupCreationRequest createPcu = PCUGroupCreationRequest
                .builder()
                .title("java_client_mini_pcu")
                .description("java_client_mini_pcu")
                .instanceType(PCUInstanceType.small.name())
                .cloudProvider(CloudProviderType.AWS)
                .region(DEV_REGION)
                .workloadType(PCUCapacityWorkloadType.flexible.name())
                .provisionType(PCUProvisionType.shared.name())
                .reserved(1)
                .min(1)
                .max(1)
                .build();
        PCUGroup group = getApiDevopsClient().pcus().create(createPcu);
        System.out.println(group);
    }

    @Test
    @Order(2)
    public void shouldDeletePcuGroup() {
        PCUGroupsOpsClient.group(UUID.fromString("0c1f67a8-1fd6-4f52-ae2c-ea1483718a11")).delete();
    }

    @Test
    @Order(3)
    public void should_create_db_and_assign_pcu() throws InterruptedException {
        UUID miniPcuUUID = UUID.fromString("57dde257-86c6-4646-9247-670cd8a4d360");
        DatabaseCreationRequest dbCreation = DatabaseCreationRequest
                .builder()
                .name("vector_db_in_mini_pcu")
                .keyspace(SDK_TEST_KEYSPACE)
                .cloudProvider(CloudProviderType.AWS)
                .cloudRegion(DEV_REGION)
                .withVector()
                .assignToPCUGroup(miniPcuUUID)
                .build();
        String dbId = getApiDevopsClient().db().create(dbCreation);
        Thread.sleep(10000);
        Assertions.assertTrue(getDatabasesClient().findById(dbId).isPresent());
        Assertions.assertNotNull(getDatabasesClient().database(dbId).get());
        Assertions.assertTrue(getDatabasesClient().findByName("vector_db_in_mini_pcu").count() > 0);
        // When
        TestUtils.waitForDbStatus(getDatabasesClient().database(dbId), DatabaseStatusType.ACTIVE, 500);
    }
   
}

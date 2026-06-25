package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.AbstractDevopsApiTest;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.pcu.domain.PcuCapacityWorkloadType;
import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PcuInstanceType;
import com.dtsx.astra.sdk.pcu.domain.PcuProvisionType;
import com.dtsx.astra.sdk.pcu.domain.PcuType;
import com.dtsx.astra.sdk.pcu.domain.PcuTypeLocationFilter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.dtsx.astra.sdk.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PCUGroupClientTest extends AbstractDevopsApiTest {

    public static final String TOKEN = "change_me";
    public static final String TEST_REGION = "us-west-2";

    @BeforeAll
    public static void beforeAll() {
        apiDevopsClient = new AstraOpsClient(TOKEN, AstraEnvironment.DEV);
    }

    @Test
    @Order(1)
    public void shouldAccessOrganizationInDev() {
        Organization org = getApiDevopsClient().getOrganization();
        Assertions.assertNotNull(org);
        Assertions.assertNotNull(org.getId());
        Assertions.assertNotNull(org.getName());
        Assertions.assertTrue(org .getName().startsWith("Data API"));
    }

    @Test
    @Order(2)
    public void shouldListPcuGroupsType() {
        PcuTypeLocationFilter location = new PcuTypeLocationFilter(CloudProviderType.AWS.getCode().toLowerCase(), TEST_REGION);
        Stream<PcuType> types = getApiDevopsClient().pcus().listPcuTypes(location);
        Assertions.assertNotNull(types);
        System.out.println(JsonUtils.marshall(types.toList()));
    }

    @Test
    @Order(2)
    public void shouldListPcuGroups() {
        Stream<PcuGroup> groups = getApiDevopsClient().pcus().findAll();
        System.out.println(groups.toList());
    }

    @Test
    @Order(2)
    public void shouldCreatePcuGroup() {
        PcuGroupCreationRequest createPcu = PcuGroupCreationRequest
                .builder()
                .title("pcu_group_from_java")
                .description("my first PCU group")
                .instanceType("standard")
                .cloudProvider(CloudProviderType.AWS)
                .region(TEST_REGION)
                .workloadType(PcuCapacityWorkloadType.FLEXIBLE)
                .provisionType(PcuProvisionType.SHARED)
                .reserved(1)
                .min(1)
                .max(1)
                .build();

        PcuGroup group = getApiDevopsClient().pcus().create(createPcu);
        System.out.println(group);
    }

    @Test
    @Order(2)
    public void shouldCreateMiniPcu() {
        PcuGroupCreationRequest createPcu = PcuGroupCreationRequest
                .builder()
                .title("java_client_mini_pcu")
                .description("java_client_mini_pcu")
                .instanceType(PcuInstanceType.SMALL.getCode())
                .cloudProvider(CloudProviderType.AWS)
                .region(TEST_REGION)
                .workloadType(PcuCapacityWorkloadType.COMMITED)
                .provisionType(PcuProvisionType.SHARED)
                .reserved(1)
                .min(1)
                .max(1)
                .build();
        PcuGroup group = getApiDevopsClient().pcus().create(createPcu);
        System.out.println(group);
    }

    @Test
    @Order(2)
    public void shouldDeletePcuGroup() {
        getApiDevopsClient().pcus().group("57dde257-86c6-4646-9247-670cd8a4d360").delete();
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
                .cloudRegion(TEST_REGION)
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

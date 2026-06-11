package com.datastax.astra.client.admin.definition;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationBuilder;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DatabaseDefinition {

    /** Default region. **/
    public static final String DEFAULT_REGION  = "us-east1";

    /** Default tier. **/
    public static final String DEFAULT_TIER    = "serverless";

    /** Default cloud. **/
    public static final CloudProviderType DEFAULT_CLOUD = CloudProviderType.GCP;

    /** CloudProvider where the database lives. */
    private CloudProviderType cloudProvider = DEFAULT_CLOUD;

    /** Region. */
    private String region = DEFAULT_REGION;

    /** Database type. */
    private String tier = DEFAULT_TIER;

    /** Name of the database--user friendly identifier. */
    private String name;

    /** Keyspace name in database */
    private String keyspace;

    /**
     * CapacityUnits is the amount of space available (horizontal scaling)
     * for the database. For free tier the max CU's is 1, and 100
     * for CXX/DXX the max is 12 on startup.
     */
    private Integer capacity = 1;

    /**
     * Default is null, if vector will be added
     */
    private DatabaseCreationType dbType;

    /**
     * Identifier to assign a database to a PCU group directly.
     */
    private UUID pcuGroupID;

    /**
     * Projection as the creation request
     *
     * @return
     *  db creation request
     */
    public DatabaseCreationRequest asRequest() {
        DatabaseCreationBuilder builder = DatabaseCreationRequest.builder();
        builder.capacityUnit(capacity);
        builder.name(name);
        builder.cloudProvider(cloudProvider);
        builder.cloudRegion(region);
        builder.tier(tier);
        builder.keyspace(keyspace);
        builder.withVector();
        builder.dbType(dbType);
        if (pcuGroupID != null) {
            builder.assignToPCUGroup(pcuGroupID);
        }
        return builder.build();
    }
}

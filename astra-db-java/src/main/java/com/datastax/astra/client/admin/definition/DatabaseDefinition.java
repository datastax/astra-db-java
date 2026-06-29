package com.datastax.astra.client.admin.definition;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2026 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    private Integer capacityUnits = 1;

    /**
     * Default is null, if vector will be added
     */
    private DatabaseCreationType dbType;

    /**
     * Identifier to assign a database to a PCU group directly.
     */
    private UUID PCUGroupId;

    /**
     * Projection as the creation request
     *
     * @return
     *  db creation request
     */
    public DatabaseCreationRequest asRequest() {
        DatabaseCreationBuilder builder = DatabaseCreationRequest.builder();
        builder.capacityUnit(capacityUnits);
        builder.name(name);
        builder.cloudProvider(cloudProvider);
        builder.cloudRegion(region);
        builder.tier(tier);
        builder.keyspace(keyspace);
        builder.withVector();
        builder.dbType(dbType);
        if (PCUGroupId != null) {
            builder.assignToPCUGroup(PCUGroupId);
        }
        return builder.build();
    }
}

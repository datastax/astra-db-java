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
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PCUGroupDefinition {

    /**
     * Unique identifier for the PCU group.
     */
    private UUID id;

    /**
     * Associated PCU Types
     */
    private PCUTypeDefinition pcuType;

    @JsonProperty("name")
    private String name;

    /**
     * Organization identifier.
     */
    private String orgId;

    /**
     * Human-readable title for the PCU group.
     */
    private String title;

    /**
     * Description of the PCU group.
     */
    private String description;

    /**
     * Cloud provider where the PCU group is deployed.
     */
    private CloudProviderType cloudProvider;

    /**
     * Cloud region where the PCU group is deployed.
     */
    private String region;

    /**
     * Instance type for the PCU group.
     */
    private String instanceType;

    /**
     * Provisioning type (e.g., on-demand, reserved).
     */
    private String provisionType;

    /**
     * Minimum number of PCUs.
     */
    private int min;

    /**
     * Maximum number of PCUs.
     */
    private int max;

    /**
     * Number of reserved PCUs.
     */
    private int reserved;

    /**
     * Timestamp when the PCU group was created.
     */
    private Instant createdAt;

    /**
     * Timestamp when the PCU group was last updated.
     */
    private Instant updatedAt;

    /**
     * User identifier who created the PCU group.
     * This is an Astra user identifier, not a standard UUID.
     */
    private String createdBy;

    /**
     * User identifier who last updated the PCU group.
     * This is an Astra user identifier, not a standard UUID.
     */
    private String updatedBy;

    /**
     * Current status of the PCU group.
     */
    private String status;

    /**
     * Constructor to map from PCUGroup (devops API) to PCUGroupDefinition.
     *
     * @param devopsPCUGroup the PCUGroup from devops API
     */
    public PCUGroupDefinition(PCUGroup devopsPCUGroup) {
        if (devopsPCUGroup != null) {
            this.id = devopsPCUGroup.getId();
            this.name = devopsPCUGroup.getName();
            this.orgId = devopsPCUGroup.getOrgId();
            this.title = devopsPCUGroup.getTitle();
            this.description = devopsPCUGroup.getDescription();
            this.cloudProvider = devopsPCUGroup.getCloudProvider();
            this.region = devopsPCUGroup.getRegion();
            this.instanceType = devopsPCUGroup.getInstanceType();
            this.provisionType = devopsPCUGroup.getProvisionType();
            this.min = devopsPCUGroup.getMin();
            this.max = devopsPCUGroup.getMax();
            this.reserved = devopsPCUGroup.getReserved();
            this.createdAt = devopsPCUGroup.getCreatedAt();
            this.updatedAt = devopsPCUGroup.getUpdatedAt();
            this.createdBy = devopsPCUGroup.getCreatedBy();
            this.updatedBy = devopsPCUGroup.getUpdatedBy();
            this.status = devopsPCUGroup.getStatus();
            
            // Map PCUType to PCUTypeDefinition
            if (devopsPCUGroup.getPcuType() != null) {
                this.pcuType = new PCUTypeDefinition(devopsPCUGroup.getPcuType());
            }
        }
    }

}

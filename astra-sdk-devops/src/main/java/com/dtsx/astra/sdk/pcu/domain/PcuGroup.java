package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a PCU (Processing Capacity Units) Group in Astra.
 * A PCU group manages compute resources for databases across cloud providers and regions.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroup {
    /**
     * Unique identifier for the PCU group.
     */
    @JsonProperty("uuid")
    private String id;
    
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
    private PcuProvisionType provisionType;

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
    private String createdAt;
    
    /**
     * Timestamp when the PCU group was last updated.
     */
    private String updatedAt;
    
    /**
     * User who created the PCU group.
     */
    private String createdBy;
    
    /**
     * User who last updated the PCU group.
     */
    private String updatedBy;

    /**
     * Current status of the PCU group.
     */
    private PcuGroupStatusType status;
}

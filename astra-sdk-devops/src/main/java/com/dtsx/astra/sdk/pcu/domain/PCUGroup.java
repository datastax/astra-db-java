package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a PCU (Processing Capacity Units) Group in Astra.
 * A PCU group manages compute resources for databases across cloud providers and regions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PCUGroup {

    /**
     * Unique identifier for the PCU group.
     */
    @JsonProperty("uuid")
    private UUID id;

    /**
     * Associated PCU Types
     */
    @JsonProperty("pcuType")
    private PCUType pcuType;

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
     * Default constructor.
     */
    public PCUGroup() {
    }

    /**
     * Gets id
     *
     * @return value of id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set value for id
     *
     * @param id new value for id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets pcuType
     *
     * @return value of pcuType
     */
    public PCUType getPcuType() {
        return pcuType;
    }

    /**
     * Set value for pcuType
     *
     * @param pcuType new value for pcuType
     */
    public void setPcuType(PCUType pcuType) {
        this.pcuType = pcuType;
    }

    /**
     * Gets name
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set value for name
     *
     * @param name new value for name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets orgId
     *
     * @return value of orgId
     */
    public String getOrgId() {
        return orgId;
    }

    /**
     * Set value for orgId
     *
     * @param orgId new value for orgId
     */
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /**
     * Gets title
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set value for title
     *
     * @param title new value for title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets description
     *
     * @return value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set value for description
     *
     * @param description new value for description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets cloudProvider
     *
     * @return value of cloudProvider
     */
    public CloudProviderType getCloudProvider() {
        return cloudProvider;
    }

    /**
     * Set value for cloudProvider
     *
     * @param cloudProvider new value for cloudProvider
     */
    public void setCloudProvider(CloudProviderType cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    /**
     * Gets region
     *
     * @return value of region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set value for region
     *
     * @param region new value for region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets instanceType
     *
     * @return value of instanceType
     */
    public String getInstanceType() {
        return instanceType;
    }

    /**
     * Set value for instanceType
     *
     * @param instanceType new value for instanceType
     */
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Gets provisionType
     *
     * @return value of provisionType
     */
    public String getProvisionType() {
        return provisionType;
    }

    /**
     * Set value for provisionType
     *
     * @param provisionType new value for provisionType
     */
    public void setProvisionType(String provisionType) {
        this.provisionType = provisionType;
    }

    /**
     * Gets min
     *
     * @return value of min
     */
    public int getMin() {
        return min;
    }

    /**
     * Set value for min
     *
     * @param min new value for min
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * Gets max
     *
     * @return value of max
     */
    public int getMax() {
        return max;
    }

    /**
     * Set value for max
     *
     * @param max new value for max
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * Gets reserved
     *
     * @return value of reserved
     */
    public int getReserved() {
        return reserved;
    }

    /**
     * Set value for reserved
     *
     * @param reserved new value for reserved
     */
    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    /**
     * Gets createdAt
     *
     * @return value of createdAt
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Set value for createdAt
     *
     * @param createdAt new value for createdAt
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets updatedAt
     *
     * @return value of updatedAt
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set value for updatedAt
     *
     * @param updatedAt new value for updatedAt
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets createdBy
     *
     * @return value of createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Set value for createdBy
     *
     * @param createdBy new value for createdBy
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets updatedBy
     *
     * @return value of updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set value for updatedBy
     *
     * @param updatedBy new value for updatedBy
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Gets status
     *
     * @return value of status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set value for status
     *
     * @param status new value for status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}

package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for PCU (Processing Capacity Units) Group creation and update requests.
 * Contains common fields and validation logic shared between creation and update operations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class PcuGroupCreateUpdateRequest {

    /**
     * Human-readable title for the PCU group.
     */
    protected String title;
    
    /**
     * Description of the PCU group.
     */
    protected String description;

    /**
     * Cloud provider where the PCU group is deployed.
     */
    protected CloudProviderType cloudProvider;
    
    /**
     * Cloud region where the PCU group is deployed.
     */
    protected String region;

    // --------------------------------------
    //          Capacity Details
    // --------------------------------------

    /**
     * Committed vs Flexible Workload.
     * <p></p>
     * Commited: Committed capacity workloads include continuously-provisioned resources, and they can never scale to zero.
     * Committed capacity workloads are intended for any database in any environment that requires long-term, continuous
     * availability, such as multi-region databases and latency sensitive workloads.
     * <p></p>
     * Flexible capacity workloads: PCU groups for flexible capacity workloads are billed entirely at the HCU rate, and they
     * have the option to manually scale to zero. With flexible capacity workloads, you are billed for continuous HCU usage
     * based on the group’s minimum capacity. While flexible capacity workloads don’t require a commitment to reserved capacity,
     * they don’t offer cost savings for continuous usage that can be realized at the RCU rate.
     */
    protected PcuCapacityWorkloadType workloadType;

    /**
     * Provisioning type for the PCU group.
     */
    @Setter(AccessLevel.NONE)
    protected PcuProvisionType provisionType;
    
    public PcuGroupCreateUpdateRequest setProvisionType(PcuProvisionType provisionType) {
        this.provisionType = provisionType;
        return this;
    }

    /**
     * Minimum number of PCUs (must be greater or equals to 1).
     */
    protected Integer min; // Integers so they're nullable

    /**
     * Maximum number of PCUs (must be greater or equals to min).
     */
    protected Integer max;

    /**
     * Number of reserved PCUs (must be non-negative and lower or equals to min).
     */
    protected Integer reserved;

    /**
     * Validates the request fields.
     *
     * @throws IllegalArgumentException
     *      if any required field is missing or invalid
     */
    protected void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("PCU group title is required");
        }

        if (cloudProvider == null) {
            throw new IllegalArgumentException("PCU group cloud provider is required");
        }

        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("PCU group region is required");
        }

        if (min == null || min < 1) {
            throw new IllegalArgumentException("PCU group min must be >= 1");
        }

        if (max == null || max < min) {
            throw new IllegalArgumentException("PCU group max must be >= min");
        }

        if (reserved != null && (reserved < 0 || reserved > min)) {
            throw new IllegalArgumentException("PCU group reserved must be non-negative and <= min");
        }
    }
}

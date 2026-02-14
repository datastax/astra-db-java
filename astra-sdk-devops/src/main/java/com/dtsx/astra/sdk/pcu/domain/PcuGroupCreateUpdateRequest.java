package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
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

    /**
     * Minimum number of PCUs (must be >= 1).
     */
    protected Integer min; // Integers so they're nullable
    
    /**
     * Maximum number of PCUs (must be >= min).
     */
    protected Integer max;
    
    /**
     * Number of reserved PCUs (must be non-negative and <= min).
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

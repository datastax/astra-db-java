package com.dtsx.astra.sdk.pcu.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Request object for creating a new PCU (Processing Capacity Units) Group.
 * Extends the base create/update request with creation-specific fields.
 */
@Getter
@Setter
@SuperBuilder
public final class PcuGroupCreationRequest extends PcuGroupCreateUpdateRequest {
    /**
     * Instance type for the PCU group (e.g., "standard").
     */
    private String instanceType;
    
    /**
     * Provisioning type for the PCU group.
     */
    private PcuProvisionType provisionType;

    /**
     * Applies default values and validates the request before creation.
     * Sets default provision type to SHARED, instance type to "standard", and reserved to 0 if not specified.
     *
     * @return
     *      this request with defaults applied
     */
    public PcuGroupCreationRequest withDefaultsAndValidations() {
        if (this.provisionType == null) {
            this.provisionType = PcuProvisionType.SHARED;
        }

        // TODO do we really want a default for this? (since pcu instance types are changing)
        if (this.instanceType == null || this.instanceType.isBlank()) {
            this.instanceType = "standard";
        }

        if (this.reserved == null) {
            this.reserved = 0;
        }

        return this;
    }
}

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
public final class PCUGroupCreationRequest extends PCUGroupCreateUpdateRequest {

    /**
     * Instance type for the PCU group (e.g., "standard").
     */
    private String instanceType;

    /**
     * Applies default values and validates the request before creation.
     * Sets default provision type to SHARED, instance type to "standard", and reserved to 0 if not specified.
     *
     * @return
     *      this request with defaults applied
     */
    public PCUGroupCreationRequest withDefaultsAndValidations() {
        if (this.provisionType == null) {
            this.provisionType = PCUProvisionType.shared.name();
        }

        // De
        if (this.instanceType == null || this.instanceType.isBlank()) {
            this.instanceType = PCUInstanceType.small.toString();
        }

        if (this.reserved == null) {
            this.reserved = 0;
            this.min = 1;
            this.max = 1;
        }

        return this;
    }


}

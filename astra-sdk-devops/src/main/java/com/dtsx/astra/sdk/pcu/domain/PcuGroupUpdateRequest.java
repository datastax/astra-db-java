package com.dtsx.astra.sdk.pcu.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Request object for updating an existing PCU (Processing Capacity Units) Group.
 * Supports partial updates where only specified fields are modified.
 */
@SuperBuilder
@NoArgsConstructor
public class PcuGroupUpdateRequest extends PcuGroupCreateUpdateRequest {
    /**
     * Applies defaults from the existing PCU group and validates the update request.
     * Fields not specified in the update request will retain their current values from the base PCU group.
     *
     * @param base
     *      the existing PCU group to update
     * @return
     *      internal representation with defaults applied and validation performed
     */
    // TODO once the bug that causes fields to potentially be lost during partial updates is fixed, we can remove the base parameter here
    public PcuGroupCreateUpdateRequest withDefaultsAndValidations(PcuGroup base) {
        InternalRep internalRep = new InternalRep();
        internalRep.setTitle(this.title == null ? base.getTitle() : this.title);
        internalRep.setDescription(this.description == null ? base.getDescription() : this.description);
        internalRep.setCloudProvider(this.cloudProvider == null ? base.getCloudProvider() : this.cloudProvider);
        internalRep.setRegion(this.region == null ? base.getRegion() : this.region);
        internalRep.setMin(this.min == null ? base.getMin() : this.min);
        internalRep.setMax(this.max == null ? base.getMax() : this.max);
        internalRep.setReserved(this.reserved == null ? base.getReserved() : this.reserved);

        internalRep.validate();

        return internalRep
            .setPcuGroupUUID(base.getId())
            .setInstanceType(base.getInstanceType())
            .setProvisionType(base.getProvisionType());
    }

    /**
     * Internal representation with additional fields for update operations.
     */
    @Setter
    @Getter
    @Accessors(chain = true)
    static class InternalRep extends PcuGroupUpdateRequest {
        private String pcuGroupUUID;
        private String instanceType;
        private PcuProvisionType provisionType;

        InternalRep() {
            super();
        }
    }
}

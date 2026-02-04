package com.dtsx.astra.sdk.pcu.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.val;

/**
 * Request object for updating an existing PCU (Processing Capacity Units) Group.
 * Supports partial updates where only specified fields are modified.
 */
@SuperBuilder
public non-sealed class PcuGroupUpdateRequest extends PcuGroupCreateUpdateRequest {
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
        val internalRep = new InternalRep(
            builder()
                .title(this.title == null ? base.getTitle() : this.title)
                .description(this.description == null ? base.getDescription() : this.description)
                .cloudProvider(this.cloudProvider == null ? base.getCloudProvider() : this.cloudProvider)
                .region(this.region == null ? base.getRegion() : this.region)
                .min(this.min == null ? base.getMin() : this.min)
                .max(this.max == null ? base.getMax() : this.max)
                .reserved(this.reserved == null ? base.getReserved() : this.reserved)
        );

        internalRep.validate();

        return internalRep
            .setPcuGroupUUID(base.getId())
            .setInstanceType(base.getInstanceType())
            .setProvisionType(base.getProvisionType());
    }

    // Internal representation - excluded from Javadoc due to Lombok builder references
    @Setter
    @Getter
    @Accessors(chain = true)
    static class InternalRep extends PcuGroupUpdateRequest {
        private String pcuGroupUUID;
        private String instanceType;
        private PcuProvisionType provisionType;

        @SuppressWarnings("all")
        InternalRep(PcuGroupUpdateRequestBuilder<?, ?> b) {
            super(b);
        }
    }
}

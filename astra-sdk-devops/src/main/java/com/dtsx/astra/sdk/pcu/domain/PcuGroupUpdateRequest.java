package com.dtsx.astra.sdk.pcu.domain;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.val;

@SuperBuilder
public non-sealed class PcuGroupUpdateRequest extends PcuGroupCreateUpdateRequest {
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
            .setPcuGroupUUID(base.getUuid())
            .setInstanceType(base.getInstanceType())
            .setProvisionType(base.getProvisionType());
    }

    @Setter
    @Accessors(chain = true)
    public static class InternalRep extends PcuGroupUpdateRequest {
        private String pcuGroupUUID;
        private String instanceType;
        private PcuProvisionType provisionType;

        protected InternalRep(PcuGroupUpdateRequestBuilder<?, ?> b) {
            super(b);
        }
    }
}

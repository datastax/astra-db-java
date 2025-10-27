package com.dtsx.astra.sdk.pcu.domain;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class PcuGroupCreationRequest extends PcuGroupCreateUpdateRequest {
    private String instanceType;
    private PcuProvisionType provisionType;

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

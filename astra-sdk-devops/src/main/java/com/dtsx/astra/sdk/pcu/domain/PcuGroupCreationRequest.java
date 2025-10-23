package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.Value;

@Value
public class PcuGroupCreationRequest {
    String title;
    String description;

    CloudProviderType cloudProvider;
    String region;

    String instanceType;
    PcuProvisionType provisionType;

    int min;
    int max;
    int reserved;

    public PcuGroupCreationRequest(PcuGroupCreationBuilder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.cloudProvider = builder.cloudProvider;
        this.region = builder.cloudRegion;
        this.instanceType = builder.instanceType;
        this.provisionType = builder.provisionType;
        this.min = builder.minCapacity;
        this.max = builder.maxCapacity;
        this.reserved = builder.reservedCapacity;
    }

    public static PcuGroupCreationBuilder builder() {
        return new PcuGroupCreationBuilder();
    }
}

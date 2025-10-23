package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
public class PcuGroupCreationBuilder {
    protected String title; // TODO should we have an alias setter function called 'name' for consistency/ease-of-use?
    protected String description;

    protected CloudProviderType cloudProvider;
    protected String cloudRegion;

    protected String instanceType;
    protected PcuProvisionType provisionType;

    protected int minCapacity;
    protected int maxCapacity;
    protected int reservedCapacity;

    public PcuGroupCreationBuilder() {}

    public PcuGroupCreationBuilder(String title) {
        this.title = title;
    }

    public PcuGroupCreationRequest build() {
        return new PcuGroupCreationRequest(this);
    }
}

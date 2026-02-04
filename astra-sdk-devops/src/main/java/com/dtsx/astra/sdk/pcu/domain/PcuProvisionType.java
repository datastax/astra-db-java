package com.dtsx.astra.sdk.pcu.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Enumeration of PCU (Processing Capacity Units) provisioning types.
 * Defines how compute resources are allocated for a PCU group.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PcuProvisionType {
    /**
     * Shared provisioning - resources are shared across multiple tenants.
     * More cost-effective but with potential resource contention.
     */
    SHARED("shared"),
    
    /**
     * Dedicated provisioning - resources are exclusively allocated.
     * Higher cost but guaranteed performance and isolation.
     */
    DEDICATED("dedicated");

    /**
     * JSON serialization value for the provision type.
     */
    @JsonValue
    private final String fieldValue;
}

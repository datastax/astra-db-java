package com.dtsx.astra.sdk.pcu.domain;

import lombok.Getter;

@Getter
public enum PCUInstanceType {
    small,
    medium,
    generalPurpose,
    cacheOptimized,

    // Legacy
    standard,
    storageOptimized;

}

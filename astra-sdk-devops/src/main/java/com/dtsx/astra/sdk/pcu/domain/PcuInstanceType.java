package com.dtsx.astra.sdk.pcu.domain;

import lombok.Getter;

@Getter
public enum PcuInstanceType {

    SMALL("small"),
    MEDIUM("medium"),
    GENERAL_PURPOSE("generalPurpose"),
    CACHE_OPTIMIZED("cacheOptimized"),

    // Legacy
    STANDARD("standard"),
    STORAGE_OPTIMIZED("storageOptimized");

    final String code;

    PcuInstanceType(String code) {
        this.code = code;
    }
}

package com.dtsx.astra.sdk.pcu.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PcuProvisionType {
    SHARED("shared"),
    DEDICATED("dedicated");

    @JsonValue
    private final String fieldValue;
}

package com.dtsx.astra.sdk.pcu.domain;

public enum PcuGroupStatusType {
    CREATED,
    PLACING,
    INITIALIZING,
    ACTIVE,
    PARKED,
    PARKING,
    UNPARKING,
    OTHER // TODO make this work with Jackson
}

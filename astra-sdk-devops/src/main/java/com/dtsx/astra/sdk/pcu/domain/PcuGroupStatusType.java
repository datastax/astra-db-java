package com.dtsx.astra.sdk.pcu.domain;

// TODO should this be a sealed interface with an 'OTHER(<status>)' (or UNKNOWN) implementation to future-proof?
public enum PcuGroupStatusType {
    CREATED,
    PLACING,
    INITIALIZING,
    ACTIVE,
    PARKED,
    PARKING,
    UNPARKING
}

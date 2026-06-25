package com.dtsx.astra.sdk.pcu.domain;

/**
 * Enumeration of PCU (Processing Capacity Units) Group status types.
 * Represents the various lifecycle states of a PCU group.
 */
public enum PcuGroupStatusType {
    /**
     * PCU group has been created but not yet placed.
     */
    CREATED,
    
    /**
     * PCU group is being placed in the infrastructure.
     */
    PLACING,
    
    /**
     * PCU group is initializing resources.
     */
    INITIALIZING,
    
    /**
     * PCU group is active and ready for use.
     */
    ACTIVE,
    
    /**
     * PCU group is parked (resources minimized to reduce costs).
     */
    PARKED,
    
    /**
     * PCU group is in the process of parking.
     */
    PARKING,
    
    /**
     * PCU group is in the process of unparking (restoring resources).
     */
    UNPARKING,
    
    /**
     * Unknown or unrecognized status.
     */
    OTHER // TODO make this work with Jackson
}

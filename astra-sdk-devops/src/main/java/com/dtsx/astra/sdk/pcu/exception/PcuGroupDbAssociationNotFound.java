package com.dtsx.astra.sdk.pcu.exception;

import lombok.Getter;

/**
 * Exception thrown when a datacenter association with a PCU (Processing Capacity Units) Group cannot be found.
 * This occurs when attempting to access or modify an association that doesn't exist.
 */
public class PcuGroupDbAssociationNotFound extends RuntimeException {
    /**
     * PCU group unique identifier.
     */
    @Getter
    private final String pcuGroupId;

    /**
     * Datacenter unique identifier.
     */
    @Getter
    private final String datacenterId;

    /**
     * Creates an exception for a missing PCU group-datacenter association.
     *
     * @param pcuGroupId
     *      the PCU group ID
     * @param datacenterId
     *      the datacenter ID
     */
    public PcuGroupDbAssociationNotFound(String pcuGroupId, String datacenterId) {
        super("Association not found for pcu group '" + pcuGroupId + "' and datacenter '" + datacenterId + "'");
        this.pcuGroupId = pcuGroupId;
        this.datacenterId = datacenterId;
    }
}

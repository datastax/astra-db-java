package com.dtsx.astra.sdk.pcu.exception;

import lombok.Getter;

public class PcuGroupDbAssociationNotFound extends RuntimeException {
    @Getter
    private final String pcuGroupId;

    @Getter
    private final String datacenterId;

    public PcuGroupDbAssociationNotFound(String pcuGroupId, String datacenterId) {
        super("Association not found for pcu group '" + pcuGroupId + "' and datacenter '" + datacenterId + "'");
        this.pcuGroupId = pcuGroupId;
        this.datacenterId = datacenterId;
    }
}

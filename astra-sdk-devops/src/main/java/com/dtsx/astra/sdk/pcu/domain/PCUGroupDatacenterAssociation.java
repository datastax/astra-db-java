package com.dtsx.astra.sdk.pcu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Represents an association between a PCU (Processing Capacity Units) Group and a datacenter.
 * This association links compute resources to specific database datacenters.
 */
// TODO add the rest of the fields once the PCU team is clear about what is going on
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroupDatacenterAssociation {
    /**
     * PCU group unique identifier.
     */
    private String pcuGroupUUID;
    
    /**
     * Datacenter unique identifier.
     */
    private String datacenterUUID;
}

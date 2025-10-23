package com.dtsx.astra.sdk.pcu.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroup {
    private String uuid; // TODO should we call it 'id' for consistency? w/ @JsonProperty("uuid") + should this be a UUID?
    private String orgId;

    private String title;
    private String description; // TODO should we use Optional<...>s?

    private CloudProviderType cloudProvider;
    private String region;

    private String instanceType;
    private PcuProvisionType provisionType;

    private int min;
    private int max;
    private int reserved;

    private String createdAt; // TODO should these be DateTimes/Instants instead?
    private String updatedAt;
    private String createdBy;
    private String updatedBy;

    private PcuGroupStatusType status;
}

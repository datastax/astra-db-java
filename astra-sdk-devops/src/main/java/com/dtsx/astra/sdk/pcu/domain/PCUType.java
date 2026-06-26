package com.dtsx.astra.sdk.pcu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PCUType {
    String type;
    String region;
    String provider;
    Map<String, Object> details;
    boolean enabled;
}

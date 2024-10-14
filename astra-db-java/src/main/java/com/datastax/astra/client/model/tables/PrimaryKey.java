package com.datastax.astra.client.model.tables;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;

@Data @NoArgsConstructor
public class PrimaryKey {

    @JsonProperty("partitionBy")
    private List<String> partitionBy;

    @JsonProperty("partitionSort")
    private LinkedHashMap<String, Integer> partitionSort;
}

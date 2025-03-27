package com.datastax.astra.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product<ID> {
    @JsonProperty("_id")
    protected ID     id;
    protected String name;
    protected Double price;
    protected UUID code;
}
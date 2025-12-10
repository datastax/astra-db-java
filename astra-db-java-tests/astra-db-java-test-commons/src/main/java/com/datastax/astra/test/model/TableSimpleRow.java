package com.datastax.astra.test.model;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TableSimpleRow {
    String email;
    Integer age;
    String country;
    String name;
    Boolean human;
}

package com.datastax.astra.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableCompositeRow {
    private int age;
    private String name;
    private String id;
}

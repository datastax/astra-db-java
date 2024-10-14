package com.datastax.astra.client.model.tables;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data @NoArgsConstructor
public class TableDefinition {

    private String name;

    private LinkedHashMap<String, ColumnDefinition> columns;

    private PrimaryKey primaryKey;
}

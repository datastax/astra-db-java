package com.datastax.astra.client.model.tables;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class ColumnDefinitionApiSupport {

    private boolean createTable;

    private boolean insert;

    private boolean read;

    private String cqlDefinition;
}

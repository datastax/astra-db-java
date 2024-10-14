package com.datastax.astra.client.model.tables;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class ColumnDefinition {

    private String type;

    private String keyType;

    private String valueType;

    private ColumnDefinitionApiSupport apiSupport;
}

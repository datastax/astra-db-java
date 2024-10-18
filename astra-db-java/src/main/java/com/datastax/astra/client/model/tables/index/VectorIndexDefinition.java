package com.datastax.astra.client.model.tables.index;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Index Definitions.
 */
@Data @NoArgsConstructor
public class VectorIndexDefinition {

    String column;

    IndexDefinitionOptions options;

    public VectorIndexDefinition column(String column) {
        this.column = column;
        return this;
    }

    public VectorIndexDefinition options(IndexDefinitionOptions options) {
        this.options = options;
        return this;
    }

}

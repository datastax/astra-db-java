package com.datastax.astra.client.model.tables.index;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Index Definitions.
 */
@Data @NoArgsConstructor
public class IndexDefinition {

    String column;

    IndexDefinitionOptions options;

    public IndexDefinition column(String column) {
        this.column = column;
        return this;
    }

    public IndexDefinition options(IndexDefinitionOptions options) {
        this.options = options;
        return this;
    }

}

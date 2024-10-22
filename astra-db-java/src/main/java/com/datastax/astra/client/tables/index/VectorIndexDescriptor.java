package com.datastax.astra.client.tables.index;

import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VectorIndexDescriptor {

    /**
     * Name of the table.
     */
    private String name;

    /**
     * Options for the table.
     */
    private VectorIndexDefinition definition;

    /**
     * Default constructor.
     */
    public VectorIndexDescriptor() {
        // left blank, serialization with jackson
    }

    /**
     * Default constructor.
     */
    public VectorIndexDescriptor(String name) {
        // left blank, serialization with jackson
        this.name = name;
    }

    public VectorIndexDescriptor name(String name) {
        this.name = name;
        return this;
    }

    public VectorIndexDescriptor definition(VectorIndexDefinition def) {
        this.definition = def;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.marshall(this);
    }
}

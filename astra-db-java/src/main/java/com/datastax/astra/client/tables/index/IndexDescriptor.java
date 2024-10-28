package com.datastax.astra.client.tables.index;

import com.datastax.astra.internal.serializer.tables.RowSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexDescriptor {

    /**
     * Name of the table.
     */
    private String name;

    /**
     * Options for the table.
     */
    private IndexDefinition definition;

    /**
     * Default constructor.
     */
    public IndexDescriptor() {
        // left blank, serialization with jackson
    }

    /**
     * Default constructor.
     */
    public IndexDescriptor(String name) {
        // left blank, serialization with jackson
        this.name = name;
    }

    public IndexDescriptor name(String name) {
        this.name = name;
        return this;
    }

    public IndexDescriptor definition(IndexDefinition def) {
        this.definition = def;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}

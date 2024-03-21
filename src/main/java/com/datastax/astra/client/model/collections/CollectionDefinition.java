package com.datastax.astra.client.model.collections;

import io.stargate.sdk.utils.JsonUtils;
import lombok.Data;

/**
 * Represents the Collection definition with its name and metadata.
 */
@Data
public class CollectionDefinition {

    /**
     * Name of the collection.
     */
    private String name;

    /**
     * Options for the collection.
     */
    private CollectionOptions options;

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.marshallForDataApi(this);
    }
}

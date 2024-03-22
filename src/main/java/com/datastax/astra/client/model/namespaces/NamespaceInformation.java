package com.datastax.astra.client.model.namespaces;

import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Data;

/**
 * Represents the Namespace (keyspac) definition with its name and metadata.
 */
@Data
public class NamespaceInformation {

    /**
     * Replication strategies
     */
    public enum ReplicationStrategy {

        /**
         * dev
         */
        SimpleStrategy,

        /**
         * prod
         */
        NetworkTopologyStrategy
    }

    /**
     * The name of the namespace.
     */
    private String name;

    /**
     * The options of the namespace.
     */
    private CreateNamespaceOptions options;

    /**
     * Default Constructor.
     */
    public NamespaceInformation() {
    }

    /**
     * Default Constructor.
     *
     * @param name
     *      create namespace information with name
     */
    public NamespaceInformation(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.marshallForDataApi(this);
    }

}

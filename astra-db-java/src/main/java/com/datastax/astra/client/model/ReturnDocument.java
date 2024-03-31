package com.datastax.astra.client.model;

import lombok.Getter;

/**
 * Enum to define the return document option.
 */
@Getter
public enum ReturnDocument {

    /** if set to before, the command will return the document before the update */
    BEFORE("before"),

    /** if set to after, the command will return the document after the update */
    AFTER("after");

    /** key to be used in the JSON payload */
    private final String key;

    /**
     * Constructor.
     *
     * @param key key to be used in the JSON payload
     */
    ReturnDocument(String key) {
        this.key = key;
    }
}
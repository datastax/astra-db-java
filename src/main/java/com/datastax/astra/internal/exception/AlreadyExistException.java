package com.datastax.astra.internal.exception;

/**
 * Specialization when creating an entity which should be unique.
 */
public class AlreadyExistException extends IllegalArgumentException {

    /**
     * Default error.
     *
     * @param msg
     *      error message
     */
    public AlreadyExistException(String msg) {
        super(msg);
    }

}

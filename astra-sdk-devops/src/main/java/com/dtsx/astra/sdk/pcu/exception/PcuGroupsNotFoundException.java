package com.dtsx.astra.sdk.pcu.exception;

/**
 * Exception thrown when multiple PCU (Processing Capacity Units) Groups cannot be found.
 * Typically used when querying for multiple groups by IDs and one or more are not found.
 */
public class PcuGroupsNotFoundException extends RuntimeException {
    /**
     * Creates an exception with the specified error message.
     *
     * @param message
     *      the error message from the API
     */
    public PcuGroupsNotFoundException(String message) {
        super(message); // unfortunately the devops api doesn't return a very workable error so will just use the message directly
    }
}

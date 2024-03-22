package com.datastax.astra.client.exception;

import com.datastax.astra.client.DataAPIOptions;

/**
 * Error when too many documents in the collection
 */
public class TooManyDocumentsToCountException extends Exception {

    /**
     * Default constructor.
     */
    public TooManyDocumentsToCountException() {
        super("Document count exceeds '" + DataAPIOptions.getMaxDocumentCount() + ", the maximum allowed by the server");
    }

    /**
     * Default constructor.
     */
    public TooManyDocumentsToCountException(int upperLimit) {
        super("Document count exceeds upper bound set in method call " + upperLimit);
    }
}

package com.datastax.astra.client.exception;

import lombok.Getter;

/**
 * An error occurred with the JSON API
 */
@Getter
public class DataApiException extends RuntimeException {

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** Default error message. */
    public static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred for Data API";

    /** Default error code. */
    public static final String DEFAULT_ERROR_CODE = "CLIENT_ERROR";

    /** Error. */
    private final String errorCode;

    /**
     * Empty constructor with defaults
     */
    public DataApiException() {
        this(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE);
    }

    /**
     *Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     * @param errorCode
     *      error code
     */
    public DataApiException(String errorCode, String errorMessage) {
        super("[" + errorCode + "] - " + errorMessage);
        this.errorCode = errorCode;
    }

    /**
     * Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     * @param errorCode
     *      error code
     * @param parent
     *      parent exception
     */
    public DataApiException(String errorMessage, String errorCode, Throwable parent) {
        super(errorMessage, parent);
        this.errorCode = errorCode;
    }

}

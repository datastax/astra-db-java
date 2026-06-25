package com.datastax.astra.client.exceptions;

import lombok.Getter;

@Getter
public class DevOpsAPIException  extends RuntimeException {

    /** Default error message. */
    public static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred for Astra Devops API";



}

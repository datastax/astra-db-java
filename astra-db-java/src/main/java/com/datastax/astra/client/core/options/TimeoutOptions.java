package com.datastax.astra.client.core.options;

import lombok.Data;

@Data
public class TimeoutOptions {
    long requestTimeoutMillis;

    long dataOperationTimeoutMillis;

    long schemaOperationTimeoutMillis;

    long databaseAdminTimeoutMillis;

    long keyspaceAdminTimeoutMillis;
}

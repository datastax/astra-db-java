package com.datastax.astra.client.model.misc;

import lombok.Data;

@Data
public final class BulkWriteOptions {

    private boolean ordered = true;

    private Integer concurrency = 5;
}
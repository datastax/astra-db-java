package com.datastax.astra.client.model.misc;

import io.stargate.sdk.data.client.model.ApiResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * List of responses
 */
@Data
public class BulkWriteResult {

    List<ApiResponse> responses;

    public BulkWriteResult(int size) {
        this.responses = new ArrayList<>(size);

    }



}

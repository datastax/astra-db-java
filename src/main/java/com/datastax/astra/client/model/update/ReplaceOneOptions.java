package com.datastax.astra.client.model.update;

import lombok.Data;

@Data
public class ReplaceOneOptions {

    /**
     * if upsert is selected
     */
    Boolean upsert = false;
}

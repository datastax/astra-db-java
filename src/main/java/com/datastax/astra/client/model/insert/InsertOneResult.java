package com.datastax.astra.client.model.insert;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InsertOneResult {

    Object insertedId;

}

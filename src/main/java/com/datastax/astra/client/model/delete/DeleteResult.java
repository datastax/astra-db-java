package com.datastax.astra.client.model.delete;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class DeleteResult {

    int deletedCount;

}

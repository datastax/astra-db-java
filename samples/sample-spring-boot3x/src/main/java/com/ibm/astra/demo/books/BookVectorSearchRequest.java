package com.ibm.astra.demo.books;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BookVectorSearchRequest", description = "Request payload for vector search on books")
public class BookVectorSearchRequest {

    @Schema(description = "Natural language query to vectorize", example = "space survival and science fiction")
    private String query;

    @Schema(description = "Maximum number of books to return", example = "5", defaultValue = "5")
    private Integer limit = 5;
}

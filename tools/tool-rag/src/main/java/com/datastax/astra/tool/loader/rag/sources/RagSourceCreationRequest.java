package com.datastax.astra.tool.loader.rag.sources;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
public class RagSourceCreationRequest {

    // author
    UUID createdBy;

    // needed to get a name
    String name;

    // needed to select the loader
    RagSources source;

    // needed to select capture the data
    String location;

}

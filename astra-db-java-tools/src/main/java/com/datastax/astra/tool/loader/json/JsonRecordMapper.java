package com.datastax.astra.tool.loader.json;

import com.datastax.astra.client.collections.definition.documents.Document;

/**
 * To import a Json containing an arrray of records.
 */
public interface JsonRecordMapper {

    Document map(Document jsonRecord);
}

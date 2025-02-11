package com.datastax.astra.tool.loader.csv;

import com.datastax.astra.client.collections.definition.documents.Document;

/**
 * Settings for the CSV Loader
 */
public interface CsvRowMapper {

    /**
     * Process the document
     *
     * @param doc
     *      document to process
     */
    Document map(Document doc) ;

}

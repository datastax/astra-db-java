package com.datastax.astra.tool.loader.csv;

import com.datastax.astra.client.model.collections.Document;

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

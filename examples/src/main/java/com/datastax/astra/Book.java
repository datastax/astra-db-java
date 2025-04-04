package com.datastax.astra;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.ColumnVector;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

@Data
@EntityTable("quickstart_table")
public class Book {

    @PartitionBy(0)
    String title;

    @PartitionBy(1)
    String author;

    int numberOfPages;

    int publicationYear;

    float rating;

    String summary;

    Set<String> genres;

    Map<String, String> metadata;

    String borrower;

    boolean checkedOut;

    @Column(name = "dueDate", type = ColumnTypes.DATE)
    Date dueDate;

    @ColumnVector(
            name="summaryGenresVector",
            // Vector properties
            dimension = 1024, metric = COSINE,
            // Adding vector service
            provider = "nvidia", modelName = "NV-Embed-QA")
    DataAPIVector summaryGenresVector;

}

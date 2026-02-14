package com.datastax.astra.test.samples.model;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.ColumnVector;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

/**
 * Demonstrates {@code @EntityTable} with composite {@code @PartitionBy},
 * {@code @ColumnVector} with vectorize service in annotation,
 * and {@link Map}/{@link Set}/{@link Date} fields.
 *
 * @see EntityTable
 * @see PartitionBy
 * @see ColumnVector
 */
@Data
@EntityTable("quickstart_table")
@SuppressWarnings("unused")
public class SampleEntityBook {

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

    @Column(name = "dueDate", type = TableColumnTypes.DATE)
    Date dueDate;

    @ColumnVector(
            name = "summaryGenresVector",
            dimension = 1024, metric = COSINE,
            provider = "nvidia", modelName = "NV-Embed-QA")
    DataAPIVector summaryGenresVector;
}

package com.datastax.astra.client.tables;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

import static com.datastax.astra.client.tables.columns.ColumnTypes.INT;
import static com.datastax.astra.client.tables.columns.ColumnTypes.SET;
import static com.datastax.astra.client.tables.columns.ColumnTypes.TEXT;
import static com.datastax.astra.client.tables.columns.ColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.columns.ColumnTypes.UUID;
import static com.datastax.astra.client.tables.columns.ColumnTypes.VECTOR;

@Data
@NoArgsConstructor
public class Game {

    private String matchId;

    private Integer round;

    private Integer score;

    private Instant when;

    private Instant winner;

    private Set<String> fighters;

    private DataAPIVector vector;

}

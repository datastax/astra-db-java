package com.datastax.astra.client.tables;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
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
import static com.datastax.astra.client.tables.columns.ColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.columns.ColumnTypes.UUID;
import static com.datastax.astra.client.tables.columns.ColumnTypes.VECTOR;

@Data
@EntityTable("game")
@NoArgsConstructor
@AllArgsConstructor
public class GameWithAnnotation {

    @PartitionBy(0)
    @Column("match_id")
    private String matchId;

    @PartitionSort(position = 0, order= SortOrder.ASCENDING)
    @Column(value="round", type=INT)
    private Integer round;

    @Column(value="score", type=INT)
    private Integer score;

    @Column(value="when", type=TIMESTAMP)
    private Instant when;

    @Column(value="winner", type=TIMESTAMP)
    private Instant winner;

    @Column(value="fighters", type=SET, valueType = UUID)
    private Set<String> fighters;

    @Column(value="m_vector", type=VECTOR, dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
    
}

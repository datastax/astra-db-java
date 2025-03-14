package com.datastax.astra.test.model;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.ColumnVector;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@EntityTable("game_ann2")
@NoArgsConstructor
@AllArgsConstructor
public class TableEntityGameWithAnnotation {

    @PartitionBy(0)
    @Column(name ="match_id")
    private String matchId;

    @PartitionSort(position = 0, order = SortOrder.ASCENDING)
    private Integer round;

    private Integer score;

    private Instant when;

    private String winner;

    private Set<UUID> fighters;

    @ColumnVector(name ="m_vector", dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
    
}

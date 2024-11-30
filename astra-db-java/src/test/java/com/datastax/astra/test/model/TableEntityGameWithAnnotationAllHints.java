package com.datastax.astra.test.model;

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
import static com.datastax.astra.client.tables.columns.ColumnTypes.TEXT;
import static com.datastax.astra.client.tables.columns.ColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.columns.ColumnTypes.UUID;
import static com.datastax.astra.client.tables.columns.ColumnTypes.VECTOR;

@Data
@EntityTable("game_ann1")
@NoArgsConstructor
@AllArgsConstructor
public class TableEntityGameWithAnnotationAllHints {

    @PartitionBy(0)
    @Column(name ="match_id", type=TEXT )
    private String matchId;

    @PartitionSort(position = 0, order= SortOrder.ASCENDING)
    @Column(name ="round", type=INT)
    private Integer round;

    @Column(name ="score", type=INT)
    private Integer score;

    @Column(name ="when", type=TIMESTAMP)
    private Instant when;

    @Column(name ="winner", type=TEXT)
    private String winner;

    @Column(name ="fighters", type=SET, valueType = UUID)
    private Set<java.util.UUID> fighters;

    @Column(name ="m_vector", type=VECTOR, dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
    
}

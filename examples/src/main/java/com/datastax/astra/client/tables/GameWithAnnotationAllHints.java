package com.datastax.astra.client.tables;

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

import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.INT;
import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.SET;
import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.TEXT;
import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.UUID;
import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.VECTOR;

@Data
@EntityTable("game_ann1")
@NoArgsConstructor
@AllArgsConstructor
public class GameWithAnnotationAllHints {

    @PartitionBy(0)
    @Column(value="match_id", type=TEXT )
    private String matchId;

    @PartitionSort(position = 0, order= SortOrder.ASCENDING)
    @Column(value ="round", type=INT)
    private Integer round;

    @Column(value ="score", type=INT)
    private Integer score;

    @Column(value ="when", type=TIMESTAMP)
    private Instant when;

    @Column(value ="winner", type=TEXT)
    private String winner;

    @Column(value ="fighters", type=SET, valueType = UUID)
    private Set<java.util.UUID> fighters;

    @ColumnVector(value ="m_vector", dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
    
}

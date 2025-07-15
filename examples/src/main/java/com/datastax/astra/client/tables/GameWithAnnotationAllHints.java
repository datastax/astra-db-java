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

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.INT;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.SET;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TEXT;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.UUID;

@Data
@EntityTable("game_ann1")
@NoArgsConstructor
@AllArgsConstructor
public class GameWithAnnotationAllHints {

    @PartitionBy(0)
    @Column(name="match_id", type=TEXT )
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

    @ColumnVector(name ="m_vector", dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
    
}

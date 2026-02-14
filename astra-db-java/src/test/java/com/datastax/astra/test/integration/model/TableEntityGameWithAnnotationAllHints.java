package com.datastax.astra.test.integration.model;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.INT;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.SET;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TEXT;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TIMESTAMP;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.UUID;

/**
 * Test model for game entity with full annotations including type hints.
 * All columns have explicit type annotations.
 */
@Data
@EntityTable("game_ann1")
@NoArgsConstructor
@AllArgsConstructor
public class TableEntityGameWithAnnotationAllHints {

    @PartitionBy(0)
    @Column(name = "match_id", type = TEXT)
    private String matchId;

    @PartitionSort(position = 0, order = SortOrder.ASCENDING)
    @Column(name = "round", type = INT)
    private Integer round;

    @Column(name = "score", type = INT)
    private Integer score;

    @Column(name = "when", type = TIMESTAMP)
    private Instant when;

    @Column(name = "winner", type = TEXT)
    private String winner;

    @Column(name = "fighters", type = SET, valueType = UUID)
    private Set<java.util.UUID> fighters;

    @ColumnVector(name = "m_vector", dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
}

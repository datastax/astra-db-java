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
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
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

/**
 * Test model for game entity with partial annotations.
 * Table name and some columns are annotated; types are inferred.
 */
@Data
@EntityTable("game_ann2")
@NoArgsConstructor
@AllArgsConstructor
public class TableEntityGameWithAnnotation {

    @PartitionBy(0)
    @Column(name = "match_id")
    private String matchId;

    @PartitionSort(position = 0, order = SortOrder.ASCENDING)
    private Integer round;

    private Integer score;

    private Instant when;

    private String winner;

    //@Column(name = "fighters", type = TableColumnTypes.TEXT, valueType = TableColumnTypes.UUID)
    private Set<UUID> fighters;

    @ColumnVector(name = "m_vector", dimension = 3, metric = SimilarityMetric.COSINE)
    private DataAPIVector vector;
}

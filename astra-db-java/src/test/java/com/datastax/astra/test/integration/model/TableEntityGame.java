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

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.mapping.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Test model for game entity without full annotations.
 */
@Data
@NoArgsConstructor
public class TableEntityGame {

    @Column(name = "match_id")
    private String matchId;

    private Integer round;

    private Integer score;

    private Instant when;

    private String winner;

    private Set<UUID> fighters;

    @Column(name = "m_vector")
    private DataAPIVector vector;
}

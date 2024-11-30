package com.datastax.astra.client.tables;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.mapping.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
public class Game {

    @Column(name ="match_id")
    private String matchId;

    private Integer round;

    private Integer score;

    private Instant when;

    private String winner;

    private Set<java.util.UUID> fighters;

    @Column(name ="m_vector")
    private DataAPIVector vector;

}

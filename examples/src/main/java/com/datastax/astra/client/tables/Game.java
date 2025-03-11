package com.datastax.astra.client.tables;

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.mapping.Column;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor
public class Game {

    @Column("match_id")
    private String matchId;

    private Integer round;

    private Integer score;

    private Instant when;

    private String winner;

    private Set<java.util.UUID> fighters;

    @Column("m_vector")
    private DataAPIVector vector;

    /**
     * Gets matchId
     *
     * @return value of matchId
     */
    public String getMatchId() {
        return matchId;
    }

    /**
     * Gets round
     *
     * @return value of round
     */
    public Integer getRound() {
        return round;
    }

    /**
     * Gets score
     *
     * @return value of score
     */
    public Integer getScore() {
        return score;
    }

    /**
     * Gets when
     *
     * @return value of when
     */
    public Instant getWhen() {
        return when;
    }

    /**
     * Gets winner
     *
     * @return value of winner
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Gets fighters
     *
     * @return value of fighters
     */
    public Set<UUID> getFighters() {
        return fighters;
    }

    /**
     * Gets vector
     *
     * @return value of vector
     */
    public DataAPIVector getVector() {
        return vector;
    }
}

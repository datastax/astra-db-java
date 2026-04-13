package com.datastax.astra.samples.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.mapping.Column;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Demonstrates typed Row methods ({@code addText}, {@code addInt}, {@code addSet},
 * {@code addList}, {@code addMap}) and POJO object mapping with {@code @Column}.
 *
 * @see Row
 * @see Table
 */
@SuppressWarnings("unused")
public class SampleTableRows {

    /** POJO for object-mapping a Row. */
    @Setter
    @Accessors(fluent = true, chain = true)
    @NoArgsConstructor
    public static class Game {
        @Column(name = "match_id")
        private String matchId;
        private Integer round;
        private Integer score;
        private Instant when;
        private String winner;
        private Set<UUID> fighters;
        @Column(name = "m_vector")
        private DataAPIVector vector;

        public String getMatchId() { return matchId; }
        public Integer getRound() { return round; }
        public Integer getScore() { return score; }
        public Instant getWhen() { return when; }
        public String getWinner() { return winner; }
        public Set<UUID> getFighters() { return fighters; }
        public DataAPIVector getVector() { return vector; }
    }

    /** Working with Set, List, and Map columns. */
    static void collectionColumns() {
        Database db = new DataAPIClient("TOKEN").getDatabase("API_ENDPOINT");

        // Set column
        Table<Row> setTable = db.createTable("TABLE_NAME", new TableDefinition()
                .addColumnSet("set_column", TableColumnTypes.INT));
        setTable.insertOne(new Row().addSet("set_column", Set.of(9, 8, 7)));

        // List column
        Table<Row> listTable = db.createTable("TABLE_NAME", new TableDefinition()
                .addColumnList("list_column", TableColumnTypes.TEXT));
        listTable.insertOne(new Row().addList("list_column", List.of("Hello", "World")));

        // Map column
        Table<Row> mapTable = db.createTable("TABLE_NAME", new TableDefinition()
                .addColumnMap("map_column", TableColumnTypes.TEXT, TableColumnTypes.INT));
        mapTable.insertOne(new Row().addMap("map_column", Map.of("key1", 1)));
    }

    /** Insert a full row with typed methods + POJO object mapping. */
    static void insertFullRowAndPojo() {
        Database db = new DataAPIClient("TOKEN").getDatabase("API_ENDPOINT");
        Table<Row> table = db.getTable("games");

        // Typed row builder
        table.insertOne(new Row()
                .addText("match_id", "mtch_0")
                .addInt("round", 1)
                .addVector("m_vector", new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f}))
                .addInt("score", 18)
                .addTimeStamp("when", Instant.now())
                .addText("winner", "Victor")
                .addSet("fighters", Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111"))));

        // POJO object mapping
        Game match1 = new Game()
                .matchId("mtch_1")
                .round(2)
                .score(20)
                .when(Instant.now())
                .winner("Victor")
                .fighters(Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111")))
                .vector(new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f}));
        db.getTable("games", Game.class).insertOne(match1);
    }
}

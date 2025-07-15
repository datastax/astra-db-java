package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.serdes.tables.RowSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorkingWithRows {

    public static void main(String[] args) {
        insertFullRow();
    }

    public static void workingWithList() {
        Database db = new DataAPIClient("token").getDatabase("endpoint");
        TableDefinition def = new TableDefinition()
                .addColumnSet("set_column", TableColumnTypes.INT);
        Table<Row> table = db.createTable("TABLE_NAME", def);
        table.insertOne(new Row().addSet("set_column", Set.of(9,8,7)));
    }

    public static void workingWithSet() {
        Database db = new DataAPIClient("token").getDatabase("endpoint");
        TableDefinition def = new TableDefinition()
                .addColumnList("list_column", TableColumnTypes.TEXT);
        Table<Row> table = db.createTable("TABLE_NAME", def);
        table.insertOne(new Row().addList("list_column", List.of("Hello", "World")));
    }

    public static void workingWithMap() {
        Database db = new DataAPIClient("token").getDatabase("endpoint");
        TableDefinition def = new TableDefinition()
                .addColumnMap("map_column", TableColumnTypes.TEXT, TableColumnTypes.INT);
        Table<Row> table = db.createTable("TABLE_NAME", def);
        table.insertOne(new Row().addMap("map_column", Map.of("key1", 1)));
    }

    public static void insertFullRow() {
        Database db = new DataAPIClient("token").getDatabase("endpoint");
        // cf CreateTable.java
        Table<Row> table = db.getTable("games");
        table.insertOne(new Row()
          .addText("match_id", "mtch_0")
          .addInt("round", 1)
          .addVector("m_vector", new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f}))
          .addInt("score", 18)
          .addTimeStamp("when", Instant.now())
          .addText("winner", "Victor")
          .addSet("fighters", Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111"))));


        // Leveraging object mapping
        Game match1 = new Game()
                .matchId("mtch_1")
                .round(2)
                .score(20)
                .when(Instant.now())
                .winner("Victor")
                .fighters(Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111")))
                .vector(new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f}));
        db.getTable("games", Game.class).insertOne(match1);

        // The convertValue will serialize twice with $binary
        Row row = new RowSerializer().convertValue(match1, Row.class);
        System.out.println(row.toString());
    }

}

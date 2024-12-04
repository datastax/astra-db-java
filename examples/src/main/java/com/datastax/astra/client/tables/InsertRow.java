package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InsertRow {
  public static void main(String[] args) {
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   Table<Row> table = db.getTable("games");
   TableInsertOneResult result = table.insertOne(new Row()
     .addText("match_id", "mtch_0")
     .addInt("round", 1)
     .addVector("m_vector", new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f}))
     .addInt("score", 18)
     .addTimeStamp("when", Instant.now())
     .addText("winner", "Victor")
     .addSet("fighters", Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111"))));

   List<Object> youPk = result.getInsertedId();
   Map<String, ColumnDefinition> yourPkSchema = result.getPrimaryKeySchema();

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
  }
}

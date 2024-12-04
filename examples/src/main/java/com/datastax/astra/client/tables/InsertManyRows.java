package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InsertManyRows {
  public static void main(String[] args) {
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   Table<Row> table = db.getTable("games");

   TableInsertManyOptions options = new TableInsertManyOptions()
     .concurrency(10)
     .ordered(false)
     .chunkSize(3);

   List<Row> myRows = new ArrayList<>();
   myRows.add(new Row().addText("match_id", "fight4")
           .addInt("round", 1)
           .addText("winner", "Victor")
           .addInt("score", 18)
           .addTimeStamp("when", Instant.now())
           .addSet("fighters", Set.of(
                   UUID.fromString("0193539a-2770-8c09-a32a-111111111111"),
                   UUID.fromString("019353e3-00b4-83f9-a127-222222222222")))
           .addVector("m_vector", new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f})));
      myRows.add(new Row().addText("match_id", "fight5").addInt("round", 1).addText("winner", "Adam"));
      myRows.add(new Row().addText("match_id", "fight5").addInt("round", 2).addText("winner", "Betta"));
      myRows.add(new Row().addText("match_id", "fight5").addInt("round", 3).addText("winner", "Caio"));
      myRows.add(new Row().addText("match_id", "challenge6").addInt("round", 1).addText("winner", "Donna")
              .addVector("m_vector", new DataAPIVector(new float[]{0.9f, -0.1f, -0.3f})));
      myRows.add(new Row().addText("match_id", "challenge6").addInt("round", 2).addText("winner", "Erick"));
      myRows.add(new Row().addText("match_id", "challenge6").addInt("round", 3).addText("winner", "Fiona"));
      myRows.add(new Row().addText("match_id", "tournamentA").addInt("round", 1).addText("winner", "Gael"));
      myRows.add(new Row().addText("match_id", "tournamentA").addInt("round", 2).addText("winner", "Hanna"));
      myRows.add(new Row().addText("match_id", "tournamentA").addInt("round", 3).addText("winner", "Ian")
              .addSet("fighters", Set.of(UUID.fromString("0193539a-2770-8c09-a32a-111111111111"))));
      myRows.add(new Row().addText("match_id", "fight7").addInt("round", 1).addText("winner", "Joy"));
      myRows.add(new Row().addText("match_id", "fight7").addInt("round", 2).addText("winner", "Kevin"));
      myRows.add(new Row().addText("match_id", "fight7").addInt("round", 3).addText("winner", "Lauretta"));
   TableInsertManyResult results = table.insertMany(myRows, options);

   TableInsertManyResult results2 = table.insertMany(
     List.of(
       new Row().addText("match_id", "fight5").addInt("round", 1).addText("winner", "Adam0"),
       new Row().addText("match_id", "fight5").addInt("round", 2).addText("winner", "Betta0"),
       new Row().addText("match_id", "fight5").addInt("round", 3).addText("winner", "Caio0"),
       new Row().addText("match_id", "fight5").addInt("round", 1).addText("winner", "Adam Zuul"),
       new Row().addText("match_id", "fight5").addInt("round", 2).addText("winner", "Betta Vigo"),
       new Row().addText("match_id", "fight5").addInt("round", 3).addText("winner", "Caio Gozer")),
   new TableInsertManyOptions().ordered(true));

  }
}

/*
Perform an ordered insertion (which would stop on the first failure):

        my_table.insert_many(
    [
{"match_id": "fight5", "round": 1, "winner": "Adam0"},
        {"match_id": "fight5", "round": 2, "winner": "Betta0"},
        {"match_id": "fight5", "round": 3, "winner": "Caio0"},
        {"match_id": "fight5", "round": 1, "winner": "Adam Zuul"},
        {"match_id": "fight5", "round": 2, "winner": "Betta Vigo"},
        {"match_id": "fight5", "round": 3, "winner": "Caio Gozer"},
        ],
ordered=True,
        )*/
package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.serdes.tables.RowSerializer;

import java.util.Optional;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gt;
import static com.datastax.astra.client.core.query.Projection.include;

public class FindOne {
 public static void main(String[] args) {
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

   Table<Row> table = db.getTable("games");

   Filter filter = and(
     eq("match_id", "mtch_0"),
     gt("round", 1),
     eq("winner", "Victor"));

   TableFindOneOptions options = new TableFindOneOptions()
     // .projection(include("match_id", "winner", "field3"))
     .sort(Sort.vector("m_vector", new DataAPIVector(new float[] {0.4f, -0.6f, 0.2f})))
     .includeSimilarity(true);

   Optional<Row> row = table.findOne(filter, options);
   row.ifPresent(r -> {
     System.out.println("Row: " + r);
     DataAPIVector v = r.getVector("m_vector");
     System.out.println(r.getInstant("when"));
   });

   Table<Game> tableGame = db.getTable("games", Game.class);
   Optional<Game> row2 = tableGame.findOne(filter, options);
     row2.ifPresent(game -> {
         System.out.println("game: " + game.getVector());
         System.out.println(game.getFighters());
         System.out.println(game.getMatchId());
   });
 }
}
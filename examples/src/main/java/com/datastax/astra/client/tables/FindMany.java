package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.cursor.TableCursor;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.List;
import java.util.Optional;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gt;
import static com.datastax.astra.client.core.query.Projection.include;

public class FindMany {
 public static void main(String[] args) {
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

   Table<Row> table = db.getTable("games");

   Filter filter = eq("match_id", "tournamentA");

   TableFindOptions options = new TableFindOptions()
     // .projection(include("match_id", "winner", "field3"))
       .limit(2)
     //.sort(Sort.vector("m_vector", new DataAPIVector(new float[] {0.4f, -0.6f, 0.2f})))
     .includeSortVector(true)
     .includeSimilarity(true);

   TableCursor<Row, Row> row = table.find(filter, options);
    row.forEach(r -> {
      System.out.println("Row: " + r);
    });

     TableCursor<Row, Game> gameCursor = table.find(filter, options, Game.class);
     gameCursor.forEach(g -> {
         System.out.println("Game: " + g.getWinner());
     });


   TableCursor<Row, Row> row2 = table.find(eq("match_id", "tournamentA"));
   row2.getSortVector().ifPresent(v -> {
       System.out.println("Sort Vector: " + v);
   });

   Filter filter3 = eq("match_id", "fight4");
   TableFindOptions options3 = new TableFindOptions()
   .projection(include("winner"))
   .sort(Sort.vector("m_vector", new float[] {0.2f, 0.3f, 0.4f}));
    List<Row> result = table.find(filter3, options3).toList();





 }
}

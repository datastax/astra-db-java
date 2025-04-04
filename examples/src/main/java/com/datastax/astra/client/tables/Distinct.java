package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableDistinctOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.List;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Projection.include;

public class Distinct {
 public static void main(String[] args) {
   Database db = new DataAPIClient("token").getDatabase("endpoint");

   Table<Row> table = db.getTable("games");

   // Show you all match id in the table
   List<Row> matches = table.find(null, new TableFindOptions()
      .projection(include("match_id"))).toList();
     matches.forEach(System.out::println);

     Filter filter = Filters.eq("match_id", "challenge6");

     TableDistinctOptions options = new TableDistinctOptions()
             .timeout(1000);
    // Show you the distinct match id in the table
     table.distinct("match_id", filter, String.class)
             .forEach(System.out::println);
 }
}

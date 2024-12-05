package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableDeleteManyOptions;
import com.datastax.astra.client.tables.commands.options.TableDeleteOneOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gte;

public class DeleteMany {
 public static void main(String[] args) {
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

  Table<Row> tableRow = db.getTable("games");

  // Update
  Filter filter = and(
    eq("match_id", "fight7"),
    eq("round", 2));

  tableRow.deleteMany(filter);
  tableRow.deleteMany(filter, new TableDeleteManyOptions()
          .timeout(1000));

  Filter filter2 = and(
          eq("match_id", "fight5"),
          gte("round", 5));
  tableRow.deleteMany(filter2);

 }

}

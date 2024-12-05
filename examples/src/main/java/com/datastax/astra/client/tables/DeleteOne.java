package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.TableDeleteOneOptions;
import com.datastax.astra.client.tables.commands.options.TableUpdateOneOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.Set;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;

public class DeleteOne {
 public static void main(String[] args) {
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

  Table<Row> tableRow = db.getTable("games");

  // Update
  Filter filter = and(
    eq("match_id", "fight7"),
    eq("round", 2));

  tableRow.deleteOne(filter);
  tableRow.deleteOne(filter, new TableDeleteOneOptions()
          .timeout(1000));

 }

}

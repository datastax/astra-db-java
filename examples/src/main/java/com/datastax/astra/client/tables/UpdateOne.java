package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableUpdateOneOptions;
import com.datastax.astra.client.tables.commands.results.TableUpdateResult;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.mapping.Column;
import lombok.Data;

import java.util.Optional;
import java.util.Set;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gt;

public class UpdateOne {
 public static void main(String[] args) {
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

  Table<Row> tableRow = db.getTable("games");

  // Update
  Filter filter = and(
    eq("match_id", "fight4"),
    eq("round", 1));
  new TableUpdateOneOptions()
    .timeout(1000);
  tableRow.updateOne(filter, new TableUpdateOperation()
    .set("winner", "Winona"));

  // Unset 2 columns
  tableRow.updateOne(filter, new TableUpdateOperation()
    .set("winner", "Winona"));

  // Set a new value for a column
  tableRow.updateOne(and(
    eq("match_id", "fight4"),
    eq("round", 1)),
  new TableUpdateOperation().set("winner", "Winona"));

  // Set a new value for a column while unsetting another colum
  tableRow.updateOne(and(
    eq("match_id", "fight4"),
    eq("round", 1)),
  new TableUpdateOperation()
    .set("score", 24)
    .unset("winner")
    .unset("fighters", Set.of()));
 }

}

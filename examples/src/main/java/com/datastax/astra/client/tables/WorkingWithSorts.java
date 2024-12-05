package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

public class WorkingWithSorts {
    public static void main(String[] args) {
        Database db = DataAPIClients.localDbWithDefaultKeyspace();
        // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);

        Table<Row> tableRow = db.getTable("TABLE_NAME");

        // Sort Clause for a vector
        Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f});;

        // Sort Clause for other fields
        Sort s1 = Sort.ascending("field1");
        Sort s2 = Sort.descending("field2");

        // Build the sort clause
        new TableFindOptions().sort(s1, s2);

        // Adding vector
        new TableFindOptions().sort(
                Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}), s1, s2);

    }
}

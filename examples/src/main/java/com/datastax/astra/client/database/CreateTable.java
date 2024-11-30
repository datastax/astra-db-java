package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static java.time.Duration.ofSeconds;

public class CreateTable {

    public static void main(String[] args) {

        // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
        Database db = DataAPIClients.localDbWithDefaultKeyspace();

        // Definition of the table in fluent style
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new ColumnDefinitionVector().dimension(3).metric(COSINE))
                .addColumn("score", ColumnTypes.INT)
                .addColumn("when",  ColumnTypes.TIMESTAMP)
                .addColumn("winner",  ColumnTypes.TEXT)
                .addColumnSet("fighters", ColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(ascending("round"));

        // Minimal creation
        Table<Row> table1 = db.createTable("game1", tableDefinition);

        // Minimal Creation with a Bean
        Table<Game> table2 = db.createTable("game2", tableDefinition, Game.class);

        // -- options --

        // One can add options to setup the creation with finer grained:
        CreateTableOptions createTableOptions = new CreateTableOptions()
                .ifNotExists(true)
                .timeout(ofSeconds(5));
        Table<Row> table3 = db.createTable("game3", tableDefinition, createTableOptions);

        // One can can tuned the table object returned by the function
        TableOptions tableOptions = new TableOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"))
                .timeout(ofSeconds(5));

        // Change the Type of objects in use instead of default Row
        Table<Row> table4 = db.createTable("game4", tableDefinition,Row.class,
                createTableOptions, tableOptions);
    }
}

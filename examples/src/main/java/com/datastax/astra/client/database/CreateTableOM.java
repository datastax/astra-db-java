package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.ddl.CreateTableOptions;
import com.datastax.astra.client.tables.row.Row;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static java.time.Duration.ofSeconds;

public class CreateTableOM {

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

        // -- options --

        // Add Options on the creation of creation
        CreateTableOptions createTableOptions = new CreateTableOptions()
                .ifNotExists(true)
                .timeout(ofSeconds(5));

        // Add Options on the initialization of the object
        TableOptions tableOptions = new TableOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"))
                .timeout(ofSeconds(5));

        // Change the Type of objects in use instead of default Row
        Table<Row> table2 = db.createTable("game2", tableDefinition,
                createTableOptions, Row.class, tableOptions);

        // -- object Mapping --
        Class<Game> gameClass = Game.class;
        Table<Game> table3 = db.createTable("game3", tableDefinition,
                createTableOptions, gameClass, tableOptions);
        
        // Assuming you mapped your object with annotations

    }
}

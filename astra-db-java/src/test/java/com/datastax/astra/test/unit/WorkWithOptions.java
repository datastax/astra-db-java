package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.options.CreateCollectionOptions;
import com.datastax.astra.client.databases.options.DropCollectionOptions;
import com.datastax.astra.client.databases.options.ListCollectionOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.ddl.CreateTableOptions;
import com.datastax.astra.client.tables.row.Row;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

public class WorkWithOptions {

    @Test
    public void shouldListCollection() {

        new ListCollectionOptions()
          .timeout(Duration.ofMillis(1000));

        new CollectionOptions()
          .timeout(Duration.ofMillis(1000))
          .dataAPIClientOptions(new DataAPIClientOptions())
          .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"));

        new CreateCollectionOptions()
            .commandType(CommandType.COLLECTION_ADMIN)
            .timeout(Duration.ofMillis(1000));

        new DropCollectionOptions()
          .timeout(Duration.ofMillis(1000));

        Database db = new DataAPIClient("token").getDatabase("db-endpoint");

        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new ColumnDefinitionVector().dimension(3).metric(COSINE))
                .addColumn("score", ColumnTypes.INT)
                .addColumn("when",  ColumnTypes.TIMESTAMP)
                .addColumn("winner",  ColumnTypes.TEXT)
                .addColumnSet("fighters", ColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(Sort.ascending("round"));

        // Optional
        CreateTableOptions createTableOptions =
                new CreateTableOptions().timeout(Duration.ofMillis(1000));

        // Optional to override spawn options
        TableOptions tableOptions =
                new TableOptions().timeout(Duration.ofMillis(1000));

        Table<Row> tableSimple  = db.createTable("TABLE_SIMPLE", tableDefinition);

    }

}

package com.datastax.astra.test.unit.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.commands.options.DropCollectionOptions;
import com.datastax.astra.client.collections.commands.options.ListCollectionOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for options building across the SDK.
 */
class WorkWithOptions {

    @Test
    void shouldBuildListCollectionOptions() {
        ListCollectionOptions opts = new ListCollectionOptions()
                .timeout(Duration.ofMillis(1000));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldBuildCollectionOptions() {
        CollectionOptions opts = new CollectionOptions()
                .timeout(Duration.ofMillis(1000))
                .dataAPIClientOptions(new DataAPIClientOptions())
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldBuildCreateCollectionOptions() {
        CreateCollectionOptions opts = new CreateCollectionOptions()
                .commandType(CommandType.COLLECTION_ADMIN)
                .timeout(Duration.ofMillis(1000));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldBuildDropCollectionOptions() {
        DropCollectionOptions opts = new DropCollectionOptions()
                .timeout(Duration.ofMillis(1000));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldBuildTableDefinitionWithOptions() {
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new TableColumnDefinitionVector().dimension(3).metric(COSINE))
                .addColumn("score", TableColumnTypes.INT)
                .addColumn("when", TableColumnTypes.TIMESTAMP)
                .addColumn("winner", TableColumnTypes.TEXT)
                .addColumnSet("fighters", TableColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(Sort.ascending("round"));

        assertThat(tableDefinition.getColumns()).hasSize(7);
        assertThat(tableDefinition.getPrimaryKey().getPartitionBy()).containsExactly("match_id");
        assertThat(tableDefinition.getPrimaryKey().getPartitionSort()).containsKey("round");
    }

    @Test
    void shouldBuildCreateTableOptions() {
        CreateTableOptions opts = new CreateTableOptions().timeout(Duration.ofMillis(1000));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldBuildTableOptions() {
        TableOptions opts = new TableOptions().timeout(Duration.ofMillis(1000));
        assertThat(opts).isNotNull();
    }

    @Test
    void shouldCreateDatabase() {
        Database db = new DataAPIClient("token").getDatabase("db-endpoint");
        assertThat(db).isNotNull();
    }
}

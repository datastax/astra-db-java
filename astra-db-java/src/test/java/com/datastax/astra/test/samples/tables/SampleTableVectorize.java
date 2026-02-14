package com.datastax.astra.test.samples.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.vector.SimilarityMetric.DOT_PRODUCT;
import static com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition.SOURCE_MODEL_OPENAI_V3_SMALL;

/**
 * Table-level vectorize end-to-end: {@link VectorServiceOptions},
 * {@link TableColumnDefinitionVector}, {@link CreateTableOptions#embeddingApiKey(String)},
 * named-column {@link Sort#vectorize(String, String)}.
 *
 * @see Table
 * @see TableColumnDefinitionVector
 */
@SuppressWarnings("unused")
public class SampleTableVectorize {

    static void tableVectorizeEndToEnd() {
        Database db = new DataAPIClient("TOKEN").getDatabase("API_ENDPOINT");

        // Define a vector column with vectorize service
        VectorServiceOptions openAI = new VectorServiceOptions()
                .provider("openai")
                .modelName("text-embedding-3-small");
        TableColumnDefinitionVector vectorDef = new TableColumnDefinitionVector()
                .dimension(512)
                .metric(DOT_PRODUCT)
                .service(openAI);
        TableDefinition miniGame = new TableDefinition()
                .addColumnText("game_id").addPartitionBy("game_id")
                .addColumnVector("m_vector", vectorDef);

        // Create table with embedding API key in operation headers
        Table<Row> tableMiniGame = db.createTable("mini_game", miniGame, new CreateTableOptions()
                .embeddingApiKey("OPENAI_API_KEY")
                .ifNotExists(true));

        // Create a vector index
        TableVectorIndexDefinition idxDef = new TableVectorIndexDefinition()
                .column("m_vector")
                .metric(DOT_PRODUCT)
                .sourceModel(SOURCE_MODEL_OPENAI_V3_SMALL);
        tableMiniGame.createVectorIndex("m_vector_index_mini", idxDef,
                CreateVectorIndexOptions.IF_NOT_EXISTS);

        // Insert with vectorize
        tableMiniGame.insertOne(new Row()
                .addText("game_id", "game1")
                .addVectorize("m_vector", "Text To Serialize"));

        // Find with vectorize sort
        TableFindOptions options = new TableFindOptions()
                .sort(Sort.vectorize("m_vector", "Text To Serialize"));
        tableMiniGame.find(new Filter(), options).toList();

        // Alternative sort options
        DataAPIVector vector = new DataAPIVector(new float[] {0.1f, 0.2f, 0.3f});
        new TableFindOptions().sort(Sort.vector("m_vector", vector));
        new TableFindOptions().sort(Sort.vector("m_vector", new float[] {0.1f, 0.2f, 0.3f}));
        new TableFindOptions().sort(Sort.vectorize("m_vector", "Text To Serialize"));

        // FindOne with vector sort
        tableMiniGame.findOne(new TableFindOneOptions()
                .sort(Sort.vector("m_vector", new DataAPIVector(new float[] {0.2f, 0.3f, 0.4f}))));
    }
}

package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.vector.SimilarityMetric.DOT_PRODUCT;
import static com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition.SOURCE_MODEL_OPENAI_V3_SMALL;

public class InsertOneWithVectorize {
    public static void main(String[] args) {
        // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
        Database db = DataAPIClients.localDbWithDefaultKeyspace();

        // Create a table with a vector column (+vectorize)
        VectorServiceOptions openAI = new VectorServiceOptions()
                .provider("openai")
                .modelName("text-embedding-3-small");
        ColumnDefinitionVector vectorDef = new ColumnDefinitionVector()
                .dimension(512)
                .metric(DOT_PRODUCT)
                .service(openAI); // no provider key we use the header
        TableDefinition miniGame = new TableDefinition()
                .addColumnText("game_id").addPartitionBy("game_id")
                .addColumnVector("m_vector", vectorDef);

        // Creating the Table and set the openAI API Key in the operation headers
        Table<Row> tableMiniGame = db.createTable("mini_game", miniGame, new CreateTableOptions()
                .embeddingApiKey(System.getenv("OPENAI_API_KEY"))
                .ifNotExists(true));

        // Create the vector Index
        TableVectorIndexDefinition idxDef = new TableVectorIndexDefinition() // no provider key we use the header
                .column("m_vector")
                .metric(DOT_PRODUCT)
                .sourceModel(SOURCE_MODEL_OPENAI_V3_SMALL);
        tableMiniGame.createVectorIndex("m_vector_index", idxDef, CreateVectorIndexOptions.IF_NOT_EXISTS);

        tableMiniGame.insertOne(new Row()
                // We need a primary Key
                .addText("game_id", "game1")
                // Add a vector with Recommended DataAPIVector Wrapper
                .addVectorize("m_vector", "Text To Serialize"));

        //tableMiniGame.listIndexes().forEach(System.out::println);
        tableMiniGame
                .findOne(Filters.eq("game_id", "game1"))
                .ifPresent(System.out::println);
    }
}

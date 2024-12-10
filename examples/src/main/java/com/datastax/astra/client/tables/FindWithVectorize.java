package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.vector.SimilarityMetric.DOT_PRODUCT;
import static com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition.SOURCE_MODEL_OPENAI_V3_SMALL;

public class FindWithVectorize {
    public static void main(String[] args) {
        // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
        Database db = new DataAPIClient("token").getDatabase("endpoint");

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
        tableMiniGame.createVectorIndex("m_vector_index_mini", idxDef,
                CreateVectorIndexOptions.IF_NOT_EXISTS);

        tableMiniGame.insertOne(new Row()
                // We need a primary Key
                .addText("game_id", "game1")
                // Add a vector with Recommended DataAPIVector Wrapper
                .addVectorize("m_vector", "Text To Serialize"));

        //tableMiniGame.listIndexes().forEach(System.out::println);
        //tableMiniGame
        //        .findOne(Filters.eq("game_id", "game1"))
        //        .ifPresent(System.out::println);

        TableFindOptions options = new TableFindOptions()
                .sort(Sort.vectorize("m_vector", "Text To Serialize"));

        // Sort with DatAPIVector
        DataAPIVector vector2 = new DataAPIVector(new float[] {0.1f, 0.2f, 0.3f});
        new TableFindOptions().sort(Sort.vector("m_vector", vector2));
        // Sort with float[]
        new TableFindOptions().sort(Sort.vector("m_vector", new float[] {0.1f, 0.2f, 0.3f}));
        // Sort with Vectorize
        new TableFindOptions().sort(Sort.vectorize("m_vector", "Text To Serialize"));


        //my_table.find_one({}, sort={"m_vector": DataAPIVector([0.2, 0.3, 0.4])})
        DataAPIVector vector = new DataAPIVector(new float[] {0.2f, 0.3f, 0.4f});
        tableMiniGame.findOne( new TableFindOneOptions().sort(Sort.vector("m_vector", vector)));

        tableMiniGame.findOne(Filters.eq("winner", "Caio Gozer"));


        new Filter().where("winner").isEqualsTo("Caio Gozer");
        System.out.println(tableMiniGame.find(new Filter(), options).toList());



    }

}

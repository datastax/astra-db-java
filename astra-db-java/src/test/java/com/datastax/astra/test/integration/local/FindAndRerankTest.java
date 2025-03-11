package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.Lexical;
import com.datastax.astra.client.core.reranking.RerankingServiceOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.core.vectorize.Vectorize;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableAddReranking;
import com.datastax.astra.client.tables.commands.AlterTableAddVectorize;
import com.datastax.astra.client.tables.commands.options.CreateTextIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.definition.indexes.TableTextIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;
import static com.datastax.astra.client.core.vector.SimilarityMetric.DOT_PRODUCT;

public class FindAndRerankTest {

    static Database database;

    protected static Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Test
    @Order(1)
    public void should_find_and_rerank_withCollection() {
        Database db = getDatabase();

        // DDL - Create Collection
        Collection<Document> collection = db.createCollection("collection_rerank",
                new CollectionDefinition()
                        .vector(1536, DOT_PRODUCT)
                        .lexical(new Analyzer(STANDARD)) // Enabled is position by the client
                        .reranking("bm25"),       // Reranking Options with only the provider,
                new CreateCollectionOptions().timeout(10000));

        // INSERTING - SAME TEXTS
        // {
        //	 "_id": "1"
        //	 "$hybrid" : "vectorize and bm25 this text pls",
        // }
        Document doc1 =  new Document(1)
                .hybrid("vectorize and bm25 this text pls");

        //
        // {
        //   "_id": "1"
        //   "$vectorize" : "vectorize and bm25 this text pls",
        //   "$lexical" : "vectorize and bm25 this text pls",
        // }
        Document doc2 =  new Document(2)
                .vectorize("vectorize and bm25 this text pls")
                .lexical("vectorize and bm25 this text pls");

        // INSERTING - DIFFERENT TEXTS
        // $hybrid as an object, that sets both the $vectorize and $lexical as diff fields
        // {
        //   "_id": "1"
        //   "$hybrid": {
        //      "$vectorize": "i like cheese",
        //      "$lexical" : "cheese"
        //    }
        // }
        Document doc3 =  new Document(3)
                .hybrid( "i like cheese",  "cheese");


        // READ
        

    }

    @Test
    @Order(2)
    public void should_find_and_rerank_withTable() {
        Database db = getDatabase();

        // DDL - Create Table
        Table<Row> table = db.getTable("rerank_table");

        // Create text index
        table.createTextIndex("name_of_index_keyspace_unique",
           new TableTextIndexDefinition().column("text_column").analyzer(STANDARD),
           CreateTextIndexOptions.IF_NOT_EXISTS);

        // Create vector index
        table.createVectorIndex("idx_vector_column",
           new TableVectorIndexDefinition().column("vector_column").metric(DOT_PRODUCT),
           CreateVectorIndexOptions.IF_NOT_EXISTS);

        // Add vectorize
        table.alter(new AlterTableAddVectorize().columns(
                Map.of("vector_column", new VectorServiceOptions()
                        .modelName("text-embedding-3-small")
                        .provider("openai")
                        )));

        // Add reranking
        table.alter(new AlterTableAddReranking().columns(
                Map.of("vector_column", new RerankingServiceOptions()
                        .modelName("bm25")
                )));

        // INSERTING - SAME TEXTS
        // {
        //	 "_id": "1"
        //	 "$hybrid" : "vectorize and bm25 this text pls",
        // }
        Row row1 = new Row();
        row1.addHybrid("vectorize and bm25 this text pls");

        // INSERTING - DIFFERENT TEXTS AND/OR STORING THE VECTORIZE PASSAGE
        // $hybrid as an object, that sets both the vectorize and lexical as diff fields
        // below will vectorize "I like cheese" into all vectorized vectors and
        // store "cheese" in the one and only one analysed text index column
        // {
        //    "_id": "1"
        //    "$hybrid": {
        //      "$vectorize": "i like cheese",
        //      "$lexical" : "cheese"
        //     }
        // }
        Row row2 = new Row();
        row2.addHybrid(new Hybrid("i like cheese", "cheese"));

        // IF I want to store the vectorize text, I use the object vectorize value
        // {
        //   "_id": "1"
        //   "$hybrid": {
        //     "$vectorize": {
        //       "$passage" : "i like cheese"
        //       "$setPassage: "vectorize_passage_col"
        //     },
        //     "$lexical" : "cheese"
        //    }
        // }
        Row row3 = new Row();
        Vectorize v = new Vectorize("i like cheese", "vectorize_passage_col");
        Lexical l = new Lexical("cheese");
        row3.addHybrid(new Hybrid(v, l));
    }

}

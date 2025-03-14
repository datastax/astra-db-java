package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.hybrid.HybridProjection;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.AnalyzerTypes;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.reranking.RerankResult;
import com.datastax.astra.client.core.reranking.RerankingOptions;
import com.datastax.astra.client.core.reranking.RerankingServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

public class LocalCollectionFindAndRerankITTest {

    protected AstraEnvironment getAstraEnvironment() { return null; }

    protected CloudProviderType getCloudProvider() { return null; }

    protected String getRegion() { return "";}

    /**
     * Initialization of the DB for local developments
     */
    protected static Database database;

    protected Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Test
    public void should_create_collection_reranking() {
        Database db = getDatabase();
        db.listCollectionNames().forEach(db::dropCollection);

        // Expected to fail
        getDatabase().createCollection("c_reranking",
            new CollectionDefinition().reranking(
                 new RerankingOptions().service(
                         new RerankingServiceOptions().modelName("bm25"))));
    }

    // --
    @Test
    public void should_create_collection_lexical() {
        Database db = getDatabase();
        db.listCollectionNames().forEach(db::dropCollection);

        // c_simple
        getDatabase().createCollection("c_simple");

        // c_lexical_standard
        Analyzer standardAnalyzer = new Analyzer(STANDARD);
        getDatabase().createCollection("c_lexical_standard", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(standardAnalyzer)));

        // c_lexical_custom
        Analyzer analyzer = new Analyzer().tokenizer(AnalyzerTypes.WHITESPACE.getValue());
        getDatabase().createCollection("c_lexical_custom", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(analyzer)));

        Analyzer analyzer2 = new Analyzer()
                .tokenizer(AnalyzerTypes.KEYWORD.getValue())
                .addFilter("synonym", Map.of("synonyms", "Alex, alex, Alexander, alexander => Alex"));
        getDatabase().createCollection("c_lexical_custom2", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(analyzer2)));

        getDatabase().createCollection("c_lexical_false", new CollectionDefinition()
                .lexical(new LexicalOptions().enabled(false).analyzer(new Analyzer(AnalyzerTypes.LETTER))));
    }

    static final DocumentSerializer SERIALIZER = new DocumentSerializer();

    @Test
    public void should_serialize_hybrid() {
        // INSERTING - SAME TEXTS
        // {
        //	 "_id": "1"
        //	 "$hybrid" : "vectorize and bm25 this text pls",
        // }
        Document doc1 =  new Document(1)
                .hybrid("text");
        Assertions.assertEquals("{\"_id\":1,\"$hybrid\":{\"$vectorize\":\"text\",\"$lexical\":\"text\"}}",
                SERIALIZER.marshall(doc1));

        //
        // {
        //   "_id": "1"
        //   "$vectorize" : "vectorize and bm25 this text pls",
        //   "$lexical" : "vectorize and bm25 this text pls",
        // }
        Document doc2 = new Document(2)
                .vectorize("vvv")
                .lexical("lll");
        Assertions.assertEquals("{\"_id\":2,\"$vectorize\":\"vvv\",\"$lexical\":\"lll\"}",
                SERIALIZER.marshall(doc2));

        // INSERTING - DIFFERENT TEXTS
        // $hybrid as an object, that sets both the $vectorize and $lexical as diff fields
        // {
        //   "_id": "1"
        //   "$hybrid": {
        //      "$vectorize": "i like cheese",
        //      "$lexical" : "cheese"
        //    }
        // }
        Document doc3 =  new Document(3).hybrid( "vvv",  "lll");
        Assertions.assertEquals("{\"_id\":3,\"$hybrid\":{\"$vectorize\":\"vvv\",\"$lexical\":\"lll\"}}",
                SERIALIZER.marshall(doc3));
    }

    @Test
    public void should_insertMany_returnDocumentResponses() {
        Document doc1 = new Document().id(1).append("a", "a").append("b", "c");
        Document doc2 = new Document().id(2).append("a", "a").append("b", "b");
        CollectionInsertManyOptions options = new CollectionInsertManyOptions();
        options.returnDocumentResponses(true);
        getDatabase().createCollection("c_simple");
        getDatabase().getCollection("c_simple").deleteAll();
        getDatabase().getCollection("c_simple").insertMany(List.of(doc1, doc2), options);
    }

    @Test
    public void should_create_farr_request() {
        Database db = getDatabase();

        Collection<Document> myCol = db.getCollection("c_farr");

        CollectionFindAndRerankOptions farrOptions = new CollectionFindAndRerankOptions()
                .sort(Sort.hybrid(new Hybrid("text")))
                .projection(Projection.include("$vectorize"))
                // Samples
                .limit(10)
                //.hybridLimits(10)
                .hybridLimits(Map.of("$vectorize", 100, "$lexical", 10))
                .hybridProjection(HybridProjection.SCORES)
                .rerankOn("content");

        CollectionFindAndRerankCursor<Document, Document> cursor = myCol
                .findAndRerank(Filters.eq("tenant", "aaa"), farrOptions);
        for (RerankResult<Document> doc : cursor) {
            System.out.println("---- result ----");
            System.out.println(doc.getDocument());
            System.out.println(doc.getScores());
        }
    }


}

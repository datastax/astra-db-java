package com.datastax.astra.test.integration;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.auth.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public abstract class AbstractVectorizeITTest extends AbstractDataAPITest {

    public static String getApiKey(String provider) {
        if (provider.equals("openai")) {
            return System.getenv("OPENAI_API_KEY");
        } else if (provider.equals("huggingface")) {
            return System.getenv("HF_API_KEY");
        } else if (provider.equals("azureOpenAI")) {
            return System.getenv("AZURE_OPENAI_API_KEY");
        } else if (provider.equals("voyageAI")) {
            return System.getenv("VOYAGE_API_KEY");
        } else if (provider.equals("jinaAI")) {
            return System.getenv("JINA_API_KEY");
        } else if (provider.equals("mistral")) {
            return System.getenv("MISTRAL_API_KEY");
        } else if (provider.equals("upstageAI")) {
            return System.getenv("UPSTAGE_API_KEY");
        } else if (provider.equals("cohere")) {
            return System.getenv("COHERE_API_KEY");
        } else if (provider.equals("vertexAI")) {
            return System.getenv("VERTEX_API_KEY");
        }
        return null;
    }

    public Map<String, Object> getParameters(String provider) {
        if (provider.equals("azureOpenAI")) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("deploymentId", "text-embedding-3-small-steo");
            parameters.put("resourceName", "steo-azure-openai");
            return parameters;
        }
        return null;
    }

    protected void dropCollection(String name) {
        getDatabase().dropCollection(name);
        log.info("Collection {} dropped", name);
    }

    protected void dropAllCollections() {
        getDatabase().listCollections().forEach(collection -> {
            getDatabase().dropCollection(collection.getName());
            log.info("Collection {} dropped", collection.getName());
        });
    }

    public void testEmbeddingProvider(String key, EmbeddingProvider provider) {
        System.out.println("TESTING PROVIDER [" + key + "]");
        String apiKey              = getApiKey(key);
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);
                Collection<Document> collection = createCollectionHeader(key, model, apiKey, params);
                log.info("Collection created {}", collection.getCollectionName());
                testCollection(collection, new EmbeddingAPIKeyHeaderProvider(apiKey));
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }

    private void testEmbeddingProviderSharedKey(String key, EmbeddingProvider provider, String keyName) {
        System.out.println("TESTING PROVIDER [" + key + "]");
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);
                Collection<Document> collection = createCollectionSharedSecret(key, model, keyName, params);
                log.info("Collection created {}", collection.getCollectionName());
                testCollectionSharedKey(collection);
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }

    protected void testCollectionSharedKey(Collection<Document> collection) {
        List<Document> entries = List.of(
                new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
                new Document(3).vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).vectorize("He's underneath the window"),
                new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).vectorize("Anyway, what you gonna do about it?")
        );

        // Ingestion
        CollectionInsertManyResult res = collection.insertMany(entries);
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vectorize("You shouldn't come around here singing up at people like that"))
                        .includeSortVector(true)
                        .includeSimilarity(true));
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);

        CollectionFindCursor<Document, Document> docs= collection.find(new CollectionFindOptions()
                .sort(Sort.vectorize("You shouldn't come around here singing up at people like that"))
                .includeSortVector(true)
                .includeSimilarity(true));
        assertThat(docs.iterator().next()).isNotNull();
        assertThat(docs.getSortVector().isPresent()).isTrue();
    }

    protected void testCollection(Collection<Document> collection, EmbeddingHeadersProvider authProvider) {
        List<Document> entries = List.of(
                new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
                new Document(3).vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).vectorize("He's underneath the window"),
                new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).vectorize("Anyway, what you gonna do about it?")
        );

        // Ingestion
        CollectionInsertManyResult res = collection.insertMany(entries, new CollectionInsertManyOptions().embeddingAuthProvider(authProvider));
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new CollectionFindOneOptions()
                        .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                        .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .embeddingAuthProvider(authProvider)
                        .includeSimilarity(true));
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
    }
   
    // ===================================================================================
    // ===================================================================================
    // ===================================================================================

    protected Collection<Document> createCollectionHeader(String provider, EmbeddingProvider.Model model, String apiKey, Map<String, Object> parameters) {
        String collectionName = getCollectionNameFromModel(model.getName());

        CollectionDefinition colDef =  new CollectionDefinition();
        colDef.vectorSimilarity(SimilarityMetric.COSINE);
        if (model.getVectorDimension() != null) {
            colDef.vectorDimension(model.getVectorDimension());
        }
        colDef.vectorize(provider, model.getName(), null, parameters);

        CreateCollectionOptions ccOptions = new CreateCollectionOptions();

        CollectionOptions colOptions = new CollectionOptions();
        colOptions.embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(apiKey));

        return getDatabase().createCollection(collectionName, colDef, ccOptions);
    }

    private String getCollectionNameFromModel(String modelName) {
        System.out.println("MODEL NAME: " + modelName);
        String name=  modelName.toLowerCase()
                .replaceAll("-", "_")
                .replaceAll("/", "_")
                .replaceAll("\\.", "");
        // Truncate
        name = name.substring(0,Math.min(name.length(), 25));
        System.out.println("Collection NAME: " + name);
        return name;
    }

    private Collection<Document> createCollectionSharedSecret(String provider, EmbeddingProvider.Model model, String keyName, Map<String, Object> parameters) {
        CollectionDefinition cd = new CollectionDefinition().vectorSimilarity(SimilarityMetric.COSINE);
        if (model.getVectorDimension() != null) {
            cd.vectorDimension(model.getVectorDimension());
        }
        cd.vectorize(provider, model.getName(), keyName, parameters);
        String collectionName = getCollectionNameFromModel(model.getName());
        return getDatabase().createCollection(collectionName, cd, Document.class);
    }

    protected void testEmbeddingModelSharedSecret(String provider, EmbeddingProvider.Model model, String keyName, Map<String, Object> parameters) {
        log.info("Testing model {}", model);
        Collection<Document> collection = createCollectionSharedSecret(provider, model, keyName, parameters);
        log.info("Collection created {}", collection.getCollectionName());
        testCollection(collection, null);
    }

    public void shouldTestOneProvider(String provider) {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            if (entry.getKey().equals(provider)) {
                this.testEmbeddingProvider(entry.getKey(), entry.getValue());
            }
        }
    }

    public void shouldTestOneProvider(String provider, String embeddingApiKey) {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            if (entry.getKey().equals(provider)) {
                this.testEmbeddingProvider(entry.getKey(), entry.getValue(), embeddingApiKey);
            }
        }
    }

    public void shouldTestOneProviderSharedKey(String provider, String keyName) {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            if (entry.getKey().equals(provider)) {
                this.testEmbeddingProviderSharedKey(entry.getKey(), entry.getValue(), keyName);
            }
        }
    }

    public void testEmbeddingProvider(String key, EmbeddingProvider provider, String apiKey) {
        System.out.println("TESTING PROVIDER [" + key + "]");
        Map<String, Object> params = getParameters(key);
        final String targetApiKey = ("nvidia".equals(key)) ? null : apiKey;
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);
                Collection<Document> collection = createCollectionHeader(key, model, targetApiKey, params);
                log.info("Collection created {}", collection.getCollectionName());
                testCollection(collection, new EmbeddingAPIKeyHeaderProvider(targetApiKey));
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }

}

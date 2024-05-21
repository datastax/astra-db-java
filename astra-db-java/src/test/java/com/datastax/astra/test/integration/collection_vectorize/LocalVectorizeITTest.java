package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_ADA002;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_LARGE;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_SMALL;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class LocalVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return DataAPIClients.createDefaultLocalDatabase();
    }





}

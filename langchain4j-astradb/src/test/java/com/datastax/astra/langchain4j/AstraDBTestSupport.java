package com.datastax.astra.langchain4j;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test support for AstraDB
 */
@Slf4j
public class AstraDBTestSupport {

    public static final String ASTRA_TOKEN       = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String OPENAI_API_KEY    = System.getenv("OPENAI_API_KEY");
    public static final String TEST_DB           = "test_langchain4j";
    public static final String TEST_DB_vectorize = "test_langchain4j_vectorize";

    /**
     * Database Creation
     *
     * @param dbName
     *      unique db name
     * @return
     *      database object
     */
    public static Database createDbIfNotExist(String dbName) {
        log.info("[init] - Create Database if not exists name={}", dbName);
        DataAPIClient client = new DataAPIClient(getAstraToken());
        AstraDBAdmin astraDBAdmin = client.getAdmin();
        AstraDBDatabaseAdmin databaseAdmin = (AstraDBDatabaseAdmin) astraDBAdmin
                .createDatabase(dbName, CloudProviderType.AWS, "us-east-2");
        UUID dbId = UUID.fromString(databaseAdmin.getDatabaseInformations().getId());
        assertThat(dbId).isNotNull();
        log.info("[init] - Database exists id={}", dbId);
        return databaseAdmin.getDatabase();
    }

    public static ChatLanguageModel openAIChatModel(OpenAiChatModelName modelName) {
        return OpenAiChatModel.builder()
                .apiKey(AstraDBTestSupport.OPENAI_API_KEY)
                .modelName(modelName)
                .build();
    }

    public static EmbeddingModel openAIEmbedModel(OpenAiEmbeddingModelName modelName) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(AstraDBTestSupport.OPENAI_API_KEY)
                .modelName(modelName)
                .build();
    }

}

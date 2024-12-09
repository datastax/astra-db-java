package dev.langchain4j.store.memory.chat.astradb;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.langchain4j.store.memory.AstraDbChatMemory;
import com.datastax.astra.langchain4j.store.memory.AstraDbChatMemoryStore;
import com.datastax.astra.langchain4j.store.memory.tables.AstraDbTableChatMemory;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.UUID;

import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;
import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
class AstraDbChatMemoryIT {

    static final String TEST_DB = "test_langchain4j";
    static UUID dbId;
    static DataAPIClient client;
    static AstraDBAdmin astraDBAdmin;
    static Database db;

    static AstraDbChatMemoryStore chatMemoryStore;

    @BeforeAll
    static void initStoreForTests() {

        /*
         * Token Value is retrieved from environment Variable 'ASTRA_DB_APPLICATION_TOKEN', it should
         * have Organization Administration permissions (to create db)
         */
        client       = new DataAPIClient(getAstraToken(), new DataAPIClientOptions().logRequests());
        astraDBAdmin = client.getAdmin();

        /*
         * Will create a Database in Astra with the name 'test_langchain4j' if does not exist and work
         * with its identifier. The call is blocking and will wait until the database is ready.
         */
        AstraDBDatabaseAdmin databaseAdmin = (AstraDBDatabaseAdmin) astraDBAdmin.createDatabase(TEST_DB);
        dbId = UUID.fromString(databaseAdmin.getDatabaseInformations().getId());
        assertThat(dbId).isNotNull();
        log.info("[init] - Database exists id={}", dbId);

        /*
         * Initialize the client from the database identifier. A database will host multiple collections.
         * A collection stands for an Embedding Store.
         */
        db = databaseAdmin.getDatabase();
        Assertions.assertThat(db).isNotNull();

        chatMemoryStore = new AstraDbChatMemoryStore(db);
        chatMemoryStore.clear();
        log.info("[init] - Embedding Store initialized");
    }

    @Test
    public void testInsertChat() throws InterruptedException {

         // When
            String chatSessionId = "chat-" + UUID.randomUUID();

            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryStore(chatMemoryStore)
                    .maxMessages(100)
                    .id(chatSessionId)
                    .build();

            // When
            chatMemory.add(systemMessage("Your are an helpful assistant and provide advice to java developers"));
            Thread.sleep(1000);
            chatMemory.add(userMessage("I will ask you a few question about ff4j."));
            Thread.sleep(1000);
            chatMemory.add(aiMessage("Sure, go ahead!"));
            Thread.sleep(1000);
            chatMemory.add(userMessage("Can i use it with javascript "));
            Thread.sleep(1000);
            chatMemory.add(aiMessage("Yes, you can use JavaScript with FF4j " +
                    "(Feature Flipping for Java) through its REST API. " +
                    "FF4j provides " +
                    "a RESTful service that you can interact with from JavaScript."));
            Thread.sleep(1000);
            chatMemory.add(aiMessage(ToolExecutionRequest.builder()
                .id("ff4j")
                .arguments("--Ddebug-true")
                .name("langchain").build()));

            // SHOW THE CHAT
            assertThat(chatMemory.messages()).size().isEqualTo(6);
            chatMemory.messages().forEach(msg -> {
                System.out.println(msg.type() + " - " + msg.text());
            });

    }

    @Test
    public void testCollectionChatMemory() throws InterruptedException {
        String chatId = UUID.randomUUID().toString();
        AstraDbChatMemory chatMemory = new AstraDbChatMemory(db, "langchain4j_chat_memory", chatId);
        // When
        chatMemory.add(systemMessage("Your are an helpful assistant and provide advice to java developers"));
        Thread.sleep(1000);
        chatMemory.add(userMessage("I will ask you a few question about ff4j."));
        Thread.sleep(1000);
        chatMemory.add(aiMessage("Sure, go ahead!"));
        Thread.sleep(1000);
        chatMemory.add(userMessage("Can i use it with javascript "));
        Thread.sleep(1000);
        chatMemory.add(aiMessage("Yes, you can use JavaScript with FF4j " +
                "(Feature Flipping for Java) through its REST API. " +
                "FF4j provides " +
                "a RESTful service that you can interact with from JavaScript."));
        Thread.sleep(1000);
        chatMemory.add(aiMessage(ToolExecutionRequest.builder()
                .id("ff4j")
                .arguments("--Ddebug-true")
                .name("langchain").build()));

        // SHOW THE CHAT
        assertThat(chatMemory.messages()).size().isEqualTo(6);
        chatMemory.messagesAstra().forEach(msg -> {
            System.out.println(msg.messageTime() + "[" + msg.type() + "] - " + msg.text());
        });
    }

    @Test
    public void testTablePreviewChatMemory() throws InterruptedException {
        UUID chatId = UUID.randomUUID();
        AstraDbTableChatMemory chatMemory = new AstraDbTableChatMemory(db, "langchain4j_chat_memory_table", chatId);
        // When
        chatMemory.add(systemMessage("Your are an helpful assistant and provide advice to java developers"));
        Thread.sleep(1000);
        chatMemory.add(userMessage("I will ask you a few question about ff4j."));
        Thread.sleep(1000);
        chatMemory.add(aiMessage("Sure, go ahead!"));
        Thread.sleep(1000);
        chatMemory.add(userMessage("Can i use it with javascript "));
        Thread.sleep(1000);
        chatMemory.add(aiMessage("Yes, you can use JavaScript with FF4j " +
                "(Feature Flipping for Java) through its REST API. " +
                "FF4j provides " +
                "a RESTful service that you can interact with from JavaScript."));
        Thread.sleep(1000);
        chatMemory.add(aiMessage(ToolExecutionRequest.builder()
                .id("ff4j")
                .arguments("--Ddebug-true")
                .name("langchain").build()));

        // SHOW THE CHAT
        chatMemory.messagesAstra().forEach(msg -> {
            System.out.println(msg.getMessageTime() + "[" + msg.getMessageType() + "] - " + msg.getText());
        });

    }

}

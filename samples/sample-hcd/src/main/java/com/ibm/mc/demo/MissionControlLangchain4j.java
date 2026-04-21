package com.ibm.mc.demo;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;
import com.ibm.mc.demo.dto.Lyric;
import com.ibm.mc.demo.dto.Song;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.ibm.mc.demo.SuperDuperSongs.HOTEL_CALIFORNIA;
import static com.ibm.mc.demo.SuperDuperSongs.ROMEO_AND_JULIET;
import static com.ibm.mc.demo.SuperDuperSongs.TWINKLE_TWINKLE;
import static com.openai.models.embeddings.EmbeddingModel.TEXT_EMBEDDING_3_SMALL;

/**
 * We want to leverage the AstraDB langchain4j store
 */
public class MissionControlLangchain4j {

    private static final Logger log = LoggerFactory.getLogger(MissionControlLangchain4j.class);

    public static void main(String[] args) {
        // 1) Connect to HCD and ensure keyspace exists
        Database hcd = connectHcdAndCreateKeyspace();

        // 2) Create the embedding model (OpenAI official via LangChain4j)
        EmbeddingModel embeddingModel = initOpenAIEmbeddingModel();

        // 3) Create (or get) the 'lyrics' collection with a 1536-dim vector (cosine)
        Collection<Lyric> collectionLyrics = hcd.createCollection(
                "lyrics",
                new CollectionDefinition().vector(1536, COSINE),
                Lyric.class
        );

        collectionLyrics.deleteAll();

        // 4) Embed one of the sample songs and insert all lyric lines at once
        insertSong(collectionLyrics, ROMEO_AND_JULIET, embeddingModel);
        insertSong(collectionLyrics, TWINKLE_TWINKLE, embeddingModel);
        insertSong(collectionLyrics, HOTEL_CALIFORNIA, embeddingModel);
    }

    static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = MissionControlLangchain4j.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Unable to find application.properties");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load application.properties", ex);
        }
        return props;
    }

    static Database connectHcdAndCreateKeyspace() {
        // Load settings from application.properties
        Properties props = loadProperties();
        String cassandraUserName = props.getProperty("cassandra.username");
        String cassandraPassword = props.getProperty("cassandra.password");
        String keyspaceName      = props.getProperty("cassandra.keyspace");
        String dataApiUrl        = props.getProperty("cassandra.data-api.url");

        // Build token: Cassandra:base64(username):base64(password)
        String token = new UsernamePasswordTokenProvider(cassandraUserName, cassandraPassword)
                .getTokenAsString();

        // Initialize client for HCD destination with options
        DataAPIClientOptions clientOptions = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD)
                .logRequests(); // trace Data API request and responses
        
        DataAPIClient client = new DataAPIClient(token, clientOptions);
        log.info("Contacting HCD on '{}'", dataApiUrl);

        // Create keyspace via DataAPIDatabaseAdmin (idempotent)
        ((DataAPIDatabaseAdmin) client
                .getDatabase(dataApiUrl)
                .getDatabaseAdmin())
                .createKeyspace(keyspaceName, KeyspaceOptions.simpleStrategy(1));
        log.info("Keyspace '{}' created (if not exists)", keyspaceName);

        // Connect to database with keyspace
        DatabaseOptions dbOptions = new DatabaseOptions(token, clientOptions).keyspace(keyspaceName);
        Database db = client.getDatabase(dataApiUrl, dbOptions);
        log.info("Connected to HCD");
        return db;
    }

    static EmbeddingModel initOpenAIEmbeddingModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }
        return OpenAiOfficialEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();
    }

    static void insertSong(Collection<Lyric> collectionLyrics, Song song, EmbeddingModel embeddedModel) {
        Song embedded = embedSong(song, embeddedModel);
        log.info("Song '{}' lyrics have been embedded", song.title());

        insertEmbeddedSong(collectionLyrics, embedded);
        log.info("Inserted lyrics for '{}' by {}", embedded.title(), embedded.band());
    }

    /**
     * Populate the vectors part of the Song using batch embedding.
     * Assumes lyrics are non-null and non-empty.
     */
    static Song embedSong(Song song, EmbeddingModel embeddingModel) {
        // Build TextSegments preserving order
        List<TextSegment> segments = Arrays.stream(song.lyrics())
                .map(TextSegment::from)
                .toList();

        // Batch embed
        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = response.content();

        // Convert to float[][] via stream
        float[][] vectors = embeddings.stream()
                .map(Embedding::vector)
                .toArray(float[][]::new);

        // Return new song with vectors populated
        return new Song(song.band(), song.title(), song.lyrics(), vectors);
    }

    /**
     * Insert all lyrics for a song in a single call.
     */
    static void insertEmbeddedSong(Collection<Lyric> collectionLyrics, Song s) {

        List<Lyric> lyrics = IntStream.range(0, s.lyrics().length)
                .mapToObj(i -> new Lyric(s.band(), s.title(), s.lyrics()[i], s.vectors()[i]))
                .toList();

        insertLyrics(collectionLyrics, lyrics);
    }

    static void insertLyrics(Collection<Lyric> collectionLyrics, List<Lyric> lyrics) {
        collectionLyrics.insertMany(lyrics);
    }
}

package com.datastax.astra.tool.loader.rag.stores;

import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.tool.loader.rag.ingestion.RagEmbeddingsModels;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition.SOURCE_MODEL_NV_QA_4;

@Data
public class RagStore {

    public static final String TABLE_NAME_PREFIX = "rag_store_";

    public static final String INDEX_NAME_PREFIX = "idx_embeddings_";

    @Column(name ="source_id", type= TableColumnTypes.UUID)
    UUID sourceId;

    @Column(name ="job_id", type= TableColumnTypes.UUID)
    UUID jobId;

    // Could be a metadata later on
    @Column(name ="contact°id", type= TableColumnTypes.UUID)
    UUID contactId;

    @Column(name ="created_at", type= TableColumnTypes.TIMESTAMP)
    Instant createdAt = Instant.now();

    @Column(name ="chunk_idx", type= TableColumnTypes.INT)
    Integer chunkIdx = 0;

    @Column(name ="chunk_md5", type= TableColumnTypes.TEXT)
    String chunkMd5;

    @Column(name ="embedded", type= TableColumnTypes.TEXT)
    String embedded;

    @Column(name ="language", type= TableColumnTypes.TEXT)
    String language;

    @Column(name ="embeddings", type= TableColumnTypes.VECTOR)
    DataAPIVector embeddings;

    @Column(name ="context", type= TableColumnTypes.TEXT)
    String context;

    @Column(name ="questions", type= TableColumnTypes.SET, valueType = TableColumnTypes.TEXT)
    Set<String> questions;

    @Column(name ="metadata", type= TableColumnTypes.SET, valueType = TableColumnTypes.TEXT)
    Map<String, String> metadata;

    @Column(name ="tags", type= TableColumnTypes.SET, valueType = TableColumnTypes.TEXT)
    Set<String> tags;
    public static TableDefinition getTableDefinition(int dimension, VectorServiceOptions vso) {
        return new TableDefinition()
                .addColumn("source_id", TableColumnTypes.UUID)
                .addColumn("job_id", TableColumnTypes.UUID)
                .addColumnTimestamp("created_at")
                .addColumnInt("chunk_idx")
                .addColumnText("chunk_md5")
                .addColumnText("embedded")
                .addColumnText("context")
                .addColumnText("language")
                .addColumnSet("questions", TableColumnTypes.TEXT)
                .addColumnMap("metadata", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .addColumnSet("tags", TableColumnTypes.TEXT)
                .addColumnVector("embeddings", new TableColumnDefinitionVector()
                        .dimension(dimension)
                        .service(vso)) // nullable
                .partitionKey("source_id")
                .clusteringColumns(Sort.ascending("chunk_idx"));
    }

    public static TableVectorIndexDefinition getVectorIndexDefinition(VectorServiceOptions vso) {
        TableVectorIndexDefinition idxDef =
                new TableVectorIndexDefinition() // no provider key we use the header
                .column("embeddings")
                .metric(SimilarityMetric.COSINE);
        if (vso != null && RagEmbeddingsModels.NVIDIA_NEMO
                .getProvider()
                .equals(vso.getProvider())) {
            idxDef.sourceModel(SOURCE_MODEL_NV_QA_4);
        }
        return idxDef;
    }

    public static String getTableName(String provider, String model) {
        return TABLE_NAME_PREFIX
                + sanitize(provider).toLowerCase()
                + "_" + sanitize(model).toLowerCase() ;
    }

    public static String getIndexName(String provider, String model) {
        return INDEX_NAME_PREFIX
                + sanitize(provider).toLowerCase()
                + "_" + sanitize(model).toLowerCase() ;
    }

    /**
     * Replace model and provider with valid characters.
     *
     * @param input
     *      string to fix
     */
    private static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        // Process first character
        char first = input.charAt(0);
        sb.append(Character.isLetter(first) ? first : '_');
        // Process the remaining characters
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append((Character.isLetterOrDigit(c) || c == '_') ? c : '_');
        }
        return sb.toString();
    }

}

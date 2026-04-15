package com.datastax.astra.tool.loader.rag.ingestion;

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;

import java.util.UUID;

@Data
@EntityTable(RagIngestionConfig.TABLE_NAME)
public class RagIngestionConfig {

    public static final String TABLE_NAME = "rag_configs";

    @PartitionBy(0)
    UUID uid = UUID.randomUUID();

    @Column(name = "name", type = TableColumnTypes.TEXT)
    String name;

    @Column(name = "description", type = TableColumnTypes.TEXT)
    String description;

    // Splitting

    @Column(name = "splitter", type = TableColumnTypes.TEXT)
    String splitter;

    @Column(name = "chunk_size", type = TableColumnTypes.INT)
    Integer chunkSize;

    @Column(name = "chunk_overlap", type = TableColumnTypes.INT)
    Integer chunkOverlap;

    // Embedding

    @Column(name = "embedding_model", type = TableColumnTypes.TEXT)
    String embeddingModel;

    @Column(name = "embedding_provider", type = TableColumnTypes.TEXT)
    String embeddingProvider;

    @Column(name = "embedding_dimension", type = TableColumnTypes.INT)
    Integer embeddingDimension;

    // Post Processing

    @Column(name = "context_before", type = TableColumnTypes.INT)
    Integer contextBefore = 1;

    @Column(name = "context_after", type = TableColumnTypes.INT)
    Integer contextAfter = 2;

    @Column(name = "enable_nlp_filter", type = TableColumnTypes.BOOLEAN)
    boolean nlp;

    @Column(name = "enable_hyde", type = TableColumnTypes.BOOLEAN)
    boolean hyde;

    public RagIngestionConfig withEmbedding(RagEmbeddingsModels model) {
        this.embeddingDimension = model.getDimension();
        this.embeddingProvider  = model.getProvider();
        this.embeddingModel     = model.getName();
        return this;
    }
}

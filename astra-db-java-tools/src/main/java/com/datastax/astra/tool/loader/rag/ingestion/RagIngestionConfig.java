package com.datastax.astra.tool.loader.rag.ingestion;

import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
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

    @Column(name = "name", type = ColumnTypes.TEXT)
    String name;

    @Column(name = "description", type = ColumnTypes.TEXT)
    String description;

    // Splitting

    @Column(name = "splitter", type = ColumnTypes.TEXT)
    String splitter;

    @Column(name = "chunk_size", type = ColumnTypes.INT)
    Integer chunkSize;

    @Column(name = "chunk_overlap", type = ColumnTypes.INT)
    Integer chunkOverlap;

    // Embedding

    @Column(name = "embedding_model", type = ColumnTypes.TEXT)
    String embeddingModel;

    @Column(name = "embedding_provider", type = ColumnTypes.TEXT)
    String embeddingProvider;

    @Column(name = "embedding_dimension", type = ColumnTypes.INT)
    Integer embeddingDimension;

    // Post Processing

    @Column(name = "context_before", type = ColumnTypes.INT)
    Integer contextBefore = 1;

    @Column(name = "context_after", type = ColumnTypes.INT)
    Integer contextAfter = 2;

    @Column(name = "enable_nlp_filter", type = ColumnTypes.BOOLEAN)
    boolean nlp;

    @Column(name = "enable_hyde", type = ColumnTypes.BOOLEAN)
    boolean hyde;

    public RagIngestionConfig withEmbedding(RagEmbeddingsModels model) {
        this.embeddingDimension = model.getDimension();
        this.embeddingProvider  = model.getProvider();
        this.embeddingModel     = model.getName();
        return this;
    }
}

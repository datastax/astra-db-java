package com.datastax.astra.tool.loader.rag.ingestion;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@EntityTable(RagIngestionJob.TABLE_NAME)
public class RagIngestionJob {

    public static final String TABLE_NAME = "rag_jobs";

    @PartitionBy(0)
    @Column(name ="source_id", type=ColumnTypes.UUID)
    UUID sourceId;

    @PartitionSort(position = 0, order=SortOrder.ASCENDING)
    @Column(name ="uid", type=ColumnTypes.UUID)
    UUID uid = UUID.randomUUID();

    @Column(name ="config_id", type=ColumnTypes.UUID)
    UUID configId;

    @Column(name ="start", type=ColumnTypes.TIMESTAMP)
    Instant start = Instant.now();

    @Column(name ="stop", type=ColumnTypes.TIMESTAMP)
    Instant stop;

    @Column(name ="elapsed", type=ColumnTypes.BIGINT)
    Long elapsed;

    @Column(name ="chunk_count", type=ColumnTypes.INT)
    Integer chunkCount = 0;

    @Column(name ="token_count", type=ColumnTypes.INT)
    Integer tokenCount = 0;

}

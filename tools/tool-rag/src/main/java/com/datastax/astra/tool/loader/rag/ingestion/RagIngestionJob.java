package com.datastax.astra.tool.loader.rag.ingestion;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
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
    @Column(name ="source_id", type= TableColumnTypes.UUID)
    UUID sourceId;

    @PartitionSort(position = 0, order=SortOrder.ASCENDING)
    @Column(name ="uid", type= TableColumnTypes.UUID)
    UUID uid = UUID.randomUUID();

    @Column(name ="config_id", type= TableColumnTypes.UUID)
    UUID configId;

    @Column(name ="start", type= TableColumnTypes.TIMESTAMP)
    Instant start = Instant.now();

    @Column(name ="stop", type= TableColumnTypes.TIMESTAMP)
    Instant stop;

    @Column(name ="elapsed", type= TableColumnTypes.BIGINT)
    Long elapsed;

    @Column(name ="chunk_count", type= TableColumnTypes.INT)
    Integer chunkCount = 0;

    @Column(name ="token_count", type= TableColumnTypes.INT)
    Integer tokenCount = 0;

}

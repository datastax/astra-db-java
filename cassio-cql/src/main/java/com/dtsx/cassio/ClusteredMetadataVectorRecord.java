package com.dtsx.cassio;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Partitioned table with cluster and vector.
 * --------------------------------------------------------------------------
 * CREATE TABLE vector_store (
 *     partition_id text,
 *     row_id timeuuid,
 *     attributes_blob text,
 *     body_blob text,
 *     metadata_s map<text, text>,
 *     vector vector<float, 1536>,
 *     PRIMARY KEY (partition_id, row_id)
 * ) WITH CLUSTERING ORDER BY (row_id DESC);
 * --------------------------------------------------------------------------
 * CREATE CUSTOM INDEX eidx_metadata_s_vector_store
 * ON vector_store (entries(metadata_s))
 * USING 'org.apache.cassandra.index.sai.StorageAttachedIndex';
 * --------------------------------------------------------------------------
 * CREATE CUSTOM INDEX idx_vector_vector_store
 * ON vector_store (vector)
 * USING 'org.apache.cassandra.index.sai.StorageAttachedIndex'
 * WITH OPTIONS = {'similarity_function': 'COSINE'};
 * --------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
public class ClusteredMetadataVectorRecord {

    /** Partition id (clustered). */
    String partitionId = "default";

    /**
     * Metadata (for metadata filtering)
     */
    Map<String, String> metadata = new HashMap<>();

    /**
     * Vector Store
     */
    List<Float> vector;

    /** Row identifier. */
    UUID rowId;

    /** Text body. */
    String body;

    /**
     * Store special attributes
     */
    String attributes;

    /**
     * Default Constructor.
     */
    public ClusteredMetadataVectorRecord() {}

    /**
     * Create a record with a vector.
     *
     * @param vector current vector.
     */
    public ClusteredMetadataVectorRecord(List<Float> vector) {
        this.rowId = Uuids.timeBased();
        this.vector = vector;
    }



}
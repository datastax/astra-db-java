package com.datastax.astra.tool.loader.rag.sources;

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.BOOLEAN;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.MAP;
import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TEXT;

@Data
@NoArgsConstructor
@EntityTable(RagSource.TABLE_NAME)
public class RagSource {

    public static final String TABLE_NAME = "rag_sources";

    @PartitionBy(0)
    UUID uid = java.util.UUID.randomUUID();

    @Column(name ="created_at", type= TableColumnTypes.TIMESTAMP)
    Instant createdAt = Instant.now();

    @Column(name ="created_by", type= TableColumnTypes.UUID)
    UUID createdBy;

    @Column(name ="name", type= TEXT)
    String name;

    // Source is file, url, document, settings
    @Column(name ="source", type= TEXT)
    String source;

    // Will be a URL, a Path, an assetId, an documentId etc
    @Column(name ="location", type= TEXT)
    String location;

    @Column(name ="extension", type= TEXT)
    String extension;

    @Column(name ="content_type", type= TEXT)
    String contentType;

    @Column(name ="language", type= TEXT)
    String language;

    @Column(name ="status", type= TEXT)
    String status = RagSourceStatus.NEW.name();

    @Column(name ="error_message", type= TEXT)
    String errorMessage;

    @Column(name ="last_loaded", type= TableColumnTypes.TIMESTAMP)
    Instant lastLoaded;

    @Column(name ="expiration_date", type= TableColumnTypes.TIMESTAMP)
    Instant expirationDate;

    @Column(name ="metadata", type= MAP, keyType = TEXT, valueType = TEXT)
    Map<String, String> metadata = new HashMap<>();

    @Column(name ="binary_data", type= TableColumnTypes.BLOB)
    byte[] binaryData;

    @Column(name ="binary_data_md5", type= TEXT)
    String binaryDataMD5;

    @Column(name ="binary_data_size", type= TableColumnTypes.BIGINT)
    Long binaryDataSize;

    @Column(name ="is_text", type= BOOLEAN)
    Boolean isText = true;

    @Column(name ="text_data", type= TEXT)
    String textData;

    public RagSource(RagSourceCreationRequest req) {
        this.name        = req.name();
        this.createdBy   = req.createdBy();
        this.source      = req.source().name();
        this.location    = req.location();
    }

}

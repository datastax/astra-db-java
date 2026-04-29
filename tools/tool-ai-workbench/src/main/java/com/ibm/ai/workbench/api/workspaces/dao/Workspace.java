package com.ibm.ai.workbench.api.workspaces.dao;

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.TablePrimaryKey;
import com.datastax.astra.client.tables.mapping.TablePrimaryKeyClass;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * CREATE TABLE wb_config_workspaces (
 *     uid           UUID,
 *     name          text,
 *     url           text,
 *     kind          text, -- astra / hcd / open-rag
 *     namespace     text,
 *     credentials   map<text, text>, -- refs only, no secrets
 *     created_at    timestamp,
 *     updated_at    timestamp,
 *     PRIMARY KEY (uid)
 * );
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityTable("wb_config_workspaces")
@Schema(name = "Workspace", description = "Configuration Workspace")
public class Workspace {

    @TablePrimaryKey
    @Column(name = "uid", type = TableColumnTypes.UUID)
    UUID uid;

    @Column(name = "name", type = TableColumnTypes.TEXT)
    String name;

    @Column(name = "url", type = TableColumnTypes.TEXT)
    String url;

    @Column(name = "namespace", type = TableColumnTypes.TEXT)
    String namespace;

    @Column(name = "kind", type = TableColumnTypes.TEXT)
    WorkspaceType kind;

    @Column(name = "credentials", type = TableColumnTypes.MAP,
            keyType = TableColumnTypes.TEXT,
            valueType =  TableColumnTypes.TEXT)
    Map<String, String> credentials;

    @Column(name = "created_at", type = TableColumnTypes.TIMESTAMP)
    Instant createdAt;

    @Column(name = "updated_at", type = TableColumnTypes.TIMESTAMP)
    Instant updatedAt;

}

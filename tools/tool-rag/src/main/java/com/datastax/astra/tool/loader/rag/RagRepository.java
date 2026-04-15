package com.datastax.astra.tool.loader.rag;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions;
import com.datastax.astra.client.databases.definition.DatabaseInfo;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.tool.loader.rag.ingestion.RagEmbeddingsModels;
import com.datastax.astra.tool.loader.rag.ingestion.RagIngestionConfig;
import com.datastax.astra.tool.loader.rag.ingestion.RagIngestionJob;
import com.datastax.astra.tool.loader.rag.sources.RagSource;
import com.datastax.astra.tool.loader.rag.stores.RagStore;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.internal.utils.Assert.notNull;

@Slf4j
public class RagRepository {

    String token;

    String keyspace;

    CloudProviderType cloudProvider = CloudProviderType.AWS;

    String cloudRegion = "us-east-2";

    public RagRepository(String token, String keyspace) {
        this.token = token;
        this.keyspace = keyspace;
    }

    public Database getOrCreateDatabase(UUID tenantId) {
            DataAPIClient dataApiClient = DataAPIClients.astra(token);
            AstraDBAdmin astraDBAdmin = dataApiClient.getAdmin();
            // Database
            Optional<DatabaseInfo> devopsDB = astraDBAdmin
                    .listDatabases()
                    .stream()
                    .filter(db -> tenantId.toString().equals(db.getName()))
                    .findFirst();
            if (devopsDB.isEmpty()) {
                log.info("Database {} does not exists and will be created.", tenantId.toString());
                DatabaseAdmin dbAdmin = astraDBAdmin
                        .createDatabase(tenantId.toString(), cloudProvider, cloudRegion);
                dbAdmin.createKeyspace(
                        new KeyspaceDefinition().name(keyspace),
                        new CreateKeyspaceOptions().updateDBKeyspace(true));
                return dbAdmin.getDatabase(keyspace);
            }
            log.info("Database {} already exists.", tenantId);
            return dataApiClient.getDatabase(devopsDB.get().getId(), devopsDB.get().getRegion(),
                    new DatabaseOptions()
                            .token(token)
                            .keyspace(keyspace)
                            // reusing the logging
                            .dataAPIClientOptions(dataApiClient.getOptions()));
    }

    private <T> Table<T> getTable(Database db, Class<T> record) {
        String tableName = db.getTableName(record);
        db.useKeyspace(keyspace);
        if (!db.tableExists(tableName)) {
            log.info("Table {} does not exists, creating...", tableName);
            db.createTable(record, new CreateTableOptions().keyspace(keyspace));
            log.info("Table {} has been successfully created", tableName);
        }
        return db.getTable(record);
    }

    // --------------------------------------------------------------------
    // Rag Source
    // --------------------------------------------------------------------

    public Table<RagSource> getTableRagSource(UUID tenantId) {
        return getTable(getOrCreateDatabase(tenantId), RagSource.class);
    }

    public UUID registerSource(UUID tenantId, RagSource source) {
        return UUID.fromString((String)
                getTableRagSource(tenantId)
                .insertOne(source)
                .getInsertedId()
                .get(0));
    }

    // --------------------------------------------------------------------
    // Config
    // --------------------------------------------------------------------

    public Table<RagIngestionConfig> getTableRagConfig(UUID tenantId) {
        return getTable(getOrCreateDatabase(tenantId), RagIngestionConfig.class);
    }

    public UUID createConfig(UUID tenantId, RagIngestionConfig config) {
        return UUID.fromString((String)
                getTableRagConfig(tenantId)
                        .insertOne(config)
                        .getInsertedId()
                        .get(0));
    }

    // --------------------------------------------------------------------
    // Jobs
    // --------------------------------------------------------------------

    public Table<RagIngestionJob> getTableRagJob(UUID tenantId) {
        return getTable(getOrCreateDatabase(tenantId), RagIngestionJob.class);
    }

    // --------------------------------------------------------------------
    // Vector Stores
    // --------------------------------------------------------------------

    public Table<RagStore> getTableRagStore(UUID tenantId, RagIngestionConfig config) {
       return getTableRagStore(getOrCreateDatabase(tenantId),
                config.getEmbeddingProvider(),
                config.getEmbeddingModel(),
                config.getEmbeddingDimension(), null);
    }

    public Table<RagStore> getTableRagStore(Database db, String provider, String model, int dimension, VectorServiceOptions options) {
        notNull(provider, "provider");
        String tableName = RagStore.getTableName(provider, model);
        db.useKeyspace(keyspace);

        if (!db.tableExists(tableName)) {
            log.info("Table {} does not exists, creating...", tableName);
            Table<Row> table = db.createTable(tableName,
                    RagStore.getTableDefinition(dimension, options),
                    new CreateTableOptions().keyspace(keyspace));
            log.info("Table {} has been successfully created", tableName);

            table.createIndex(RagStore.getIndexName(provider, model) + "_sourceId","source_id");

            String indexName = RagStore.getIndexName(provider, model);
            table.createVectorIndex(indexName,
                    RagStore.getVectorIndexDefinition(options),
                    CreateVectorIndexOptions.IF_NOT_EXISTS);
            log.info("Vector Index {} has been successfully created", indexName);
        }
        return db.getTable(tableName, RagStore.class);
    }

    public Table<RagStore> getTableRagStore(Database db, RagEmbeddingsModels model) {
        return getTableRagStore(db, model.getProvider(), model.getName(), model.getDimension(), null);
    }

}

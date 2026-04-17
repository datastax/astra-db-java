package com.ibm.astra.demo.controller;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDescriptor;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/infos")
@Tag(name = "Data API", description = "Database metadata endpoints")
public class InfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoController.class);

    private final Database database;

    public InfoController(Database database) {
        this.database = database;
    }

    @GetMapping
    @Operation(summary = "Get current database metadata")
    public Map<String, Object> getDatabaseInfo() {
        LOGGER.info("Fetching database metadata");

        DatabaseOptions options = database.getOptions();
        DatabaseAdmin databaseAdmin = database.getDatabaseAdmin();

        List<String> tables = database.listTableNames();
        Map<String, List<String>> indexesByTable = new LinkedHashMap<>();
        for (String tableName : tables) {
            try {
                Table<Row> table = database.getTable(tableName);
                List<String> indexNames = table.listIndexes().stream()
                        .map(TableIndexDescriptor::getName)
                        .toList();
                indexesByTable.put(tableName, indexNames);
            } catch (Exception exception) {
                LOGGER.warn("Unable to list indexes for table {}", tableName, exception);
                indexesByTable.put(tableName, List.of());
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("databaseName", extractDatabaseName(database));
        response.put("databaseEndpoint", extractEndpoint(database));
        response.put("currentKeyspace", options.getKeyspace());
        response.put("keyspaces", databaseAdmin.listKeyspaceNames());
        response.put("collections", database.listCollectionNames());
        response.put("tables", tables);
        response.put("types", database.listTypeNames());
        response.put("indexes", indexesByTable);
        return response;
    }

    private String extractEndpoint(Database database) {
        return database.getRootEndpoint();
    }

    private String extractDatabaseName(Database database) {
        try {
            URI uri = URI.create(database.getRootEndpoint());
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return "unknown";
            }
            return host.split("\\.")[0];
        } catch (Exception exception) {
            LOGGER.warn("Unable to extract database name from endpoint", exception);
            return "unknown";
        }
    }
}

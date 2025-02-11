package com.datastax.astra.tool.loader.rag;

import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.tool.loader.rag.sources.RagSource;

import java.util.UUID;

public class RagGenericTest {

    public static void main(String[] args) {

        String token = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        UUID TEST_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000000");

        //Database db = DataAPIClients.astra(token).getDatabase(TEST_TENANT);
        //System.out.println(db.getInfo().getName());
        RagRepository repo = new RagRepository(token, "goodbards");
        Table<RagSource> tableSources = repo.getTableRagSource(TEST_TENANT);

        TableFindOptions options = new TableFindOptions()
                .projection(Projection.include("name", "source", "location"));
        tableSources.find(options).toList().forEach(System.out::println);
    }


}

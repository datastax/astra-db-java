package com.datastax.astra.samples;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.tool.loader.json.JsonDocumentLoader;
import com.datastax.astra.tool.loader.json.JsonLoaderSettings;
import com.datastax.astra.tool.loader.json.JsonRecordMapper;
import com.dtsx.astra.sdk.utils.JsonUtils;

public class JsonLoaderMtgSets {

    public static final String ASTRA_TOKEN       = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_DB_ENDPOINT = "https://7d7388a6-5ba2-431a-942a-250012f785c0-us-east1.apps.astra.datastax.com";
    public static final String SOURCE_JSON       = "/Users/cedricklunven/dev/datastax/JAVA/astra-db-java/astra-db-java-tools/src/test/resources/demo-set-list.json";

    public static void main(String[] args) throws Exception {
        DataAPIClient client = new DataAPIClient(ASTRA_TOKEN);
        Database db = client.getDatabase(ASTRA_DB_ENDPOINT);
        Collection<Document> collection = db.createCollection("mtg_sets");
        collection.deleteAll();

        JsonDocumentLoader.load(SOURCE_JSON,
                JsonLoaderSettings.builder()
                        .timeoutSeconds(300)
                        .batchSize(100)
                        .build(),
                collection,
                new MtgSetMapper());
    }

    public static class MtgSetMapper implements JsonRecordMapper {

        @Override
        public Document map(Document jsonRecord) {
            // manipulate each record as you like
            jsonRecord.put("_id", jsonRecord.get("code"));
            return jsonRecord;
        }
    }
}

package com.datastax.astra.tool;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class JsonMapLoader {

    public static final String TOKEN = "AstraCS:iLPiNPxSSIdefoRdkTWCfWXt:2b360d096e0e6cb732371925ffcc6485541ff78067759a2a1130390e231c2c7a";
    public static final String API_ENDPOINT = "https://a7843732-ee9b-4535-80d6-dbe6c1169783-us-east1.apps.astra.datastax.com";
    public static final String COLLECTION = "airbnb";
    public static final String JSON_FILE = "/Users/cedricklunven/Downloads/AllIdentifiers.json";
    private static final int BATCH_SIZE = 20;
    private static final int THREAD_POOL_SIZE = 5;
    private static final int TIMEOUT = 180;
    private static final String COLUMN_ID = "id";

    public static void main(String[] args) {
        JsonFactory jsonFactory = new JsonFactory();
        File f = new File(JSON_FILE);
        try (JsonParser jsonParser = jsonFactory.createParser(new FileInputStream(f))) {
            // Move to the start of the document
            while (jsonParser.nextToken() != JsonToken.START_OBJECT) {
                // do nothing, just advance to the start of the JSON object
            }

            // Iterate over the JSON tokens
            ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                if ("data".equals(fieldName)) {
                    jsonParser.nextToken(); // move to the start of the data object
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String key = jsonParser.getCurrentName();
                        jsonParser.nextToken(); // Move to the value of the key
                        // Read the value as a Map
                        Map<String, Object> valueMap = objectMapper.readValue(jsonParser, Map.class);
                        System.out.println("Key: " + key);
                        System.out.println("Value: " + valueMap);
                    }
                } else {
                    jsonParser.skipChildren(); // skip fields other than "data"
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

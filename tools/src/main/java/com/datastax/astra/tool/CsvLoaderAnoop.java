package com.datastax.astra.tool;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.utils.JsonUtils;
import com.datastax.astra.tool.csv.CsvLoader;
import com.datastax.astra.tool.csv.CsvLoaderSettings;
import com.datastax.astra.tool.csv.CsvRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvLoaderAnoop {

    public static void main(String[] args) throws Exception {
        DataAPIClient client =
                new DataAPIClient("replace_me");
        Database db =
                client.getDatabase("replace_me");
        Collection<Document> collection =
                db.createCollection("best_buy");
        collection.deleteAll();
        CsvLoader.load(
                "/Users/cedricklunven/Downloads/anoop.csv",
                CsvLoaderSettings.builder().timeoutSeconds(300).batchSize(1).build(),
                collection,
                new AnoopRowMapper());
    }

    /**
     * Post Processing of the ROW.
     * <p>Rename "id" field to "_id" to match the DataStax Astra API</p>
     */
    public static class AnoopRowMapper implements CsvRowMapper {

        /** {@inheritDoc} */
        @Override
        public Document map(Document doc) {
            doc.put("carrier1", mapCarrier(doc.getString("Carrier1")));
            doc.put("carrier2", mapCarrier(doc.getString("Carrier2")));
            doc.put("carrierN", mapCarrier(doc.getString("CarrierN")));
            doc.remove("Carrier1");
            doc.remove("Carrier2");
            doc.remove("CarrierN");
            doc.put("destination", doc.getString("Destination"));
            doc.remove("Destination");

            doc.put("weight", doc.getString("Billable Weight"));
            doc.remove("Billable Weight");

            doc.put("origin", doc.getString("Origin"));
            doc.remove("Origin");
            return doc;
        }

        private Object mapCarrier(String input) {
            String carrier1 = input
                    .replaceAll("'", "")
                    .replaceAll("\\d+: ", "");
            carrier1 = "[" + carrier1.substring(1, carrier1.length() - 1) + "]";
            int lastIndex = carrier1.lastIndexOf(",");
            if (lastIndex != -1) { // Check if there is at least one comma
                carrier1 = carrier1.substring(0, lastIndex) + carrier1.substring(lastIndex + 1);
            }
            //List<List<Object>> endValue = new ArrayList<>();
            //List<Map<String, List<Object>>> val = JsonUtils.unMarshallBean(carrier1, List.class);
            //for (Map<String, List<Object>> map : val) {
            //    List<Object> list = map.get("carrSer");
            //   endValue.add(list);
            // }
            // }
            return JsonUtils.unMarshallBean(carrier1, List.class).subList(0,5);
        }
    }



}

package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;

public class AstraProdTableITTest {

    public static final String ASTRA_TOKEN = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_DB_ENDPOINT = "https://7d7388a6-5ba2-431a-942a-250012f785c0-us-east1.apps.astra.datastax.com";
    public static final String TABLE_NAME = "sample_table";

    private Table<Row> createTable() {
        DataAPIClient client = DataAPIClients.astra(ASTRA_TOKEN);//new DataAPIClient(ASTRA_TOKEN);
        Database db     = client.getDatabase(ASTRA_DB_ENDPOINT);

        // Simple
        Table<Row> table =  db.createTable(TABLE_NAME, new TableDefinition()
                .addColumnText("email")
                .addColumnText("name")
                .addColumnVector("vector", new ColumnDefinitionVector()
                        .dimension(1024)
                        .service( new VectorServiceOptions()
                                .provider("nvidia")
                                .modelName("NV-Embed-QA")))
                .partitionKey("email"), IF_NOT_EXISTS);

        table.createVectorIndex("idx_vector_" + TABLE_NAME,
                new TableVectorIndexDefinition()
                        .column("vector")
                        .options(new TableVectorIndexDefinitionOptions().metric(COSINE)),
                new CreateVectorIndexOptions().ifNotExists(true));

        return table;
    }

    private void populateTable(Table<Row> table) {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row().add("email", "a.a@a.com").add("name", "a").addVectorize("vector", "a sample string"));
        rows.add(new Row().add("email", "a.b@a.com").add("name", "a").addVectorize("vector", "a second string"));
        rows.add(new Row().add("email", "b.b@a.com").add("name", "b").addVectorize("vector", "again a string"));
        table.insertMany(rows);
    }

    @Test
    public void shouldListItemsAndSortVector() {

        Table<Row> table = createTable();
        populateTable(table);

        table.find(null, new TableFindOptions()
          .includeSortVector(true)
          .includeSimilarity(true)
          .sort(Sort.vectorize("vector", "sample")))
        .forEach(row -> {
            System.out.println(row.getSimilarity());
            System.out.println(row.get("name"));
        });

        //System.out.println(
        table.find(null, new TableFindOptions()
          .includeSortVector(true)
          .includeSimilarity(true)
          .sort(Sort.vectorize("vector", "sample")))
        .getSortVector()
        .ifPresent(v -> System.out.println(v.dimension()));


    }


}

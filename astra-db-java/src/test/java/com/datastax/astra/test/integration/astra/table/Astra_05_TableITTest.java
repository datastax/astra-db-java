package com.datastax.astra.test.integration.astra.table;

import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.test.integration.AbstractTableITTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;

//@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "astra_prod")
//@DisabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "(?!astra_prod)")
public class Astra_05_TableITTest extends AbstractTableITTest {

    public static final String TABLE_NAME = "astra_table";

    private Table<Row> createTable() {
        // Simple
        Table<Row> table =  getDatabase().createTable(TABLE_NAME, new TableDefinition()
                .addColumnText("email")
                .addColumnText("name")
                .addColumnVector("vector", new TableColumnDefinitionVector()
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

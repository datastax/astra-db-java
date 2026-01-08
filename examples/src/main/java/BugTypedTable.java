import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BugTypedTable {

    public static final String ASTRA_TOKEN    = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_ENDPOINT = "https://844c28ee-7824-40a5-9b48-f4ca160c2b7b-us-east-2.apps.astra.datastax.com";
    public static final String KEYSPACE = "ks2";

    public static final String COL_OPENAI     = "c_product_openai";
    public static final String COL_OPENAI_KMS = "c_product_openai_kms";

    @EntityTable("t_book")
    @Data
    @NoArgsConstructor
    public static class Book {
        @PartitionBy(0)
        @Column(name = "title", type = TableColumnTypes.TEXT)
        private String title;

        @Column(name = "number_of_pages", type = TableColumnTypes.INT)
        private Integer number_of_pages;

        @Column(name = "rating", type = TableColumnTypes.FLOAT)
        private Float rating;

        @Column(name = "genres", type = TableColumnTypes.SET, valueType = TableColumnTypes.TEXT)
        private Set<String> genres;

        @Column(
                name = "metadata",
                type = TableColumnTypes.MAP,
                keyType = TableColumnTypes.TEXT,
                valueType = TableColumnTypes.TEXT)
        private Map<String, String> metadata;

        @Column(name = "is_checked_out", type = TableColumnTypes.BOOLEAN)
        private Boolean is_checked_out;

        @Column(name = "due_date", type = TableColumnTypes.DATE)
        private Date due_date;
    }

    public static void main(String[] args) throws Exception {

        // connect
        DataAPIClient client = new DataAPIClient(ASTRA_TOKEN, new DataAPIClientOptions());
        Database db = client.getDatabase(ASTRA_ENDPOINT, KEYSPACE);
        System.out.println("Connected to Astra");

        /*
        Table<Book> table = db.createTable(Book.class);
        Book book = new Book();
        book.title = "Book Title";
        book.number_of_pages = 1;
        book.rating = 2.5f;
        book.is_checked_out = false;
        book.due_date = new Date();
        table.insertOne(book);
        table.findAll().forEach(System.out::println);
        */

        System.out.println(db.listTableNames());
        db.useKeyspace("default_keyspace");
        System.out.println(db.listTableNames());


    }
}

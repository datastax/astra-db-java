package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.GameWithAnnotation;
import com.datastax.astra.client.tables.GameWithAnnotationAllHints;
import com.datastax.astra.client.tables.Table;

import static com.datastax.astra.client.tables.ddl.DropTableOptions.IF_EXISTS;

public class CreateTableOM {

    public static void main(String[] args) {
        Database db = DataAPIClients.localDbWithDefaultKeyspace();

        db.dropTable(db.getTableName(GameWithAnnotationAllHints.class), IF_EXISTS);
        db.dropTable(db.getTableName(GameWithAnnotation.class), IF_EXISTS);

        // Creation with a fully annotated bean
        Table<GameWithAnnotationAllHints> table1 = db.createTable(GameWithAnnotationAllHints.class);

        // Minimal creation
        Table<GameWithAnnotation> table2 = db.createTable(GameWithAnnotation.class);
    }
}

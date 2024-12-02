package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableAddVectorize;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.Map;

public class AlterTableAddVectorizes {

 public static void main(String[] args) {
  // Database db = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  Table<Row> myTable1 = db.getTable("games");
  AlterTableAddVectorize addVectorize =
   new AlterTableAddVectorize().columns(
    Map.of("m_vector", new VectorServiceOptions()
     .modelName("text-embedding-3-small")
     .provider("openai").authentication(
      Map.of("providerKey", "ASTRA_KMS_API_KEY_NAME")
   ))
  );
  myTable1.alter(addVectorize);
 }

}

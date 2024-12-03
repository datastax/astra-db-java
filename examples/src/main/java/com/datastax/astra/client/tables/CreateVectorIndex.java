package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;

public class CreateVectorIndex {
 public static void main(String[] args) {
   //Database db = new DataAPIClient("token").getDatabase("endpoint");
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   Table<Row> tableGames = db.getTable("games");

   //tableGames.createVectorIndex("m_vector_index", "m_vector");

   TableVectorIndexDefinition definition = new TableVectorIndexDefinition()
     .column("m_vector")
     .metric(SimilarityMetric.COSINE)
     .sourceModel("openai-v3-large");

   CreateVectorIndexOptions options = new CreateVectorIndexOptions()
     .ifNotExists(true)
     .timeout(Duration.ofSeconds(2));

   tableGames.createVectorIndex("m_vector_index", definition, options);
 }
}

package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

public class CreateCollection2 {

  public static void main(String[] args) {
    Database db = DataAPIClients.localDbWithDefaultKeyspace();

    // Building the definition
    CollectionDefinition def = new CollectionDefinition();

    // Vector
    VectorServiceOptions vectorService = new VectorServiceOptions()
            .provider( "openai")
            .modelName("text-embedding-3-small");
    VectorOptions vectorOptions = new VectorOptions()
            .dimension(1536)
            .metric(SimilarityMetric.COSINE.getValue())
            .service(vectorService);
    def.vector(vectorOptions);

    // Lexical
    LexicalOptions lexicalOptions = new LexicalOptions()
            .enabled(true)
            .analyzer(new Analyzer(STANDARD));
    def.lexical(lexicalOptions);

    // Rerank
    RerankServiceOptions rerankService = new RerankServiceOptions()
            .modelName("nvidia/llama-3.2-nv-rerankqa-1b-v2")
            .provider("nvidia");
    CollectionRerankOptions rerankOptions = new CollectionRerankOptions()
            .enabled(true)
            .service(rerankService);
    def.rerank(rerankOptions);

    db.createCollection("c_find_rerank",def);

  }

  public static void fluentCollectionCreateion() {
    DataAPIClients.localDbWithDefaultKeyspace()
      .createCollection("c_find_rerank", new CollectionDefinition()
       .vector(1536, SimilarityMetric.COSINE)
       .vectorize("nvidia", "NV-Embed-QA")
       .lexical(STANDARD)
       .rerank("nvidia", "nvidia/llama-3.2-nv-rerankqa-1b-v2"));
  }

}

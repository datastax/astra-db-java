package com.datastax.astra.client.collections.findrerank;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;

public class FindRerankerProvidesr {

    public static void main(String[] args) {
        Database db = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT");

        DatabaseAdmin dbAdmin = db
                .getDatabaseAdmin();

        FindRerankingProvidersResult results = dbAdmin
                .findRerankingProviders();

        results.getRerankingProviders().forEach((name, provider) -> {;
            System.out.println("Provider: " + name);
            System.out.println("Display Name: " + provider.getDisplayName());
            System.out.println("Is Default: " + provider.getIsDefault());
            System.out.println("Parameters: " + provider.getParameters());
            System.out.println("Supported Authentication: " + provider.getSupportedAuthentication());
            System.out.println("Models: " + provider.getModels());
        });

    }

    /*
    import { DataAPIClient } from '@datastax/astra-db-ts'

// Spawn an AstraDbAdmin instance
const admin = new DataAPIClient('**TOKEN**').admin();
const dbAdmin = admin.dbAdmin('**ENDPOINT**');

(async function () {
  const { rerankingProviders: info } = await dbAdmin.findRerankingProviders();

  // { nvidia: { ... }, ... }
  console.log(info);

  // ['nvidia', ...]
  console.log(Object.keys(info))

  // { displayName: 'Nvidia', models: [...], ... }
  console.log(info.nvidia);

  // 'Nvidia'
  console.log(info.nvidia.displayName);

  // true
  console.log(info.nvidia.isDefault);

  // []
  console.log(info.nvidia.parameters);

  // { NONE: { enabled: true, ... } }
  console.log(info.nvidia.supportedAuthentication);

  // { name: 'nvidia/llama-3.2-nv-rerankqa-1b-v2', parameters: [] }
  console.log(info.nvidia.models[0]);

  // true
  console.log(info.nvidia.models[0].isDefault);

  // []
  console.log(info.nvidia.models[0].parameters);
})();

     */
}


/*
// With Vectorize
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid("A tree on a hill")))) {
            System.out.println(results.getDocument());
        }

        // Without Vectorize
        Hybrid hybrid = new Hybrid()
                .vector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f})
                .lexical("A tree on a hill");
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid(hybrid))
                        .rerankOn("$lexical")
                        .rerankQuery("A house in the woods"))) {
            System.out.println(results.getDocument());
        }

        // Use a different query in the reranking step
        Hybrid hybrid2 = new Hybrid()
                .vector(new DataAPIVector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f}))
                .lexical("lexical query");
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid(hybrid2))
                        .rerankOn("$lexical")
                        .rerankQuery("A house in the woods"))) {
            System.out.println(results.getDocument());
        }

        // Use Filters
        Filter filter = Filters.and(
                Filters.eq("isCheckedOut", false),
                Filters.lt("numberOfPages", 300)
        );
 */

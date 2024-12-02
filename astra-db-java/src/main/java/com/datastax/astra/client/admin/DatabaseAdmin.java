package com.datastax.astra.client.admin;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.commands.CommandRunner;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.internal.utils.Assert;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the core client interface for interacting with the Data API, focusing on CRUD (Create, Read, Update, Delete)
 * operations for namespaces. This interface extends the {@link CommandRunner}, incorporating methods that
 * allow for the execution of various data manipulation and query commands within the scope of a namespace.
 * <p>
 * Implementations of this interface should provide concrete methods for namespace management, including the
 * creation, retrieval, updating, and deletion of namespaces. By leveraging the extended command runner capabilities,
 * it facilitates a streamlined and efficient approach to data management within the specified context.
 * </p>
 * <p>&nbsp;</p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 *
 * // Initialization of the client
 * DataApiClient client1 = DataApiClients.create("http://<>endpoint>", "<token>", new HttpClientOptions());
 *
 * // Example operation: Create a new namespace
 * DataApiNamespace newNamespace = client.createNamespace("exampleNamespace");
 *
 * // Example operation: Fetch a namespace
 * DataApiNamespace fetchedNamespace = client.getNamespace("exampleNamespace");
 *
 * // Example operation: Delete a namespace
 * client.deleteNamespace("exampleNamespace");
 * }
 * </pre>
 */
public interface DatabaseAdmin {

    /**
     * Retrieves a stream of keyspaces names available in the current database. This method is essential for
     * applications that need to enumerate all namespaces to perform operations such as displaying available
     * namespaces to users, managing keyspaces programmatically, or executing specific tasks within each
     * keyspace. The returned Stream facilitates efficient processing of keyspace names, enabling
     * operations like filtering, sorting, and mapping without the need for preloading all names into memory.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming 'client' is an instance of DataApiClient
     * Stream<String> keyspacesNames = client.listKeyspacesNames());
     * // Display names in the console
     * keyspacesNames.forEach(System.out::println);
     * }
     * </pre>
     *
     * @return A {@link Set} containing the names of all namespaces within the current database. The stream
     *         provides a flexible and efficient means to process the namespace names according to the application's needs.
     */
    Set<String> listKeyspaceNames();

    /**
     * Retrieve the list of embedding providers available in the current database. Embedding providers are services
     * that provide embeddings for text, images, or other data types. This method returns a map of provider names to
     * {@link EmbeddingProvider} instances, allowing applications to access and utilize the embedding services.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming 'client' is an instance of DataApiClient
     * Map<String, EmbeddingProvider> providers = client.findEmbeddingProvidersAsMap());
     * }
     * </pre>
     * @return
     *      list of available providers
     */
    FindEmbeddingProvidersResult findEmbeddingProviders();

    /**
     * Asynchronously retrieves a stream of keyspaces names available in the current database. This method facilitates
     * non-blocking operations by allowing the application to continue executing other tasks while the list of keyspace
     * names is being fetched. The method returns a CompletableFuture that, upon completion, provides a
     * Stream of keyspace names, enabling efficient and flexible processing through stream operations.
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming 'client' is an instance of DataApiClient
     * CompletableFuture<Stream<String>> futureKeyspaces= client.listKeyspacesNames();
     * // Process the stream of names asynchronously once it's available
     * futureKeyspaces.thenAccept(streamOfNames -> {
     *   Stream<String> keyspaceNames = streamOfNames);
     *   keyspaceNames.forEach(System.out::println);
     * }).exceptionally(ex -> {
     *   System.out.println("An error occurred: " + ex.getMessage());
     *   return null;
     * });
     * }
     * </pre>
     *
     * @return A CompletableFuture that, when completed, provides a stream containing the names
     *         of all keyspaces within the current database. This allows for the asynchronous processing of keyspace
     *         names with the flexibility and efficiency benefits of using a stream.
     */
    default CompletableFuture<Set<String>> listKeyspacesNamesAsync() {
        return CompletableFuture.supplyAsync(this::listKeyspaceNames);
    }

    /**
     * Retrieves a {@link Database} instance that represents a specific database (or namespace) based on the
     * provided namespace name.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String keyspace = "exampleNamespace";
     *
     * // Retrieve the namespace instance
     * DataApiNamespace namespace = client.getNamespace(keyspace);
     *
     * // Now, 'namespace' can be used to perform operations within 'exampleNamespace'
     * }
     * </pre>
     *
     * This example illustrates how to obtain a {@code DataApiNamespace} instance for a specified namespace name,
     * which then enables the execution of various database operations within that namespace. It highlights the
     * method's role in facilitating direct interaction with different parts of the database.
     *
     * @param keyspace The name of the keyspace to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @return A {@code DataApiNamespace} instance that encapsulates the operations and information specific to the
     *         given keyspace.
     */
    Database getDatabase(String keyspace);

    /**
     * Access the Database associated with this admin class.
     *
     * @param keyspace
     *      the destination keyspace for this database
     * @param userToken
     *      the user token with DML access if different from admin.
     * @return
     *      instance of the database
     */
    Database getDatabase(String keyspace, String userToken);

    /**
     * Access the Database associated with this admin class.
     *
     * @return
     *      associated database
     */
    Database getDatabase();

    /**
     * Drops (deletes) the specified keyspace from the database. This operation is idempotent; it will not
     * produce an error if the keyspace does not exist. This method is useful for cleaning up data or removing
     * entire keyspaces as part of database maintenance or restructuring. Caution should be exercised when using
     * this method, as dropping a keyspace will remove all the data, collections, or tables contained within it,
     * and this action cannot be undone.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String keyspace = "targetKeyspace";
     *
     * // Drop the keyspace
     * client.dropKeyspace(keyspace);
     *
     * // The keyspace 'targetKeyspace' is now deleted, along with all its contained data
     * }
     * </pre>
     *
     * This example demonstrates how to safely drop a keyspace by name. The operation ensures that even if the
     * keyspace does not exist, the method call will not interrupt the flow of the application, thereby allowing
     * for flexible and error-tolerant code design.
     *
     * @param keyspace The name of the keyspace to be dropped. This parameter specifies the target keyspace
     *                  that should be deleted. The operation will proceed silently and without error even if the
     *                  keyspace does not exist, ensuring consistent behavior.
     */
    default void dropKeyspace(String keyspace) {
        Assert.hasLength(keyspace, "keyspace");
        dropKeyspace(keyspace, null);
    }

    void dropKeyspace(String keyspace, BaseOptions<?> options);

    /**
     * Asynchronously drops (deletes) the specified keyspace from the database. This operation is idempotent, meaning
     * it will not produce an error if the keyspace does not exist. Performing this operation asynchronously ensures
     * that the calling thread remains responsive, and can be particularly useful for applications that require high
     * availability and cannot afford to block on potentially long-running operations. Just like its synchronous counterpart,
     * this method should be used with caution as dropping a keyspace will remove all associated data, collections,
     * or tables, and this action is irreversible.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String keyspace = "asyncTargetKeyspace";
     *
     * // Asynchronously drop the namespace
     * client.dropKeyspaceAsync(keyspace);
     *
     * // The keyspace 'asyncTargetKeyspace' is now being deleted in the background, along with all its contained data
     * }
     * </pre>
     *
     * This example illustrates the non-blocking nature of dropping a keyspace. It demonstrates the method's utility in
     * maintaining application responsiveness, even when performing potentially long-running database operations.
     *
     * @param keyspace The name of the keyspace to be dropped. This is the target keyspace that will be deleted.
     *                  The asynchronous nature of this method means that it will execute without blocking the calling
     *                  thread, regardless of whether the keyspace exists or not, ensuring a consistent and responsive
     *                  application behavior.
     */
    default void dropKeyspaceAsync(String keyspace) {
        CompletableFuture.runAsync(() -> dropKeyspace(keyspace));
    }

    /**
     * Create a Keyspace providing a name.
     *
     * @param keyspace
     *      current keyspace.
     * @param updateDBKeyspace
     *      if the keyspace should be updated in the database.
     */
    void createKeyspace(String keyspace, boolean updateDBKeyspace);

    /**
     * Syntax Sugar, retro compatible.
     *
     * @param keyspace
     *      current namespace.
     **/
    default void createKeyspace(String keyspace) {
        createKeyspace(keyspace, false);
    }

    /**
     * Create a keyspace providing a name.
     *
     * @param keyspace
     *      current keyspace.
     * @return
     *      client for namespace
     */
    default CompletableFuture<Void> createKeyspaceAsync(String keyspace) {
        return CompletableFuture.runAsync(() -> createKeyspace(keyspace));
    }

    /**
     * Evaluate if a keyspace exists.
     *
     * @param keyspace
     *      keyspace name.
     * @return
     *      if keyspace exists
     */
    default boolean keyspaceExists(String keyspace) {
        return listKeyspaceNames().contains(keyspace);
    }

}

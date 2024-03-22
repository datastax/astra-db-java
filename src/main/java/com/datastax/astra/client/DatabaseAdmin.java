package com.datastax.astra.client;

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

import com.datastax.astra.client.model.CommandRunner;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.datastax.astra.client.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Defines the core client interface for interacting with the Data API, focusing on CRUD (Create, Read, Update, Delete)
 * operations for namespaces. This interface extends the {@link CommandRunner}, incorporating methods that
 * allow for the execution of various data manipulation and query commands within the scope of a namespace.
 * <p>
 * Implementations of this interface should provide concrete methods for namespace management, including the
 * creation, retrieval, updating, and deletion of namespaces. By leveraging the extended command runner capabilities,
 * it facilitates a streamlined and efficient approach to data management within the specified context.
 * </p>
 * <p></p>
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
     * Retrieves a stream of namespace names available in the current database. This method is essential for
     * applications that need to enumerate all namespaces to perform operations such as displaying available
     * namespaces to users, managing namespaces programmatically, or executing specific tasks within each
     * namespace. The returned {@link Stream} facilitates efficient processing of namespace names, enabling
     * operations like filtering, sorting, and mapping without the need for preloading all names into memory.
     * <p></p>
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming 'client' is an instance of DataApiClient
     * Stream<String> namespaceNames = client.listNamespaceNames());
     * // Display names in the console
     * namespaceNames.forEach(System.out::println);
     * }
     * </pre>
     *
     * @return A {@link Stream<String>} containing the names of all namespaces within the current database. The stream
     *         provides a flexible and efficient means to process the namespace names according to the application's needs.
     */
    Stream<String> listNamespaceNames();

    /**
     * Asynchronously retrieves a stream of namespace names available in the current database. This method facilitates
     * non-blocking operations by allowing the application to continue executing other tasks while the list of namespace
     * names is being fetched. The method returns a {@link CompletableFuture} that, upon completion, provides a
     * {@link Stream} of namespace names, enabling efficient and flexible processing through stream operations.
     * <p></p>
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming 'client' is an instance of DataApiClient
     * CompletableFuture<Stream<String>> futureNamespaces = client.listNamespaceNamesAsync();
     * // Process the stream of names asynchronously once it's available
     * futureNamespaces.thenAccept(streamOfNames -> {
     *   Stream<String> namespaceNames = streamOfNames);
     *   namespaceNames.forEach(System.out::println);
     * }).exceptionally(ex -> {
     *   System.out.println("An error occurred: " + ex.getMessage());
     *   return null;
     * });
     * }
     * </pre>
     *
     * @return A {@link CompletableFuture&tl;Stream&tl;String&gt;&gt;} that, when completed, provides a stream containing the names
     *         of all namespaces within the current database. This allows for the asynchronous processing of namespace
     *         names with the flexibility and efficiency benefits of using a stream.
     */
    default CompletableFuture<Stream<String>> listNamespaceNamesAsync() {
        return CompletableFuture.supplyAsync(this::listNamespaceNames);
    }

    /**
     * Retrieves a {@link Database} instance that represents a specific database (or namespace) based on the
     * provided namespace name.
     *
     * @param namespaceName The name of the namespace (or keyspace) to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @return A {@code DataApiNamespace} instance that encapsulates the operations and information specific to the
     *         given namespace.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String namespaceName = "exampleNamespace";
     *
     * // Retrieve the namespace instance
     * DataApiNamespace namespace = client.getNamespace(namespaceName);
     *
     * // Now, 'namespace' can be used to perform operations within 'exampleNamespace'
     * }
     * </pre>
     *
     * This example illustrates how to obtain a {@code DataApiNamespace} instance for a specified namespace name,
     * which then enables the execution of various database operations within that namespace. It highlights the
     * method's role in facilitating direct interaction with different parts of the database.
     */
    Database getDatabase(String namespaceName);
    Database getDatabase(String namespaceName, String userToken);
    default Database getDatabase() {
        return getDatabase(DEFAULT_NAMESPACE);
    }

    /**
     * Drops (deletes) the specified namespace from the database. This operation is idempotent; it will not
     * produce an error if the namespace does not exist. This method is useful for cleaning up data or removing
     * entire keyspaces as part of database maintenance or restructuring. Caution should be exercised when using
     * this method, as dropping a namespace will remove all the data, collections, or tables contained within it,
     * and this action cannot be undone.
     *
     * @param namespace The name of the namespace to be dropped. This parameter specifies the target namespace
     *                  that should be deleted. The operation will proceed silently and without error even if the
     *                  namespace does not exist, ensuring consistent behavior.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String namespace = "targetNamespace";
     *
     * // Drop the namespace
     * client.dropNamespace(namespace);
     *
     * // The namespace 'targetNamespace' is now deleted, along with all its contained data
     * }
     * </pre>
     *
     * This example demonstrates how to safely drop a namespace by name. The operation ensures that even if the
     * namespace does not exist, the method call will not interrupt the flow of the application, thereby allowing
     * for flexible and error-tolerant code design.
     */
    void dropNamespace(String namespace);

    /**
     * Asynchronously drops (deletes) the specified namespace from the database. This operation is idempotent, meaning
     * it will not produce an error if the namespace does not exist. Performing this operation asynchronously ensures
     * that the calling thread remains responsive, and can be particularly useful for applications that require high
     * availability and cannot afford to block on potentially long-running operations. Just like its synchronous counterpart,
     * this method should be used with caution as dropping a namespace will remove all associated data, collections,
     * or tables, and this action is irreversible.
     *
     * @param namespace The name of the namespace to be dropped. This is the target namespace that will be deleted.
     *                  The asynchronous nature of this method means that it will execute without blocking the calling
     *                  thread, regardless of whether the namespace exists or not, ensuring a consistent and responsive
     *                  application behavior.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String namespace = "asyncTargetNamespace";
     *
     * // Asynchronously drop the namespace
     * client.dropNamespaceAsync(namespace);
     *
     * // The namespace 'asyncTargetNamespace' is now being deleted in the background, along with all its contained data
     * }
     * </pre>
     *
     * This example illustrates the non-blocking nature of dropping a namespace. It demonstrates the method's utility in
     * maintaining application responsiveness, even when performing potentially long-running database operations.
     */
    default void dropNamespaceAsync(String namespace) {
        CompletableFuture.runAsync(() -> dropNamespace(namespace));
    }

    /**
     * Create a Namespace providing a name.
     *
     * @param namespace
     *      current namespace.
     * @return
     *      client for namespace
     */
     void createNamespace(String namespace);

    /**
     * Create a Namespace providing a name.
     *
     * @param namespace
     *      current namespace.
     * @return
     *      client for namespace
     */
    default CompletableFuture<Void> createNamespaceAsync(String namespace) {
        return CompletableFuture.runAsync(() -> createNamespace(namespace));
    }

    /**
     * Evaluate if a namespace exists.
     *
     * @param namespace
     *      namespace name.
     * @return
     *      if namespace exists
     */
    default boolean namespaceExists(String namespace) {
        return listNamespaceNames().anyMatch(namespace::equals);
    }

}

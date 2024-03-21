package com.datastax.astra.client;


import io.stargate.sdk.data.client.model.namespaces.CreateNamespaceOptions;
import io.stargate.sdk.data.client.model.namespaces.NamespaceInformation;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
public interface DatabaseAdmin extends CommandRunner {

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
     * Retrieves a stream of {@link NamespaceInformation} representing each namespace along with its replication details.
     * This method provides a convenient way to access metadata about each namespace, including its name and replication
     * strategy, facilitating the management and overview of the database schema.
     *
     * @return A {@link Stream<NamespaceInformation>} containing metadata about each namespace,
     *         including names and replication information.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * client.listNamespaces().forEach(namespaceInfo -> {
     *   System.out.println("Namespace Name: " + namespaceInfo.getName());
     *   System.out.println("Replication Details: " + namespaceInfo.getOptions().getReplication());
     * });
     * }
     * </pre>
     *
     * This example demonstrates how to iterate over the stream of namespaces, printing out the name and replication
     * information of each. It showcases the ease with which information about the available keyspaces can be accessed
     * and used for informational or administrative purposes.
     */
    Stream<NamespaceInformation> listNamespaces();

    /**
     * Asynchronously retrieves a stream of {@link NamespaceInformation} representing each namespace along with its
     * replication details. This method extends the functionality of {@link #listNamespaces()} by performing the
     * operation in an asynchronous fashion, thereby not blocking the calling thread. The returned
     * {@link CompletableFuture} encapsulates the operation, allowing the result to be processed once the operation
     * is complete. This approach is particularly useful for applications that require non-blocking I/O operations,
     * ensuring that the system remains responsive while handling potentially long-running database operations.
     *
     * @return A {@link CompletableFuture} that, when completed, will contain a {@link Stream<NamespaceInformation>}
     *         with metadata about each keyspace, including names and replication information. The stream supports
     *         further operations such as filtering, sorting, or collecting to a specific data structure, facilitating
     *         flexible and efficient data processing in an asynchronous context.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * client.listNamespacesAsync().thenAccept(stream -> {
     *     stream.forEach(namespaceInfo -> {
     *         System.out.println("Keyspace Name: " + namespaceInfo.getName());
     *         System.out.println("Replication Details: " + namespaceInfo.getOptions().getReplication());
     *     });
     * }).exceptionally(e -> {
     *     e.printStackTrace();
     *     return null;
     * });
     * }
     * </pre>
     *
     * In this example, the asynchronous operation to retrieve namespace information is initiated, and upon completion,
     * the resulting stream is processed to print out the name and replication information of each keyspace. The use of
     * `exceptionally` provides a mechanism to handle any exceptions that may occur during the operation, ensuring that
     * the application can gracefully deal with errors.
     */
    default CompletableFuture<Stream<NamespaceInformation>> listNamespacesAsync() {
        return CompletableFuture.supplyAsync(this::listNamespaces);
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
    Database getNamespace(String namespaceName);

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
     * Creates a new namespace (or keyspace) in the database with the provided options. This method allows for
     * detailed configuration of the namespace properties, such as replication factors, datacenter specifics,
     * and other relevant settings encapsulated within {@link CreateNamespaceOptions}. It is designed for flexible
     * namespace creation, accommodating various database schemas and replication strategies to suit different
     * application requirements and environments. Upon successful creation, this method returns a {@link Database}
     * client instance, which can be used to interact with the newly created namespace, performing operations such
     * as data manipulation, schema modifications, and namespace management.
     *
     * @param namespace The name of the namespace to be created. This identifier must be unique within the database
     *                  and adhere to any naming conventions or restrictions imposed by the database system.
     * @param options   An instance of {@link CreateNamespaceOptions} containing the configuration settings for the
     *                  new namespace. These options dictate how the namespace is set up, including its replication
     *                  strategy and other properties.
     * @return A {@code DataApiNamespace} instance representing the client for interacting with the newly created
     *         namespace. This client provides access to further operations and management functionalities for the namespace.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assume 'client' is an instance of your data API client
     * String namespaceName = "newNamespace";
     * CreateNamespaceOptions options = CreateNamespaceOptions
     *
     * // Create the namespace with the specified options
     * DataApiNamespace newNamespace = client.createNamespace(namespaceName, options);
     *
     * // 'newNamespace' can now be used for further operations within 'newNamespace'
     * }
     * </pre>
     *
     * This example demonstrates how to create a new namespace with specific configuration options, showing the
     * flexibility and control offered by the method. It highlights the method's utility in preparing the database
     * environment to suit particular application needs or data distribution requirements.
     */
    Database createNamespace(String namespace, CreateNamespaceOptions options);

    /**
     * Create a Namespace asynchronously
     *
     * @param namespace
     *      current namespace
     * @param options
     *      all namespace options
     * @return
     *      client for namespace
     */
    default CompletableFuture<Database> createNamespaceAsync(String namespace, CreateNamespaceOptions options) {
        return CompletableFuture.supplyAsync(() -> createNamespace(namespace, options));
    }

    /**
     * Create a Namespace providing a name.
     *
     * @param namespace
     *      current namespace.
     * @return
     *      client for namespace
     */
    default Database createNamespace(String namespace) {
        return createNamespace(namespace, CreateNamespaceOptions.simpleStrategy(1));
    }

    /**
     * Create a Namespace providing a name.
     *
     * @param namespace
     *      current namespace.
     * @return
     *      client for namespace
     */
    default CompletableFuture<Database> createNamespaceAsync(String namespace) {
        return CompletableFuture.supplyAsync(() -> createNamespace(namespace, CreateNamespaceOptions.simpleStrategy(1)));
    }

    /**
     * Evaluate if a namespace exists.
     *
     * @param namespace
     *      namespace name.
     * @return
     *      if namespace exists
     */
    default boolean isNamespaceExists(String namespace) {
        return listNamespaceNames().anyMatch(namespace::equals);
    }



}

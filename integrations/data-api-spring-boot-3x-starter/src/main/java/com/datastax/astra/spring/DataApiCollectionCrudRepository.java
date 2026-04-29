package com.datastax.astra.spring;

import com.datastax.astra.boot.autoconfigure.DataAPIClientProperties;
import com.datastax.astra.boot.autoconfigure.SchemaAction;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndReplaceOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.mapping.DataApiCollection;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.internal.reflection.CollectionBeanDefinition;
import com.datastax.astra.internal.utils.BetaPreview;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.lang.NonNull;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for Spring Data CRUD repositories backed by DataStax Astra DB Collections.
 * <p>
 * This class provides a complete implementation of Spring's {@link CrudRepository} interface
 * using the DataStax Astra DB Java SDK's {@link Collection} API. It automatically discovers
 * and initializes collections based on the {@link DataApiCollection} annotation on the document class.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @DataApiCollection(name = "products")
 * public class Product {
 *     @DocumentId
 *     private String id;
 *
 *     private String name;
 *     private BigDecimal price;
 *     // getters and setters
 * }
 *
 * @Repository
 * public interface ProductRepository extends DataApiCollectionCrudRepository<Product, String> {}
 * }
 * </pre>
 *
 * @param <RECORD> the document type, should be annotated with {@link DataApiCollection}
 * @param <ID> the document ID type (must match the type of the field annotated with @DocumentId)
 */
@Slf4j
@BetaPreview
public abstract class DataApiCollectionCrudRepository<RECORD, ID>
        implements CrudRepository<RECORD, ID>, QueryByExampleExecutor<RECORD> {

    /**
     * Injected Database instance from Spring context.
     */
    @Autowired
    @Getter
    protected Database database;
    /**
     * Injected configuration properties.
     */
    @Autowired
    @Getter
    protected DataAPIClientProperties yamlConfig;

    /**
     * The underlying DataStax Astra DB Collection instance.
     */
    @Getter
    protected Collection<RECORD> collection;

    /**
     * The document class type.
     */
    @Getter
    protected Class<RECORD> documentClass;

    /**
     * The document ID class type.
     */
    @Getter
    protected Class<ID> idClass;

    /**
     * Bean definition for the document class.
     */
    @Getter
    protected CollectionBeanDefinition<RECORD> beanDefinition;

    /**
     * Initializes the repository after dependency injection.
     * <p>
     * This method discovers the document class from the generic type parameters,
     * validates that it's annotated with {@link DataApiCollection}, and initializes
     * the underlying collection based on the configured schema action.
     * </p>
     *
     * @throws IllegalStateException if the document class is not properly annotated
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    protected void init() {
        // Extract the document and ID classes from generic type parameters
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.documentClass = (Class<RECORD>) genericSuperclass.getActualTypeArguments()[0];
        this.idClass = (Class<ID>) genericSuperclass.getActualTypeArguments()[1];

        // Create bean definition
        this.beanDefinition = new CollectionBeanDefinition<>(documentClass);

        // Validate that the document class is annotated with @DataApiCollection
        DataApiCollection annotation = documentClass.getAnnotation(DataApiCollection.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format(
                    "Document class '%s' must be annotated with @DataApiCollection",
                    documentClass.getName()));
        }

        // Get the collection name from annotation or use class name
        String collectionName = annotation.name();
        if (collectionName == null || collectionName.isEmpty()) {
            collectionName = documentClass.getSimpleName().toLowerCase();
        }

        // Handle schema actions
        if (SchemaAction.CREATE_IF_NOT_EXISTS.equals(yamlConfig.getSchemaAction())) {
            log.info("Detected schema action CREATE_IF_NOT_EXISTS, ensuring collection {} exists...", collectionName);
            if (!database.collectionExists(collectionName)) {
                log.info("Collection '{}' does not exist, creating it...", collectionName);
                CollectionDefinition expected = beanDefinition.buildCollectionDefinition();
                database.createCollection(collectionName, expected, documentClass);
                log.info("Collection '{}' created successfully", collectionName);
            } else {
                log.info("Collection '{}' already exists", collectionName);
            }
        } else if (SchemaAction.VALIDATE.equals(yamlConfig.getSchemaAction())) {
            log.info("Detected schema action VALIDATE, validating collection {}...", collectionName);
            if (!database.collectionExists(collectionName)) {
                throw new IllegalArgumentException("Collection '" + collectionName + "' does not exist");
            } else {
                CollectionDefinition existing = database.getCollection(collectionName).getDefinition();
                CollectionDefinition expected = beanDefinition.buildCollectionDefinition();

                // Compare collection definitions
                if (!existing.equals(expected)) {
                    throw new IllegalStateException(String.format(
                            "Collection '%s' schema mismatch. Existing collection definition does not match document class '%s' definition. " +
                                    "Expected: %s, Found: %s",
                            collectionName, documentClass.getName(), expected, existing));
                }
                log.info("Collection '{}' schema validated successfully", collectionName);
            }
        }

        // Get the collection instance
        this.collection = database.getCollection(collectionName, documentClass);
    }

    /**
     * Extracts the document ID from a document instance.
     *
     * @param document the document
     * @return the document ID
     */
    @SuppressWarnings("unchecked")
    protected ID extractDocumentId(RECORD document) {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null");
        }
        Object id = beanDefinition.getId(document);
        if (id == null) {
            throw new IllegalStateException("Document ID is null");
        }
        return (ID) id;
    }

    // ==================== CrudRepository Implementation ====================

    @Override
    @NonNull
    public <S extends RECORD> S save(@NonNull S entity) {
        Object documentId = beanDefinition.getId(entity);
        if (documentId == null) {
            collection.insertOne(entity);
            return entity;
        }
        collection.findOneAndReplace(
                Filters.id(documentId),
                entity,
                new CollectionFindOneAndReplaceOptions().upsert(true));
        return entity;
    }

    @Override
    @NonNull
    public <S extends RECORD> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        List<S> batch = new ArrayList<>();
        for (S entity : entities) {
            batch.add(entity);
        }
        if (batch.isEmpty()) {
            return batch;
        }

        CollectionInsertManyResult result = collection.insertMany(batch);
        if (beanDefinition.canSetId()) {
            List<Object> insertedIds = result.getInsertedIds();
            int size = Math.min(batch.size(), insertedIds.size());
            for (int i = 0; i < size; i++) {
                if (beanDefinition.getId(batch.get(i)) == null && insertedIds.get(i) != null) {
                    beanDefinition.setId(batch.get(i), insertedIds.get(i));
                }
            }
        }
        return batch;
    }

    @Override
    @NonNull
    public Optional<RECORD> findById(@NonNull ID id) {
        return collection.findById(id);
    }

    @Override
    public boolean existsById(@NonNull ID id) {
        return findById(id).isPresent();
    }

    @Override
    @NonNull
    public Iterable<RECORD> findAll() {
        return collection.findAll().toList();
    }

    /**
     * Finds all entities matching the provided Data API filter.
     *
     * @param filter
     *      filter to apply
     * @return
     *      matching entities
     */
    @NonNull
    public Iterable<RECORD> findAll(Filter filter) {
        return collection.find(filter).toList();
    }

    /**
     * Finds all entities matching the provided Data API filter and Spring sort.
     *
     * @param filter
     *      filter to apply
     * @param sort
     *      spring sort to map
     * @return
     *      matching entities
     */
    @NonNull
    public Iterable<RECORD> findAll(com.datastax.astra.client.core.query.Filter filter, @NonNull Sort sort) {
        com.datastax.astra.client.collections.commands.options.CollectionFindOptions options =
                new com.datastax.astra.client.collections.commands.options.CollectionFindOptions();
        com.datastax.astra.client.core.query.Sort[] mappedSort = DataApiSpringQueryMapper.mapSort(sort);
        if (mappedSort.length > 0) {
            options.sort(mappedSort);
        }
        return collection.find(filter, options).toList();
    }

    /**
     * Finds all entities matching the provided Data API filter and pageable settings.
     *
     * @param filter
     *      filter to apply
     * @param pageable
     *      spring pageable to map
     * @return
     *      matching entities
     */
    @NonNull
    public Iterable<RECORD> findAll(com.datastax.astra.client.core.query.Filter filter, @NonNull Pageable pageable) {
        return collection.find(filter, DataApiSpringQueryMapper.mapPageable(pageable)).toList();
    }

    @Override
    @NonNull
    public Iterable<RECORD> findAllById(@NonNull Iterable<ID> ids) {
        com.datastax.astra.client.core.query.Filter idFilter = DataApiSpringQueryMapper.mapIdIn(ids);
        if (idFilter == null) {
            return List.of();
        }
        return collection.find(idFilter).toList();
    }

    @Override
    public long count() {
        return collection.countDocuments(Integer.MAX_VALUE);
    }

    @Override
    public void deleteById(@NonNull ID id) {
        collection.deleteOne(com.datastax.astra.client.core.query.Filters.id(id));
    }

    @Override
    public void delete(@NonNull RECORD entity) {
        deleteById(extractDocumentId(entity));
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends RECORD> entities) {
        for (RECORD entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        collection.deleteAll();
    }

    @Override
    @NonNull
    public <S extends RECORD> Optional<S> findOne(@NonNull Example<S> example) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        return collection.findOne(filter).map(example.getProbeType()::cast);
    }

    @Override
    @NonNull
    public <S extends RECORD> Iterable<S> findAll(@NonNull Example<S> example) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        return collection.find(filter, new com.datastax.astra.client.collections.commands.options.CollectionFindOptions(), example.getProbeType()).toList();
    }

    @Override
    @NonNull
    public <S extends RECORD> Iterable<S> findAll(@NonNull Example<S> example, @NonNull Sort sort) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        com.datastax.astra.client.collections.commands.options.CollectionFindOptions options =
                new com.datastax.astra.client.collections.commands.options.CollectionFindOptions();
        com.datastax.astra.client.core.query.Sort[] mappedSort = DataApiSpringQueryMapper.mapSort(sort);
        if (mappedSort.length > 0) {
            options.sort(mappedSort);
        }
        return collection.find(filter, options, example.getProbeType()).toList();
    }

    @Override
    @NonNull
    public <S extends RECORD> org.springframework.data.domain.Page<S> findAll(@NonNull Example<S> example, @NonNull Pageable pageable) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        com.datastax.astra.client.collections.commands.options.CollectionFindOptions options = DataApiSpringQueryMapper.mapPageable(pageable);
        java.util.List<S> content = collection.find(filter, options, example.getProbeType()).toList();
        return new org.springframework.data.domain.PageImpl<>(content, pageable, content.size());
    }

    @Override
    public <S extends RECORD> long count(@NonNull Example<S> example) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        return collection.countDocuments(filter, Integer.MAX_VALUE);
    }

    @Override
    public <S extends RECORD> boolean exists(@NonNull Example<S> example) {
        com.datastax.astra.client.core.query.Filter filter = DataApiSpringQueryMapper.mapExample(example, new CollectionBeanDefinition<>(example.getProbeType()));
        return collection.findOne(filter).isPresent();
    }

    @Override
    public <S extends RECORD, R> R findBy(
            @NonNull Example<S> example,
            @NonNull java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("QueryByExampleExecutor#findBy is not implemented yet for Data API repositories");
    }
}
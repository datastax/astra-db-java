package com.datastax.astra.spring;

import com.datastax.astra.boot.autoconfigure.DataAPIClientProperties;
import com.datastax.astra.boot.autoconfigure.SchemaAction;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import com.datastax.astra.client.tables.mapping.TablePrimaryKey;
import com.datastax.astra.client.tables.mapping.TablePrimaryKeyClass;
import com.datastax.astra.internal.reflection.EntityTableBeanDefinition;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract base class for Spring Data CRUD repositories backed by DataStax Astra DB Tables.
 * <p>
 * This class provides a complete implementation of Spring's {@link CrudRepository} interface
 * using the DataStax Astra DB Java SDK's {@link Table} API. It automatically discovers
 * and initializes tables based on the {@link EntityTable} annotation on the entity class.
 * </p>
 *
 * <p><strong>Primary Key Patterns:</strong></p>
 * <p>The repository supports three patterns for defining primary keys:</p>
 * <ol>
 *   <li><strong>Single Partition Key:</strong> Use the field type directly (e.g., UUID, String)</li>
 *   <li><strong>Composite Key with Map:</strong> Use Map&lt;String, Object&gt; where keys are column names</li>
 *   <li><strong>Composite Key with @TablePrimaryKeyClass:</strong> Use a dedicated class annotated with @TablePrimaryKeyClass</li>
 * </ol>
 *
 * <p><strong>Pattern 1: Single Partition Key</strong></p>
 * <pre>
 * {@code
 * @EntityTable("users")
 * public class User {
 *     @PartitionBy(1)
 *     @Column("user_id")
 *     private UUID userId;
 *     
 *     @Column("name")
 *     private String name;
 * }
 *
 * @Repository
 * public interface UserRepository extends DataApiTableCrudRepository<User, UUID> {}
 * }
 * </pre>
 *
 * <p><strong>Pattern 2: Composite Key with Map</strong></p>
 * <pre>
 * {@code
 * @EntityTable("orders")
 * public class Order {
 *     @PartitionBy(1)
 *     @Column("customer_id")
 *     private String customerId;
 *     
 *     @PartitionBy(2)
 *     @Column("order_date")
 *     private LocalDate orderDate;
 *     
 *     @Column("amount")
 *     private BigDecimal amount;
 * }
 *
 * @Repository
 * public interface OrderRepository extends DataApiTableCrudRepository<Order, Map<String, Object>> {}
 * 
 * // Usage:
 * Map<String, Object> pk = Map.of("customer_id", "CUST123", "order_date", LocalDate.now());
 * Optional<Order> order = orderRepository.findById(pk);
 * }
 * </pre>
 *
 * <p><strong>Pattern 3: Composite Key with @TablePrimaryKeyClass (Recommended)</strong></p>
 * <pre>
 * {@code
 * @TablePrimaryKeyClass
 * public class OrderKey {
 *     @PartitionBy(1)
 *     @Column("customer_id")
 *     private String customerId;
 *     
 *     @PartitionSort(position = 1, order = PartitionSortOrder.ASC)
 *     @Column("order_date")
 *     private LocalDate orderDate;
 *     
 *     // constructors, getters, setters, equals, hashCode
 * }
 *
 * @EntityTable("orders")
 * public class Order {
 *     @TablePrimaryKey
 *     private OrderKey key;
 *     
 *     @Column("amount")
 *     private BigDecimal amount;
 * }
 *
 * @Repository
 * public interface OrderRepository extends DataApiTableCrudRepository<Order, OrderKey> {}
 * 
 * // Usage:
 * OrderKey key = new OrderKey("CUST123", LocalDate.now());
 * Optional<Order> order = orderRepository.findById(key);
 * }
 * </pre>
 *
 * @param <ROW> the entity type, must be annotated with {@link EntityTable}
 * @param <PK> the primary key type (single field, Map&lt;String, Object&gt;, or @TablePrimaryKeyClass annotated class)
 */
@Slf4j
public abstract class DataApiTableCrudRepository<ROW, PK> implements CrudRepository<ROW, PK> {

    /**
     * The underlying DataStax Astra DB Table instance.
     */
    protected Table<ROW> dataAPITable;

    /**
     * The entity class type.
     */
    protected Class<ROW> entityClass;

    /**
     * The primary key class type.
     */
    protected Class<PK> primaryKeyClass;

    /**
     * Cached list of partition key column names in order.
     */
    protected List<String> partitionKeyColumns;

    /**
     * Cached list of clustering column names in order.
     */
    protected List<String> clusteringColumns;

    /**
     * Whether the primary key is a @TablePrimaryKeyClass annotated class.
     */
    protected boolean isPrimaryKeyClass;

    /**
     * Field in the entity annotated with @TablePrimaryKey (if using @TablePrimaryKeyClass pattern).
     */
    protected Field primaryKeyField;

    /**
     * Injected Database instance from Spring context.
     */
    @Autowired
    protected Database database;

    @Autowired
    protected DataAPIClientProperties yamlConfig;

    /**
     * Initializes the repository after dependency injection.
     * <p>
     * This method discovers the entity class from the generic type parameters,
     * validates that it's annotated with {@link EntityTable}, and initializes
     * the underlying table.
     * </p>
     *
     * @throws IllegalStateException if the entity class is not properly annotated
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    protected void init() {
        // Extract the entity and primary key classes from generic type parameters
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<ROW>) genericSuperclass.getActualTypeArguments()[0];
        this.primaryKeyClass = (Class<PK>) genericSuperclass.getActualTypeArguments()[1];

        // Validate that the entity class is annotated with @EntityTable
        EntityTable annotation = entityClass.getAnnotation(EntityTable.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format(
                    "Entity class '%s' must be annotated with @EntityTable",
                    entityClass.getName()));
        }

        // Check if using @TablePrimaryKeyClass pattern
        this.isPrimaryKeyClass = primaryKeyClass.isAnnotationPresent(TablePrimaryKeyClass.class);
        
        if (isPrimaryKeyClass) {
            // Find the field annotated with @TablePrimaryKey in the entity
            this.primaryKeyField = findPrimaryKeyField(entityClass);
            if (primaryKeyField == null) {
                throw new IllegalStateException(String.format(
                        "Entity class '%s' must have a field annotated with @TablePrimaryKey when using @TablePrimaryKeyClass",
                        entityClass.getName()));
            }
            // Extract partition keys from the primary key class
            this.partitionKeyColumns = extractPartitionKeyColumns(primaryKeyClass);
            this.clusteringColumns = extractClusteringColumns(primaryKeyClass);
        } else {
            // Extract partition keys from the entity class directly
            this.partitionKeyColumns = extractPartitionKeyColumns(entityClass);
            this.clusteringColumns = extractClusteringColumns(entityClass);
        }

        if (partitionKeyColumns.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Entity class '%s' must have at least one field annotated with @PartitionBy",
                    entityClass.getName()));
        }

        // Get the table name from annotation or use class name
        String tableName = annotation.value();
        if (tableName == null || tableName.isEmpty()) {
            tableName = entityClass.getSimpleName().toLowerCase();
        }

        // Check the schema action
        if (yamlConfig.getSchemaAction() != null) {
            if (SchemaAction.CREATE_IF_NOT_EXISTS.equals(yamlConfig.getSchemaAction())) {
                log.info("Detected schema action CREATE_IF_NOT_EXISTS, creating table "  + tableName + "...");
                database.createTable(entityClass);
                log.info("Table '{}' has been created", tableName);
            }
        } else if (SchemaAction.VALIDATE.equals(yamlConfig.getSchemaAction())) {
            log.info("Detected schema action VALIDATE, validating table " + tableName + "...");
            if (!database.tableExists(tableName)) {
                log.info("Table '{}' does not exist", tableName);
                throw new IllegalArgumentException("Table '" + tableName + "' does not exist");
            } else {
                TableDefinition existing = database.getTable(tableName).getDefinition();
                TableDefinition settings = database.getTable(tableName, entityClass).getDefinition();
                
                // Compare table definitions
                if (!existing.equals(settings)) {
                    throw new IllegalStateException(String.format(
                            "Table '%s' schema mismatch. Existing table definition does not match entity class '%s' definition. " +
                            "Expected: %s, Found: %s",
                            tableName, entityClass.getName(), settings, existing));
                }
                log.info("Table '{}' schema validated successfully", tableName);
            }
        }

        // Get the table instance
        this.dataAPITable = database.getTable(tableName, entityClass);
    }

    /**
     * Finds the field annotated with @TablePrimaryKey in the entity class.
     *
     * @param clazz the entity class
     * @return the primary key field, or null if not found
     */
    protected Field findPrimaryKeyField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TablePrimaryKey.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    /**
     * Extracts partition key column names from a class.
     * Fields annotated with @PartitionBy are sorted by their position value.
     *
     * @param clazz the class to inspect
     * @return ordered list of partition key column names
     */
    protected List<String> extractPartitionKeyColumns(Class<?> clazz) {
        List<PartitionKeyInfo> partitionKeys = new ArrayList<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            PartitionBy partitionBy = field.getAnnotation(PartitionBy.class);
            if (partitionBy != null) {
                Column column = field.getAnnotation(Column.class);
                String columnName = (column != null && column.name() != null && !column.name().isEmpty()) 
                        ? column.name() 
                        : field.getName();
                partitionKeys.add(new PartitionKeyInfo(partitionBy.value(), columnName));
            }
        }
        
        // Sort by position and extract column names
        return partitionKeys.stream()
                .sorted(Comparator.comparingInt(PartitionKeyInfo::position))
                .map(PartitionKeyInfo::columnName)
                .collect(Collectors.toList());
    }

    /**
     * Extracts clustering column names from a class.
     * Fields annotated with @PartitionSort are sorted by their position value.
     *
     * @param clazz the class to inspect
     * @return ordered list of clustering column names
     */
    protected List<String> extractClusteringColumns(Class<?> clazz) {
        List<ClusteringKeyInfo> clusteringKeys = new ArrayList<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            PartitionSort partitionSort = field.getAnnotation(PartitionSort.class);
            if (partitionSort != null) {
                Column column = field.getAnnotation(Column.class);
                String columnName = (column != null && column.name() != null && !column.name().isEmpty()) 
                        ? column.name() 
                        : field.getName();
                clusteringKeys.add(new ClusteringKeyInfo(partitionSort.position(), columnName));
            }
        }
        
        // Sort by position and extract column names
        return clusteringKeys.stream()
                .sorted(Comparator.comparingInt(ClusteringKeyInfo::position))
                .map(ClusteringKeyInfo::columnName)
                .collect(Collectors.toList());
    }

    /**
     * Helper record to store partition key information.
     */
    protected record PartitionKeyInfo(int position, String columnName) {}

    /**
     * Helper record to store clustering key information.
     */
    protected record ClusteringKeyInfo(int position, String columnName) {}

    /**
     * Gets the underlying DataStax Astra DB Table instance.
     *
     * @return the table instance
     */
    public Table<ROW> getTable() {
        return dataAPITable;
    }

    /**
     * Converts a primary key to a Map representation.
     * <p>
     * Handles three cases:
     * <ul>
     *   <li>Single partition key: creates a map with one entry</li>
     *   <li>Map primary key: returns as-is</li>
     *   <li>@TablePrimaryKeyClass: extracts fields using reflection</li>
     * </ul>
     * </p>
     *
     * @param pk the primary key
     * @return Map representation of the primary key
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> primaryKeyToMap(PK pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Primary key must not be null");
        }

        // If already a Map, return it
        if (pk instanceof Map) {
            return (Map<String, Object>) pk;
        }

        Map<String, Object> pkMap = new HashMap<>();

        // If using @TablePrimaryKeyClass, extract fields from the primary key object
        if (isPrimaryKeyClass) {
            try {
                List<String> allKeyColumns = new ArrayList<>(partitionKeyColumns);
                allKeyColumns.addAll(clusteringColumns);
                
                for (String columnName : allKeyColumns) {
                    Field field = findFieldByColumnName(primaryKeyClass, columnName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object value = field.get(pk);
                        if (value != null) {
                            pkMap.put(columnName, value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to extract primary key fields", e);
            }
        } else {
            // Single partition key - create a map with one entry
            if (partitionKeyColumns.size() == 1 && clusteringColumns.isEmpty()) {
                pkMap.put(partitionKeyColumns.get(0), pk);
            } else {
                throw new IllegalArgumentException(
                        "For composite primary keys, use Map<String, Object> or @TablePrimaryKeyClass");
            }
        }

        return pkMap;
    }

    /**
     * Extracts the primary key from an entity.
     *
     * @param entity the entity
     * @return the primary key
     */
    @SuppressWarnings("unchecked")
    protected PK extractPrimaryKey(ROW entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        try {
            if (isPrimaryKeyClass && primaryKeyField != null) {
                // Extract the @TablePrimaryKey field from the entity
                return (PK) primaryKeyField.get(entity);
            } else {
                // Extract partition key values directly from entity fields
                Map<String, Object> pkValues = new HashMap<>();
                List<String> allKeyColumns = new ArrayList<>(partitionKeyColumns);
                allKeyColumns.addAll(clusteringColumns);
                
                for (String columnName : allKeyColumns) {
                    Field field = findFieldByColumnName(entityClass, columnName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        if (value != null) {
                            pkValues.put(columnName, value);
                        }
                    }
                }
                
                // Return single value or map
                if (allKeyColumns.size() == 1) {
                    return (PK) pkValues.values().iterator().next();
                } else {
                    return (PK) pkValues;
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to extract primary key from entity", e);
        }
    }

    /**
     * Creates a filter for the primary key.
     *
     * @param pk the primary key value
     * @return a Filter for the primary key
     */
    protected Filter createPrimaryKeyFilter(PK pk) {
        Map<String, Object> pkMap = primaryKeyToMap(pk);
        
        List<Filter> filters = new ArrayList<>();
        List<String> allKeyColumns = new ArrayList<>(partitionKeyColumns);
        allKeyColumns.addAll(clusteringColumns);
        
        for (String keyColumn : allKeyColumns) {
            Object value = pkMap.get(keyColumn);
            if (value == null) {
                throw new IllegalArgumentException(String.format(
                        "Primary key must contain value for column '%s'", keyColumn));
            }
            filters.add(Filters.eq(keyColumn, value));
        }

        return filters.size() == 1 ? filters.get(0) : Filters.and(filters.toArray(new Filter[0]));
    }

    /**
     * Finds a field by its column name or field name.
     *
     * @param clazz the class to search
     * @param columnName the column name to find
     * @return the field, or null if not found
     */
    protected Field findFieldByColumnName(Class<?> clazz, String columnName) {
        for (Field field : clazz.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            String fieldColumnName = (column != null && column.name() != null && !column.name().isEmpty()) 
                    ? column.name() 
                    : field.getName();
            if (fieldColumnName.equals(columnName)) {
                return field;
            }
        }
        return null;
    }

    // ==================== CrudRepository Implementation ====================

    @Override
    @NonNull
    public <S extends ROW> S save(@NonNull S entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        dataAPITable.insertOne(entity);
        return entity;
    }

    @Override
    @NonNull
    public <S extends ROW> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Entities must not be null");
        }
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    @NonNull
    public Optional<ROW> findById(@NonNull PK pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Primary key must not be null");
        }
        Filter filter = createPrimaryKeyFilter(pk);
        return dataAPITable.findOne(filter);
    }

    @Override
    public boolean existsById(@NonNull PK pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Primary key must not be null");
        }
        return findById(pk).isPresent();
    }

    @Override
    @NonNull
    public Iterable<ROW> findAll() {
        return dataAPITable.findAll().toList();
    }

    @Override
    @NonNull
    public Iterable<ROW> findAllById(@NonNull Iterable<PK> pks) {
        if (pks == null) {
            throw new IllegalArgumentException("Primary keys must not be null");
        }
        List<ROW> result = new ArrayList<>();
        for (PK pk : pks) {
            findById(pk).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        try {
            return dataAPITable.countRows(Integer.MAX_VALUE);
        } catch (TooManyRowsToCountException e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void deleteById(@NonNull PK pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Primary key must not be null");
        }
        Filter filter = createPrimaryKeyFilter(pk);
        dataAPITable.deleteOne(filter);
    }

    @Override
    public void delete(@NonNull ROW entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        PK pk = extractPrimaryKey(entity);
        deleteById(pk);
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends PK> pks) {
        if (pks == null) {
            throw new IllegalArgumentException("Primary keys must not be null");
        }
        for (PK pk : pks) {
            deleteById(pk);
        }
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends ROW> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Entities must not be null");
        }
        for (ROW entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        dataAPITable.deleteAll();
    }
}

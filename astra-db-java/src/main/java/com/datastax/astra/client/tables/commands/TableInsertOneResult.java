package com.datastax.astra.client.tables.commands;

import com.datastax.astra.client.tables.columns.ColumnDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents the result of an insertion operation into a table.
 *
 * <p>This record stores the inserted IDs and the schema for the primary key.
 * It includes both an all-arguments constructor and a no-argument constructor that initializes
 * the fields to empty collections.
 * </p>
 *
 * @param insertedIds
 *      A list of objects representing the IDs of the inserted records.
 *
 * @param primaryKeySchema
 *      A map that defines the schema of the primary key, where the key is the
 *      column name and the value is the {@link ColumnDefinition}.
 */
public record TableInsertOneResult(
        List<Object> insertedIds,
        LinkedHashMap<String, ColumnDefinition> primaryKeySchema) {

    /**
     * No-argument constructor that initializes {@code insertedIds} to an empty {@link ArrayList}
     * and {@code primaryKeySchema} to an empty {@link LinkedHashMap}.
     */
    public TableInsertOneResult() {
        this(new ArrayList<>(), new LinkedHashMap<>());
    }
}

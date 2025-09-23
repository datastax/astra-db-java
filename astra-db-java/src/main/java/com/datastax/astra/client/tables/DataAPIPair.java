package com.datastax.astra.client.tables;

import com.datastax.astra.internal.serdes.tables.DataAPIPairSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represent a key/value pair.
 *
 * @param <K> key type
 * @param <V> value type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = DataAPIPairSerializer.class)
public class DataAPIPair<K, V> {

    K key;

    V value;
}

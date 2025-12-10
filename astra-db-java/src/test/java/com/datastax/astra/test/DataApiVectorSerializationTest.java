package com.datastax.astra.test;

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DataApiVectorSerializationTest {

    @Test
    public void binarySerialization() {
        byte[] bytes = "hello".getBytes();
        RowSerializer serializer = new RowSerializer();
        String json = serializer.marshall(bytes);
        // {"$binary":"aGVsbG8="}

        System.out.println(json);
        byte[] bytes2 = serializer.unMarshallBean(json, byte[].class);
        assertThat(new String(bytes2)).isEqualTo("hello");
    }

    @Test
    public void testVectorSerialization() {
        DataAPIClientOptions.getSerdesOptions().encodeDataApiVectorsAsBase64(true);;
        RowSerializer serializer = new RowSerializer();
        DataAPIVector vector = new DataAPIVector(new float[]{0.4f, -0.6f, 0.2f});
        System.out.println(serializer.marshall(vector));

        String json1 = "{\"$binary\":\"PszMzb8ZmZo+TMzN\"}";
        DataAPIVector vector2 = serializer.unMarshallBean(json1, DataAPIVector.class);
        System.out.println(vector2.getEmbeddings());

        String json2 = "[0.4, -0.6, 0.2]";
    }

    @Test
    public void serializationInstant() {
        String sample = "2024-12-04T15:04:07.203Z";
        Instant.parse(sample);
    }
}

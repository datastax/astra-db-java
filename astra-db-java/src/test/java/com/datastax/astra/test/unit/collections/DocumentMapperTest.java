package com.datastax.astra.test.unit.collections;

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.internal.serdes.collections.DocumentMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DocumentMapper interface.
 */
class DocumentMapperTest {

    @Test
    void shouldReturnIdentityMapper() {
        // Given
        Document doc = new Document().append("name", "John").append("age", 30);
        
        // When
        Document result = DocumentMapper.identity().map(doc);
        
        // Then
        assertThat(result).isSameAs(doc);
        assertThat(result.getString("name")).isEqualTo("John");
        assertThat(result.getInteger("age")).isEqualTo(30);
    }

    @Test
    void shouldTransformDocument() {
        // Given
        Document doc = new Document().append("email", "JOHN@EXAMPLE.COM");
        DocumentMapper normalizeEmail = d -> {
            if (d.containsKey("email")) {
                d.put("email", d.getString("email").toLowerCase());
            }
            return d;
        };
        
        // When
        Document result = normalizeEmail.map(doc);
        
        // Then
        assertThat(result.getString("email")).isEqualTo("john@example.com");
    }

    @Test
    void shouldAddFieldsToDocument() {
        // Given
        Document doc = new Document().append("name", "John");
        DocumentMapper addTimestamp = d -> {
            d.append("processedAt", Instant.now().toString());
            return d;
        };
        
        // When
        Document result = addTimestamp.map(doc);
        
        // Then
        assertThat(result.getString("name")).isEqualTo("John");
        assertThat(result.containsKey("processedAt")).isTrue();
    }

    @Test
    void shouldRemoveFieldsFromDocument() {
        // Given
        Document doc = new Document()
            .append("name", "John")
            .append("password", "secret123")
            .append("ssn", "123-45-6789");
        
        DocumentMapper removeSensitive = d -> {
            d.remove("password");
            d.remove("ssn");
            return d;
        };
        
        // When
        Document result = removeSensitive.map(doc);
        
        // Then
        assertThat(result.getString("name")).isEqualTo("John");
        assertThat(result.containsKey("password")).isFalse();
        assertThat(result.containsKey("ssn")).isFalse();
    }

    @Test
    void shouldChainMappers() {
        // Given
        Document doc = new Document()
            .append("email", "JOHN@EXAMPLE.COM")
            .append("password", "secret");
        
        DocumentMapper normalizeEmail = d -> {
            if (d.containsKey("email")) {
                d.put("email", d.getString("email").toLowerCase());
            }
            return d;
        };
        
        DocumentMapper removeSensitive = d -> {
            d.remove("password");
            return d;
        };
        
        DocumentMapper addTimestamp = d -> {
            d.append("processedAt", "2024-01-01");
            return d;
        };
        
        // When
        DocumentMapper combined = normalizeEmail
            .andThen(removeSensitive)
            .andThen(addTimestamp);
        Document result = combined.map(doc);
        
        // Then
        assertThat(result.getString("email")).isEqualTo("john@example.com");
        assertThat(result.containsKey("password")).isFalse();
        assertThat(result.getString("processedAt")).isEqualTo("2024-01-01");
    }

    @Test
    void shouldHandleNestedDocuments() {
        // Given
        Document nested = new Document().append("city", "New York");
        Document doc = new Document()
            .append("name", "John")
            .append("address", nested);
        
        DocumentMapper addCountry = d -> {
            if (d.containsKey("address")) {
                Document addr = (Document) d.get("address");
                addr.append("country", "USA");
            }
            return d;
        };
        
        // When
        Document result = addCountry.map(doc);
        
        // Then
        Document resultAddress = (Document) result.get("address");
        assertThat(resultAddress.getString("city")).isEqualTo("New York");
        assertThat(resultAddress.getString("country")).isEqualTo("USA");
    }
}

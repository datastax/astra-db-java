package com.datastax.astra.test.unit.tables;

import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.definition.rows.Row;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TableUpdateOperation.
 */
class TableUpdateOperationTest {

    // --------------------------------------------------
    // set() operations
    // --------------------------------------------------

    @Test
    void shouldSetSingleField() {
        TableUpdateOperation op = new TableUpdateOperation().set("name", "Alice");
        assertThat(op.getColumnMap()).containsKey("$set");
        @SuppressWarnings("unchecked")
        Map<String, Object> setOp = (Map<String, Object>) op.getColumnMap().get("$set");
        assertThat(setOp).containsEntry("name", "Alice");
    }

    @Test
    void shouldSetMultipleFields() {
        TableUpdateOperation op = new TableUpdateOperation()
                .set("name", "Bob")
                .set("age", 30);
        @SuppressWarnings("unchecked")
        Map<String, Object> setOp = (Map<String, Object>) op.getColumnMap().get("$set");
        assertThat(setOp).containsEntry("name", "Bob");
        assertThat(setOp).containsEntry("age", 30);
    }

    @Test
    void shouldSetFromRow() {
        Row row = new Row().addText("city", "Paris").addInt("zip", 75001);
        TableUpdateOperation op = new TableUpdateOperation().set(row);
        @SuppressWarnings("unchecked")
        Map<String, Object> setOp = (Map<String, Object>) op.getColumnMap().get("$set");
        assertThat(setOp).containsEntry("city", "Paris");
        assertThat(setOp).containsEntry("zip", 75001);
    }

    @Test
    void shouldHandleSetNullRow() {
        TableUpdateOperation op = new TableUpdateOperation().set((Row) null);
        assertThat(op.getColumnMap()).doesNotContainKey("$set");
    }

    // --------------------------------------------------
    // unset() operations
    // --------------------------------------------------

    @Test
    void shouldUnsetSingleField() {
        TableUpdateOperation op = new TableUpdateOperation().unset("name");
        assertThat(op.getColumnMap()).containsKey("$unset");
        @SuppressWarnings("unchecked")
        Map<String, Object> unsetOp = (Map<String, Object>) op.getColumnMap().get("$unset");
        assertThat(unsetOp).containsKey("name");
    }

    @Test
    void shouldUnsetMultipleFields() {
        TableUpdateOperation op = new TableUpdateOperation().unset("name", "age", "city");
        @SuppressWarnings("unchecked")
        Map<String, Object> unsetOp = (Map<String, Object>) op.getColumnMap().get("$unset");
        assertThat(unsetOp).containsKey("name");
        assertThat(unsetOp).containsKey("age");
        assertThat(unsetOp).containsKey("city");
    }

    @Test
    void shouldUnsetWithValue() {
        TableUpdateOperation op = new TableUpdateOperation().unset("field", "value");
        @SuppressWarnings("unchecked")
        Map<String, Object> unsetOp = (Map<String, Object>) op.getColumnMap().get("$unset");
        assertThat(unsetOp).containsEntry("field", "value");
    }

    // --------------------------------------------------
    // Combined set + unset
    // --------------------------------------------------

    @Test
    void shouldCombineSetAndUnset() {
        TableUpdateOperation op = new TableUpdateOperation()
                .set("name", "Alice")
                .unset("old_field");
        assertThat(op.getColumnMap()).containsKey("$set");
        assertThat(op.getColumnMap()).containsKey("$unset");
    }

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------

    @Test
    void shouldCreateFromJson() {
        TableUpdateOperation op = new TableUpdateOperation("{\"$set\":{\"name\":\"test\"}}");
        assertThat(op.getColumnMap()).containsKey("$set");
    }

    @Test
    void shouldCreateFromMap() {
        Map<String, Object> map = Map.of("$set", Map.of("name", "test"));
        TableUpdateOperation op = new TableUpdateOperation(map);
        assertThat(op.getColumnMap()).containsKey("$set");
    }

    @Test
    void shouldCreateEmpty() {
        TableUpdateOperation op = new TableUpdateOperation();
        assertThat(op.getColumnMap()).isEmpty();
    }

    // --------------------------------------------------
    // Null handling for unset
    // --------------------------------------------------

    @Test
    void shouldHandleUnsetNull() {
        TableUpdateOperation op = new TableUpdateOperation().unset((String[]) null);
        assertThat(op.getColumnMap()).doesNotContainKey("$unset");
    }
}

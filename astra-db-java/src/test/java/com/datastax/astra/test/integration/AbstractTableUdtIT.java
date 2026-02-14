package com.datastax.astra.test.integration;

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

import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTypeAddFields;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.commands.options.DropTypeOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for Table User-Defined Type (UDT) integration tests.
 * Tests the full UDT lifecycle: create, list, alter (add/rename fields),
 * use in table columns, insert/read/update, and drop.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableUdtIT extends AbstractDataAPITest {

    static final String UDT_ADDRESS = "udt_address_test";

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }
    static final String TABLE_WITH_UDT = "table_with_udt";

    // ------------------------------------------
    // Create UDT
    // ------------------------------------------

    @Test
    @Order(1)
    @DisplayName("01. Should create user-defined type")
    public void shouldCreateUserDefinedType() {
        getDatabase().dropType(UDT_ADDRESS, DropTypeOptions.IF_EXISTS);

        getDatabase().createType(UDT_ADDRESS, new TableUserDefinedTypeDefinition()
                .addFieldText("street")
                .addFieldText("city")
                .addFieldText("state")
                .addFieldInt("zipcode"), CreateTypeOptions.IF_NOT_EXISTS);

        List<String> typeNames = getDatabase().listTypeNames();
        assertThat(typeNames).contains(UDT_ADDRESS);
        log.info("Created UDT '{}'. All types: {}", UDT_ADDRESS, typeNames);
    }

    // ------------------------------------------
    // List UDTs
    // ------------------------------------------

    @Test
    @Order(2)
    @DisplayName("02. Should list user-defined types with details")
    public void shouldListUserDefinedTypes() {
        List<TableUserDefinedTypeDescriptor> types = getDatabase().listTypes();
        assertThat(types).isNotEmpty();

        boolean found = types.stream().anyMatch(t -> UDT_ADDRESS.equals(t.getUdtName()));
        assertThat(found).isTrue();
        types.forEach(t -> log.info("UDT: {} — {} field(s)", t.getUdtName(),
                t.getDefinition().getFields().size()));
    }

    // ------------------------------------------
    // Alter UDT — Add Fields
    // ------------------------------------------

    @Test
    @Order(3)
    @DisplayName("03. Should alter type — add fields")
    public void shouldAlterTypeAddFields() {
        getDatabase().alterType(UDT_ADDRESS,
                new AlterTypeAddFields()
                        .addFieldText("country")
                        .addFieldBoolean("primary_address"));

        List<TableUserDefinedTypeDescriptor> types = getDatabase().listTypes();
        TableUserDefinedTypeDescriptor udt = types.stream()
                .filter(t -> UDT_ADDRESS.equals(t.getUdtName()))
                .findFirst()
                .orElseThrow();
        assertThat(udt.getDefinition().getFields()).containsKey("country");
        assertThat(udt.getDefinition().getFields()).containsKey("primary_address");
        log.info("Added fields 'country' and 'primary_address' to UDT '{}'", UDT_ADDRESS);
    }

    // ------------------------------------------
    // Alter UDT — Rename Fields
    // ------------------------------------------

    @Test
    @Order(4)
    @DisplayName("04. Should alter type — rename fields")
    public void shouldAlterTypeRenameFields() {
        getDatabase().alterType(UDT_ADDRESS,
                new AlterTypeRenameFields().addField("country", "country_name"));

        List<TableUserDefinedTypeDescriptor> types = getDatabase().listTypes();
        TableUserDefinedTypeDescriptor udt = types.stream()
                .filter(t -> UDT_ADDRESS.equals(t.getUdtName()))
                .findFirst()
                .orElseThrow();
        assertThat(udt.getDefinition().getFields()).containsKey("country_name");
        assertThat(udt.getDefinition().getFields()).doesNotContainKey("country");
        log.info("Renamed field 'country' to 'country_name' in UDT '{}'", UDT_ADDRESS);
    }

    // ------------------------------------------
    // Create Table with UDT Column
    // ------------------------------------------

    @Test
    @Order(5)
    @DisplayName("05. Should create table with UDT column")
    public void shouldCreateTableWithUdtColumn() {
        getDatabase().dropTable(TABLE_WITH_UDT, IF_EXISTS);

        getDatabase().createTable(TABLE_WITH_UDT, new TableDefinition()
                .addColumnText("name")
                .addColumnUserDefinedType("address", UDT_ADDRESS)
                .addColumnListUserDefinedType("address_list", UDT_ADDRESS)
                .partitionKey("name"), IF_NOT_EXISTS);

        assertThat(getDatabase().tableExists(TABLE_WITH_UDT)).isTrue();
        log.info("Created table '{}' with UDT column", TABLE_WITH_UDT);
    }

    // ------------------------------------------
    // Insert and Read with UDT
    // ------------------------------------------

    @Test
    @Order(6)
    @DisplayName("06. Should insert and read row with UDT data")
    public void shouldInsertAndReadWithUdt() {
        Table<Row> table = getDatabase().getTable(TABLE_WITH_UDT);

        table.insertOne(new Row()
                .add("name", "cedrick")
                .add("address", Map.of(
                        "street", "123 Main St",
                        "city", "Paris",
                        "state", "IDF",
                        "zipcode", 75001)));

        Optional<Row> result = table.findOne(Filters.eq("name", "cedrick"));
        assertThat(result).isPresent();
        log.info("Read row with UDT: {}", result.get());
    }

    // ------------------------------------------
    // Update UDT Column
    // ------------------------------------------

    @Test
    @Order(7)
    @DisplayName("07. Should update UDT column")
    public void shouldUpdateUdtColumn() {
        Table<Row> table = getDatabase().getTable(TABLE_WITH_UDT);

        table.updateOne(Filters.eq("name", "cedrick"),
                new TableUpdateOperation().set("address", Map.of(
                        "street", "456 Rue de Rivoli",
                        "city", "Paris",
                        "state", "IDF",
                        "zipcode", 75004)));

        Optional<Row> result = table.findOne(Filters.eq("name", "cedrick"));
        assertThat(result).isPresent();
        log.info("Updated UDT column: {}", result.get());
    }

    // ------------------------------------------
    // UDT List Column
    // ------------------------------------------

    @Test
    @Order(8)
    @DisplayName("08. Should insert and read row with UDT list")
    public void shouldInsertAndReadWithUdtList() {
        Table<Row> table = getDatabase().getTable(TABLE_WITH_UDT);

        Map<String, Object> addr1 = Map.of("street", "10 Downing St", "city", "London", "state", "ENG", "zipcode", 10000);
        Map<String, Object> addr2 = Map.of("street", "1600 Penn Ave", "city", "Washington", "state", "DC", "zipcode", 20500);

        table.updateOne(Filters.eq("name", "cedrick"),
                new TableUpdateOperation().set("address_list", List.of(addr1, addr2)));

        Optional<Row> result = table.findOne(Filters.eq("name", "cedrick"));
        assertThat(result).isPresent();
        log.info("Read row with UDT list: {}", result.get());
    }

    // ------------------------------------------
    // Drop UDT
    // ------------------------------------------

    @Test
    @Order(9)
    @DisplayName("09. Should drop user-defined type")
    public void shouldDropUserDefinedType() {
        // Must drop table using the UDT first
        getDatabase().dropTable(TABLE_WITH_UDT, IF_EXISTS);

        getDatabase().dropType(UDT_ADDRESS, DropTypeOptions.IF_EXISTS);

        List<String> typeNames = getDatabase().listTypeNames();
        assertThat(typeNames).doesNotContain(UDT_ADDRESS);
        log.info("Dropped UDT '{}'. Remaining types: {}", UDT_ADDRESS, typeNames);
    }
}

package com.datastax.astra.test.integration;

import com.datastax.astra.client.Table;
import com.datastax.astra.client.model.tables.Row;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableITTest extends AbstractDataAPITest {

    /** Tested collection1. */
    protected static Table<Row> simpleTable;

    @Test
    public void shouldListTables() {
        assertThat(getDatabase().listTables().collect(Collectors.toList())).isNotNull();
    }

    @Test
    public void shouldInstanciateTable() {
        new Table()
        getDatabase().getCollection()
        assertThat(getDatabase().listTables().collect(Collectors.toList())).isNotNull();
    }



}

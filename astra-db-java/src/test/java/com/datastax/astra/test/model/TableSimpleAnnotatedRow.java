package com.datastax.astra.test.model;

import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.test.integration.local.LocalTableITTest;
import lombok.Builder;
import lombok.Data;

import static com.datastax.astra.client.core.query.SortOrder.ASCENDING;
import static com.datastax.astra.client.tables.columns.ColumnTypes.BOOLEAN;
import static com.datastax.astra.client.tables.columns.ColumnTypes.INT;
import static com.datastax.astra.client.tables.columns.ColumnTypes.TEXT;

@Data
@Builder
@EntityTable(LocalTableITTest.TABLE_SIMPLE)
public class TableSimpleAnnotatedRow {

    @PartitionBy(0)
    @Column(value="email", type= TEXT)
    private String email;

    @PartitionSort(position = 0, order = ASCENDING)
    @Column(value="age", type = INT)
    private Integer age;

    @Column(value="country", type = TEXT)
    private String country;

    @Column(value="name", type = TEXT)
    private String name;

    @Column(value="human", type = BOOLEAN)
    private Boolean human;

}

package com.datastax.astra.client.tables.mapping;

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import lombok.Data;

import java.lang.reflect.Method;

@Data
public class IntrospectedField {

    private String name;

    private Class<?> type;

    private Method getter;

    private Method setter;

    // @Column
    private String      columnName;
    private ColumnTypes columnType;

    // @PartitionBy
    private Integer     partitionByPosition;

    // @PartitionSort
    private Integer     partitionSortPosition;
    private SortOrder   partitionSortOrder;
}

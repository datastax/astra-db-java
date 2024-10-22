package com.datastax.astra.test.model;

import com.datastax.astra.client.tables.annotations.Column;
import com.datastax.astra.client.tables.annotations.Table;
import com.datastax.astra.test.integration.local.LocalTableITTest;

@Table(LocalTableITTest.TABLE_SIMPLE)
public class TableSimpleRow {

    @Column("email")
    private String email;

    @Column("human")
    private Boolean human;

    @Column("age")
    private Integer age;

    @Column("country")
    private String country;

    @Column("name")
    private String name;
}

package com.datastax.astra.client.tables.mapping;

import com.datastax.astra.client.tables.columns.ColumnTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name();

    ColumnTypes type();
}

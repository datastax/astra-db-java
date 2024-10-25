package com.datastax.astra.client.tables.mapping;

import com.datastax.astra.client.core.query.SortOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionSort {

    int position() default 0;

    SortOrder order() default SortOrder.ASCENDING;

}

package com.datastax.astra.client.tables.mapping;

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

import com.datastax.astra.client.core.query.SortOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines sorting behavior for partitioned fields or methods. This annotation allows specifying
 * the position and sort order within a partition for annotated elements.
 * <p>
 * Apply this annotation to fields or methods to control their ordering when partitioned data is sorted.
 * The {@code position()} specifies the sorting priority, and the {@code order()} determines whether
 * the sorting is ascending or descending.
 * </p>
 *
 * <p><b>Retention:</b> {@code RUNTIME}</p>
 * This annotation is retained at runtime to enable runtime reflection.
 *
 * <p><b>Target:</b> {@code FIELD}, {@code METHOD}</p>
 * This annotation can be applied to fields or methods.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionSort {

    /**
     * Specifies the sorting position for the annotated field or method.
     * Fields or methods with lower positions are sorted before those with higher positions.
     *
     * @return an integer indicating the sorting position
     */
    int position();

    /**
     * Specifies the sorting order (ascending or descending) for the annotated field or method.
     * The default value is {@code SortOrder.ASCENDING}.
     *
     * @return the sorting order
     */
    SortOrder order() default SortOrder.ASCENDING;
}


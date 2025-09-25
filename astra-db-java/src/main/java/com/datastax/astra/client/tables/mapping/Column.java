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

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define properties for a database column. This annotation can be used on fields
 * to specify custom column names, types, and additional properties.
 *
 * <p>The {@code Column} annotation provides flexibility for mapping fields to database columns,
 * with options to customize column name, type, and other attributes.</p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Specifies the name of the column. If not provided, the field's name will be used.
     *
     * @return the custom column name or an empty string if not set
     */
    String name();

    /**
     * Specifies the name of the column. If not provided, the field's name will be used.
     *
     * @return the custom column name or an empty string if not set
     */
    String udtName() default "";

    /**
     * Specifies the type of the column. If not provided, the field's type will be used.
     *
     * @return the column type or {@link TableColumnTypes#UNDEFINED} if not set
     */
    TableColumnTypes type() default TableColumnTypes.UNDEFINED;

    /**
     * Specifies the value type of the column, typically used for complex data structures.
     * If not provided, defaults to {@link TableColumnTypes#UNDEFINED}.
     *
     * @return the value type or {@link TableColumnTypes#UNDEFINED} if not set
     */
    TableColumnTypes valueType() default TableColumnTypes.UNDEFINED;

    /**
     * Specifies the key type of the column, typically used for mapping keys in key-value pairs.
     * If not provided, defaults to {@link TableColumnTypes#UNDEFINED}.
     *
     * @return the key type or {@link TableColumnTypes#UNDEFINED} if not set
     */
    TableColumnTypes keyType() default TableColumnTypes.UNDEFINED;

}

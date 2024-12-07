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

import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;

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
     * Specifies the type of the column. If not provided, the field's type will be used.
     *
     * @return the column type or {@link ColumnTypes#UNDEFINED} if not set
     */
    ColumnTypes type() default ColumnTypes.UNDEFINED;

    /**
     * Specifies the value type of the column, typically used for complex data structures.
     * If not provided, defaults to {@link ColumnTypes#UNDEFINED}.
     *
     * @return the value type or {@link ColumnTypes#UNDEFINED} if not set
     */
    ColumnTypes valueType() default ColumnTypes.UNDEFINED;

    /**
     * Specifies the key type of the column, typically used for mapping keys in key-value pairs.
     * If not provided, defaults to {@link ColumnTypes#UNDEFINED}.
     *
     * @return the key type or {@link ColumnTypes#UNDEFINED} if not set
     */
    ColumnTypes keyType() default ColumnTypes.UNDEFINED;

    /**
     * Specifies the dimension of the column, usually relevant for multidimensional data.
     * Defaults to -1, indicating unspecified dimensions.
     *
     * @return the dimension value or -1 if not set
     */
    int dimension() default -1;

    /**
     * Specifies the similarity metric for the column. Defaults to {@link SimilarityMetric#COSINE}.
     *
     * @return the similarity metric
     */
    SimilarityMetric metric() default SimilarityMetric.COSINE;
}

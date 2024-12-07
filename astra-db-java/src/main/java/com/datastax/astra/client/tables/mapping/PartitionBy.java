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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a partition key for fields or methods. This annotation is used to define the partitioning
 * value that groups or categorizes data elements.
 * <p>
 * Apply this annotation to fields or methods in classes where partitioning logic is required.
 * The {@code value()} method defines the partition key as an integer.
 * </p>
 *
 * <p><b>Retention:</b> {@code RUNTIME}</p>
 * This annotation is retained at runtime to allow runtime reflection.
 *
 * <p><b>Target:</b> {@code FIELD}, {@code METHOD}</p>
 * This annotation can be applied to fields or methods.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionBy {
    /**
     * The partition key value for the annotated field or method.
     *
     * @return an integer representing the partition key
     */
    int value();
}

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

import com.datastax.astra.client.tables.row.Row;

public class TableRowMapper<T> {

    Class<T> beanClass;

    public TableRowMapper(Class<T> clazz) {
        this.beanClass = clazz;
        if (beanClass.getAnnotation(Table.class) == null) {
            throw new IllegalArgumentException("Class " + beanClass.getName() + " is not annotated with @Table");
        }
        IntrospectedBean<T> rowDecorator = new IntrospectedBean<>(beanClass);
        System.out.println("Table Name: " + rowDecorator.getName());
        for (IntrospectedField field : rowDecorator.getFields().values()) {
            System.out.println("Field: " + field.getName() + ", Type: " + field.getType().getName());
        }
    }

    public Row mapAsRow(T object) {
        return null;
    }

    public T mapFromRow(Row object) {
        return null;
    }

}

package com.datastax.astra.client.tables.definition.indexes;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Function;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "indexType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TableRegularIndexDescriptor.class, name = "regular"),
    @JsonSubTypes.Type(value = TableVectorIndexDescriptor.class, name = "vector")
})
@Getter @Setter
public abstract class TableIndexDescriptor<DEF extends TableIndexDefinition<?>> {

    /**
     * The name of the table.
     */
    protected String name;

    /**
     * The type of index (vector, regular)
     */
    protected String indexType;

    /**
     * The options defining the table's index characteristics.
     */
    protected DEF definition;

    /**
     * Function to create a new instance of the subclass.
     */
    @JsonIgnore
    protected final Function<DEF, ? extends TableIndexDescriptor<DEF>> constructor;

    /**
     * Default constructor for serialization/deserialization.
     */
    /**
     * Constructor that accepts a function for instance creation.
     */
    protected TableIndexDescriptor(Function<DEF, ? extends TableIndexDescriptor<DEF>> constructor) {
        this.constructor = constructor;
    }

    /**
     * Generic method to create a new instance with modified properties.
     *
     * @param updater Consumer function to modify the new instance.
     * @return A new modified instance.
     */
    protected TableIndexDescriptor<DEF> mapImpl(Consumer<TableIndexDescriptor<DEF>> updater) {
        TableIndexDescriptor<DEF> newInstance = constructor.apply(this.definition);
        newInstance.indexType = this.indexType;
        newInstance.definition = this.definition;
        updater.accept(newInstance);
        return newInstance;
    }

    /**
     * Sets the name of the table.
     *
     * @param name the name of the table.
     * @return a new instance with the updated table name.
     */
    public TableIndexDescriptor<DEF> name(String name) {
        return mapImpl(desc -> desc.name = name);
    }

    /**
     * Sets the index definition for the table.
     *
     * @param def the {@link TableIndexDefinition} defining the index options for the table.
     * @return a new instance with the updated definition.
     */
    public TableIndexDescriptor<DEF> definition(DEF def) {
        return mapImpl(desc -> desc.definition = def);
    }

    /**
     * Converts the object to a string using the {@link RowSerializer}.
     *
     * @return a string representation of this {@code TableIndexDescriptor}.
     */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }

}

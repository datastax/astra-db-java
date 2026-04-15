package com.datastax.astra.internal.reflection;

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

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.mapping.DataApiCollection;
import com.datastax.astra.client.collections.mapping.DocumentId;
import com.datastax.astra.client.collections.mapping.Lexical;
import com.datastax.astra.client.collections.mapping.Vector;
import com.datastax.astra.client.collections.mapping.Vectorize;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides introspection and metadata for a collection document entity.
 * <p>
 * This class inspects a JavaBean of type {@code T} annotated with {@link DataApiCollection}
 * to extract and manage metadata about its properties, particularly the document ID field
 * marked with {@link DocumentId}.
 * </p>
 *
 * @param <T> the type of the collection document entity being introspected
 */
@Slf4j
@Data
public class CollectionRecordDefinition<T> {

    /** Class introspected. */
    private final Class<T> clazz;

    /** Collection name. */
    private final String collectionName;

    /** All fields in the bean. */
    private final Map<String, EntityFieldDefinition> fields;

    /** The field marked with @DocumentId. */
    private EntityFieldDefinition idField;

    /** The field marked with @Vectorize. */
    private EntityFieldDefinition vectorizeField;

    /** The field marked with @Lexical. */
    private EntityFieldDefinition lexicalField;

    /** The field marked with @Vector. */
    private EntityFieldDefinition vectorField;

    /**
     * Mapper for the serialization
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Constructor for the collection record definition.
     *
     * @param clazz the class type
     */
    public CollectionRecordDefinition(Class<T> clazz) {
        this.clazz = clazz;
        this.fields = new HashMap<>();

        // Collection Name
        DataApiCollection collectionAnn = clazz.getAnnotation(DataApiCollection.class);
        if (collectionAnn != null && !collectionAnn.value().isEmpty()) {
            this.collectionName = collectionAnn.value();
        } else {
            this.collectionName = clazz.getSimpleName().toLowerCase();
        }

        // Find properties
        List<BeanPropertyDefinition> properties = OBJECT_MAPPER
                .getSerializationConfig()
                .introspect(TypeFactory.defaultInstance().constructType(clazz))
                .findProperties();

        // Fields
        for (BeanPropertyDefinition property : properties) {
            EntityFieldDefinition field = new EntityFieldDefinition();
            field.setName(property.getName());
            field.setType(property.getPrimaryType().getRawClass());
            field.setJavaType(property.getPrimaryType());

            // Set getter and setter
            if (property.getGetter() != null) {
                field.setGetter(property.getGetter().getAnnotated());
            }
            if (property.getSetter() != null) {
                field.setSetter(property.getSetter().getAnnotated());
            }

            // Check for @DocumentId, @Vectorize, @Lexical, and @Vector annotations
            AnnotatedField annfield = property.getField();
            if (annfield != null) {
                // Count how many special annotations are present on this field
                int annotationCount = 0;
                DocumentId documentIdAnn = annfield.getAnnotated().getAnnotation(DocumentId.class);
                Vectorize vectorizeAnn = annfield.getAnnotated().getAnnotation(Vectorize.class);
                Lexical lexicalAnn = annfield.getAnnotated().getAnnotation(Lexical.class);
                Vector vectorAnn = annfield.getAnnotated().getAnnotation(Vector.class);

                if (documentIdAnn != null) annotationCount++;
                if (vectorizeAnn != null) annotationCount++;
                if (lexicalAnn != null) annotationCount++;
                if (vectorAnn != null) annotationCount++;

                // Validate that only one special annotation is present
                if (annotationCount > 1) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' in class '%s' can only have one of @DocumentId, @Vectorize, @Lexical, or @Vector annotations.",
                            field.getName(), clazz.getName()));
                }

                if (documentIdAnn != null) {
                    if (this.idField != null) {
                        throw new IllegalArgumentException(String.format(
                                "Multiple fields annotated with @DocumentId in class '%s'. Only one field can be marked as document ID.",
                                clazz.getName()));
                    }
                    this.idField = field;
                }

                if (vectorizeAnn != null) {
                    if (this.vectorizeField != null) {
                        throw new IllegalArgumentException(String.format(
                                "Multiple fields annotated with @Vectorize in class '%s'. Only one field can be marked for vectorization.",
                                clazz.getName()));
                    }
                    if (!String.class.equals(field.getType())) {
                        throw new IllegalArgumentException(String.format(
                                "Field '%s' annotated with @Vectorize in class '%s' must be of type String.",
                                field.getName(), clazz.getName()));
                    }
                    this.vectorizeField = field;
                }

                if (lexicalAnn != null) {
                    if (this.lexicalField != null) {
                        throw new IllegalArgumentException(String.format(
                                "Multiple fields annotated with @Lexical in class '%s'. Only one field can be marked for lexical search.",
                                clazz.getName()));
                    }
                    if (!String.class.equals(field.getType())) {
                        throw new IllegalArgumentException(String.format(
                                "Field '%s' annotated with @Lexical in class '%s' must be of type String.",
                                field.getName(), clazz.getName()));
                    }
                    this.lexicalField = field;
                }

                if (vectorAnn != null) {
                    if (this.vectorField != null) {
                        throw new IllegalArgumentException(String.format(
                                "Multiple fields annotated with @Vector in class '%s'. Only one field can be marked as vector.",
                                clazz.getName()));
                    }
                    if (!float[].class.equals(field.getType()) && !DataAPIVector.class.equals(field.getType())) {
                        throw new IllegalArgumentException(String.format(
                                "Field '%s' annotated with @Vector in class '%s' must be of type float[] or DataAPIVector.",
                                field.getName(), clazz.getName()));
                    }
                    this.vectorField = field;
                }
            }

            fields.put(field.getName(), field);
        }
    }

    /**
     * Gets the ID value from the given instance.
     *
     * @param instance the instance to extract the ID from
     * @return the ID value, or null if no @DocumentId field is defined or the value is null
     * @throws IllegalStateException if the ID field cannot be accessed
     */
    public Object getId(T instance) {
        if (instance == null) {
            return null;
        }

        if (idField == null) {
            throw new IllegalStateException(String.format(
                    "No field annotated with @DocumentId found in class '%s'",
                    clazz.getName()));
        }

        Method getter = idField.getGetter();
        if (getter == null) {
            throw new IllegalStateException(String.format(
                    "No getter method found for @DocumentId field '%s' in class '%s'",
                    idField.getName(), clazz.getName()));
        }

        try {
            return getter.invoke(instance);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Failed to get ID value from field '%s' in class '%s'",
                    idField.getName(), clazz.getName()), e);
        }
    }

    /**
     * Checks if this collection record has a field annotated with @DocumentId.
     *
     * @return true if an ID field exists, false otherwise
     */
    public boolean hasIdField() {
        return idField != null;
    }

    /**
     * Gets the name of the ID field.
     *
     * @return the ID field name, or null if no @DocumentId field is defined
     */
    public String getIdFieldName() {
        return idField != null ? idField.getName() : null;
    }

    /**
     * Gets the vectorize value from the given instance.
     *
     * @param instance the instance to extract the vectorize value from
     * @return the vectorize value, or null if no @Vectorize field is defined or the value is null
     * @throws IllegalStateException if the vectorize field cannot be accessed
     */
    public String getVectorize(T instance) {
        if (instance == null) {
            return null;
        }

        if (vectorizeField == null) {
            return null;
        }

        Method getter = vectorizeField.getGetter();
        if (getter == null) {
            throw new IllegalStateException(String.format(
                    "No getter method found for @Vectorize field '%s' in class '%s'",
                    vectorizeField.getName(), clazz.getName()));
        }

        try {
            return (String) getter.invoke(instance);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Failed to get vectorize value from field '%s' in class '%s'",
                    vectorizeField.getName(), clazz.getName()), e);
        }
    }

    /**
     * Checks if this collection record has a field annotated with @Vectorize.
     *
     * @return true if a vectorize field exists, false otherwise
     */
    public boolean hasVectorizeField() {
        return vectorizeField != null;
    }

    /**
     * Gets the name of the vectorize field.
     *
     * @return the vectorize field name, or null if no @Vectorize field is defined
     */
    public String getVectorizeFieldName() {
        return vectorizeField != null ? vectorizeField.getName() : null;
    }

    /**
     * Gets the lexical value from the given instance.
     *
     * @param instance the instance to extract the lexical value from
     * @return the lexical value, or null if no @Lexical field is defined or the value is null
     * @throws IllegalStateException if the lexical field cannot be accessed
     */
    public String getLexical(T instance) {
        if (instance == null) {
            return null;
        }

        if (lexicalField == null) {
            return null;
        }

        Method getter = lexicalField.getGetter();
        if (getter == null) {
            throw new IllegalStateException(String.format(
                    "No getter method found for @Lexical field '%s' in class '%s'",
                    lexicalField.getName(), clazz.getName()));
        }

        try {
            return (String) getter.invoke(instance);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Failed to get lexical value from field '%s' in class '%s'",
                    lexicalField.getName(), clazz.getName()), e);
        }
    }

    /**
     * Checks if this collection record has a field annotated with @Lexical.
     *
     * @return true if a lexical field exists, false otherwise
     */
    public boolean hasLexicalField() {
        return lexicalField != null;
    }

    /**
     * Gets the name of the lexical field.
     *
     * @return the lexical field name, or null if no @Lexical field is defined
     */
    public String getLexicalFieldName() {
        return lexicalField != null ? lexicalField.getName() : null;
    }

    /**
     * Gets the vector value from the given instance.
     *
     * @param instance the instance to extract the vector value from
     * @return the vector value (float[] or DataAPIVector), or null if no @Vector field is defined or the value is null
     * @throws IllegalStateException if the vector field cannot be accessed
     */
    public Object getVector(T instance) {
        if (instance == null) {
            return null;
        }

        if (vectorField == null) {
            return null;
        }

        Method getter = vectorField.getGetter();
        if (getter == null) {
            throw new IllegalStateException(String.format(
                    "No getter method found for @Vector field '%s' in class '%s'",
                    vectorField.getName(), clazz.getName()));
        }

        try {
            return getter.invoke(instance);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Failed to get vector value from field '%s' in class '%s'",
                    vectorField.getName(), clazz.getName()), e);
        }
    }

    /**
     * Checks if this collection record has a field annotated with @Vector.
     *
     * @return true if a vector field exists, false otherwise
     */
    public boolean hasVectorField() {
        return vectorField != null;
    }

    /**
     * Gets the name of the vector field.
     *
     * @return the vector field name, or null if no @Vector field is defined
     */
    public String getVectorFieldName() {
        return vectorField != null ? vectorField.getName() : null;
    }

    /**
     * Builds a CollectionDefinition from the @DataApiCollection annotation properties.
     *
     * @return a CollectionDefinition configured according to the annotation, or null if no annotation present
     */
    public CollectionDefinition buildCollectionDefinition() {
        DataApiCollection annotation = clazz.getAnnotation(DataApiCollection.class);
        if (annotation == null) {
            return null;
        }

        CollectionDefinition definition = new CollectionDefinition();

        // DefaultId
        if (annotation.defaultIdType() != null && !annotation.defaultIdType().isEmpty()) {
            definition.defaultId(CollectionDefaultIdTypes.fromValue(annotation.defaultIdType()));
        }

        // Indexing options
        if (annotation.indexingDeny().length > 0 && annotation.indexingAllow().length > 0) {
            throw new IllegalArgumentException(String.format(
                    "Class '%s' has both indexingDeny and indexingAllow specified. These are mutually exclusive.",
                    clazz.getName()));
        }
        if (annotation.indexingDeny().length > 0) {
            definition.indexingDeny(annotation.indexingDeny());
        }
        if (annotation.indexingAllow().length > 0) {
            definition.indexingAllow(annotation.indexingAllow());
        }

        // Vector options
        if (annotation.vectorDimension() > 0) {
            definition.vector(annotation.vectorDimension(), annotation.vectorSimilarity());
        }

        // Vectorize options
        if (Utils.hasLength(annotation.vectorizeProvider()) && Utils.hasLength(annotation.vectorizeModel())) {
            if (Utils.hasLength(annotation.vectorizeSharedSecret())) {
                definition.vectorize(annotation.vectorizeProvider(), annotation.vectorizeModel(), 
                        annotation.vectorizeSharedSecret());
            } else {
                definition.vectorize(annotation.vectorizeProvider(), annotation.vectorizeModel());
            }
        }

        // Lexical options
        if (!annotation.lexicalEnabled()) {
            definition.disableLexical();
        } else if (annotation.lexicalAnalyzer() != null) {
            definition.lexical(new Analyzer(annotation.lexicalAnalyzer()));
        }

        // Rerank options
        if (annotation.rerankEnabled()) {
            if (Utils.hasLength(annotation.rerankProvider()) && Utils.hasLength(annotation.rerankModel())) {
                definition.rerank(annotation.rerankProvider(), annotation.rerankModel());
            } else {
                throw new IllegalArgumentException(String.format(
                        "Class '%s' has rerankEnabled=true but missing rerankProvider or rerankModel",
                        clazz.getName()));
            }
        }

        return definition;
    }
}

package com.datastax.astra.client.core.hybrid;

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

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.lexical.Lexical;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.Vectorize;
import com.datastax.astra.internal.serdes.core.HybridSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Hybrid object that can be used to store both vector and lexical information.
 */
@Data
@JsonSerialize(using = HybridSerializer.class)
public class Hybrid {

    /**
     * Document to use.
     */
    final Document doc;

    /**
     * Default constructor.
     */
    public Hybrid() {
        this.doc = new Document();
    }

    /**
     * Constructor with text.
     *
     * @param text
     *      text to use
     */
    public Hybrid(String text) {
        this();
        vectorize(text);
        lexical(text);
    }

    /**
     * Constructor with custom document.
     *
     * @param doc
     *      document to use
     */
    public Hybrid(Document doc) {
        this.doc = doc;
    }

    /**
     * Add a vectorize field.
     *
     * @param vectorize
     *      vectorize to use
     * @return
     *      this
     */
    public Hybrid vectorize(String vectorize) {
        this.doc.append(DataAPIKeywords.VECTORIZE.getKeyword(), vectorize);
        return this;
    }

    /**
     * Add a vectorize field.
     *
     * @param vectorize
     *      vectorize to use
     * @return
     *      this
     */
    public Hybrid vectorize(Vectorize vectorize) {
        this.doc.append(DataAPIKeywords.VECTORIZE.getKeyword(), vectorize);
        return this;
    }

    /**
     * Add a lexical field.
     *
     * @param lexical
     *      lexical to use
     * @return
     *      this
     */
    public Hybrid lexical(String lexical) {
        this.doc.append(DataAPIKeywords.LEXICAL.getKeyword(), lexical);
        return this;
    }

    /**
     * Add a lexical field.
     *
     * @param lexical
     *      lexical to use
     * @return
     *      this
     */
    public Hybrid lexical(Lexical lexical) {
        this.doc.append(DataAPIKeywords.LEXICAL.getKeyword(), lexical);
        return this;
    }

    /**
     * Add a vector that will be serialized as a float array.
     *
     * @param embeddings
     *      embeddings to use
     * @return
     *      this
     */
    public Hybrid vector(float[] embeddings) {
        this.doc.append(DataAPIKeywords.VECTOR.getKeyword(), embeddings);
        return this;
    }

    /**
     * Add a vector that will be serialized as a base64 encoded string..
     *
     * @param vector
     *      vector to use
     * @return
     *      this
     */
    public Hybrid vector(DataAPIVector vector) {
        this.doc.append(DataAPIKeywords.VECTOR.getKeyword(), vector);
        return this;
    }



}

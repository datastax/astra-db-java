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
import com.datastax.astra.client.core.vectorize.Vectorize;
import com.datastax.astra.internal.serdes.core.HybridSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize(using = HybridSerializer.class)
public class Hybrid {

    final Document doc;

    public Hybrid(String text) {
        this.doc = new Document()
                .append(DataAPIKeywords.VECTORIZE.getKeyword(), text)
                .append(DataAPIKeywords.LEXICAL.getKeyword(), text);
    }

    public Hybrid(String vectorize, String lexical) {
        this.doc = new Document()
                .append(DataAPIKeywords.VECTORIZE.getKeyword(), vectorize)
                .append(DataAPIKeywords.LEXICAL.getKeyword(), lexical);
    }

    public Hybrid(Vectorize vectorize, Lexical lexical) {
        this.doc = new Document()
                .append(DataAPIKeywords.VECTORIZE.getKeyword(), vectorize)
                .append(DataAPIKeywords.LEXICAL.getKeyword(), lexical);
    }

    public Hybrid(Document doc) {
        this.doc = doc;
    }

}

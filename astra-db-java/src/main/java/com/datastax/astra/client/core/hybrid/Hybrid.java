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

import com.datastax.astra.client.core.lexical.Lexical;
import com.datastax.astra.client.core.vectorize.Vectorize;
import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Hybrid {

    @JsonProperty("$vectorize")
    final Vectorize vectorize;

    @JsonProperty("$lexical")
    final Lexical lexical;

    public Hybrid(String text) {
        this(new Vectorize(text), new Lexical(text));
    }

    public Hybrid(String vectorize, String lexical) {
        this(new Vectorize(vectorize), new Lexical(lexical));
    }

    public Hybrid(Vectorize vectorize, Lexical lexical) {
        Assert.notNull(vectorize, "vectorize cannot be null");
        Assert.notNull(lexical, "lexical cannot be null");
        this.vectorize = vectorize;
        this.lexical = lexical;
    }

}

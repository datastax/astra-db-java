package com.datastax.astra.client.core.hybrid;

import lombok.Getter;

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
@Getter
public enum HybridProjection {

    /**
     * Indicates that the projection will not retrieve any sorting informations
     */
    NONE("none"),

    /**
     * Indicates that the projection will not retrieve the 'passage:setPassage' as key value
     */
    PASSAGE("passage"),

    /**
     * Indicates that the projection will not retrieve the extended scores
     */
    SCORES("scores");

    String value;

    HybridProjection(String value) {
       this.value = value;
    }
}

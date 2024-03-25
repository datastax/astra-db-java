package com.datastax.astra.internal;

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

import java.util.function.Supplier;

/**
 * To work the APi needs a token. 
 * It can be static or dynamically generated.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public interface TokenProvider extends Supplier<String> {
    
    /** {@inheritDoc} */
    @Override
    default String get() {
        return getToken();
    }
    
    /**
     * Building the token.
     *
     * @return
     *      current token
     */
    String getToken();

}

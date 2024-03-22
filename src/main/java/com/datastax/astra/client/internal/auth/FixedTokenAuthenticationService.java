package com.datastax.astra.client.internal.auth;

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

/**
 * Static token, never expires..
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FixedTokenAuthenticationService implements TokenProvider {

    /** Reference to token. */
    private String token;
    
    /**
     * Constructor with all parameters.
     *
     * @param token
     *      static token to be used
     */
    public FixedTokenAuthenticationService(String token) {
        this.token = token;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getToken() {
        return token;
    }

}

package com.datastax.astra.client.core.auth;

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

import com.datastax.astra.internal.http.RetryHttpClient;
import com.datastax.astra.internal.utils.Assert;

import java.util.Base64;

/**
 * Creating a token with base64 encoded credentials.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UsernamePasswordTokenProvider implements TokenProvider {

    /** Default username for Cassandra. */
    public static final String DEFAULT_USERNAME      = "cassandra";

    /** Default password for Cassandra. */
    public static final String DEFAULT_CREDENTIALS = "cassandra";

    /** Default URL for local deployments. */
    public static final String DEFAULT_URL = "http://localhost:8181";

    /** Storing an authentication token to speed up queries. */
    private final String token;

    /**  Using defaults settings. */
    public UsernamePasswordTokenProvider() {
        this(DEFAULT_USERNAME, DEFAULT_CREDENTIALS);
    }

    /**
     * Full-fledged constructor.
     *
     * @param username
     *      username
     * @param password
     *      password
     */
    public UsernamePasswordTokenProvider(String username, String password) {
        Assert.hasLength(username, "username");
        Assert.hasLength(password, "password");
        Base64.Encoder encoder = Base64.getEncoder();
        this.token = "Cassandra:" +
                Base64.getEncoder().encodeToString(username.getBytes()) + ":" +
                Base64.getEncoder().encodeToString(password.getBytes());
    }

    /**
     * Generate or renew authentication token.
     *
     * @return String
     */
    @Override
    public String getToken() {
        return token;
    }

}

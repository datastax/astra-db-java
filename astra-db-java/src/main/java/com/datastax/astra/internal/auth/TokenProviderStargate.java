package com.datastax.astra.internal.auth;

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

import com.datastax.astra.internal.api.ApiConstants;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.datastax.astra.internal.http.RetryHttpClient;
import com.datastax.astra.internal.utils.JsonUtils;
import com.datastax.astra.internal.utils.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Using the authentication endpoint you should be able tp...
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class TokenProviderStargate implements TokenProvider, ApiConstants {

    /** Simple Client. */
    public static RetryHttpClient httpClient = new RetryHttpClient();

    /** Default username for Cassandra. */
    public static final String DEFAULT_USERNAME      = "cassandra";
    
    /** Default password for Cassandra. */
    public static final String DEFAULT_CREDENTIALS = "cassandra";
    
    /** Default URL for a Stargate node. */
    public static final String DEFAULT_AUTH_URL      = "http://localhost:8081";
    
    /** Default Timeout for Stargate token (1800s). */
    public static Duration DEFAULT_TIMEOUT_TOKEN = Duration.ofMinutes(30);

    /** Credentials. */
    private final String username;

    /** Credentials. */
    private final String password;

    /** Authentication token, time to live. */
    private final Duration tokenTtl = DEFAULT_TIMEOUT_TOKEN;
    
    /** Mark the token update. */
    private long tokenCreationTime = 0;
    
    /** Storing an authentication token to speed up queries. */
    private String token;

    private final String authenticationUrl;

    /**  Using defaults settings. */
    public TokenProviderStargate() {
        this(DEFAULT_USERNAME, DEFAULT_CREDENTIALS, DEFAULT_AUTH_URL);
    }

    /**
     * Full-fledged constructor.
     *
     * @param username
     *      username
     * @param password
     *      password
     * @param url
     *      endpoint to authenticate.
     */
    public TokenProviderStargate(String username, String password, String url) {
        Assert.hasLength(username, "username");
        Assert.hasLength(password, "password");
        Assert.hasLength(url, "Url list");
        this.username = username;
        this.password = password;
        this.authenticationUrl = url;
    }

    /**
     * Generate or renew authentication token.
     *
     * @return String
     */
    @Override
    public String getToken() {
        if ((System.currentTimeMillis() - tokenCreationTime) > 1000 * tokenTtl.getSeconds()) {
            token = renewToken();
            tokenCreationTime = System.currentTimeMillis();
        }
        return token;
    }

    /**
     * If token is null or too old (X seconds) renew the token.
     *
     * @return
     *      new value for a token
     */
    private String renewToken() {
        String body = JsonUtils.marshall(Map.of("username", username, "password", password));
        try {
            HttpRequest request = HttpRequest.newBuilder()
              .uri(new URI(authenticationUrl + "/v1/auth"))
              .method("POST", HttpRequest.BodyPublishers.ofString(body))
              .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
              .header(HEADER_USER_AGENT, REQUEST_WITH)
              .header(HEADER_REQUEST_ID, UUID.randomUUID().toString())
              .header(HEADER_REQUESTED_WITH, REQUEST_WITH)
              .build();

            // Reuse Execute HTTP for the retry mechanism
            ApiResponseHttp response = httpClient.executeHttp(request, true);
            if (response !=null) {
                if (201 == response.getCode() || 200 == response.getCode()) {
                    return (String) JsonUtils.unMarshallBean(response.getBody(), Map.class).get("authToken");
                }
            }
            String errorMessage = (response != null) ? response.getBody() : "no response";
            throw new IllegalStateException("Cannot generate authentication token " + errorMessage);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Authentication URL was invalid " + authenticationUrl, e);
        }
    }

}

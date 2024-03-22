package com.datastax.astra.client.internal;

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
 * Group constants on a dedicated interface.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public interface ApiConstants {

    /** Headers, Api is usig JSON */
    String CONTENT_TYPE_JSON        = "application/json";
    
    /** Headers, Api is usig JSON */
    String CONTENT_TYPE_GRAPHQL     = "application/graphql";
    
    /** Header param. */
    String HEADER_ACCEPT            = "Accept";
    
    /** Headers param to insert the token. */
    String HEADER_CASSANDRA         = "X-Cassandra-Token";
    
    /** Headers param to insert the unique identifier for the request. */
    String HEADER_REQUEST_ID        = "X-Cassandra-Request-Id";
    
    /** Headers param to insert the conte type. */
    String HEADER_CONTENT_TYPE      = "Content-Type";
    
    /** Headers param to insert the token for devops API. */
    String HEADER_AUTHORIZATION     = "Authorization";
    
    /** Headers name to insert the user agent identifying the client. */
    String HEADER_USER_AGENT        = "User-Agent";
    
    /** Headers param to insert the user agent identifying the client. */
    String HEADER_REQUESTED_WITH    = "X-Requested-With";
    
    /** Value for the requested with. */
    String REQUEST_WITH = "data-api-client-java";

}

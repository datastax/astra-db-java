package com.datastax.astra.client.internal.http;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Response HTTP.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ApiResponseHttp {
    
    /** JSON String. */
    private final String body;
    
    /** Http status code. */
    private final int code;
    
    /** Http Headers. **/
    private Map<String, String> headers = new HashMap<>();
    
    /**
     * Defaut constructor.
     * 
     * @param body
     *      request body
     * @param code
     *      request code
     */
    public ApiResponseHttp(String body, int code) {
        this.body = body;
        this.code = code;
    }
    
    /**
     * Full constructor.
     * 
     * @param body
     *      request body
     * @param code
     *      request code
     * @param headers
     *      request headers      
     */
    public ApiResponseHttp(String body, int code, Map<String, String> headers) {
        this.body = body;
        this.code = code;
        this.headers = headers;
    }

    /**
     * Getter accessor for attribute 'body'.
     *
     * @return
     *       current value of 'body'
     */
    public String getBody() {
        return body;
    }

    /**
     * Getter accessor for attribute 'code'.
     *
     * @return
     *       current value of 'code'
     */
    public int getCode() {
        return code;
    }

    /**
     * Getter accessor for attribute 'headers'.
     *
     * @return
     *       current value of 'headers'
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    

}

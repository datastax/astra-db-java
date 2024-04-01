package com.datastax.astra.internal.api;

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

import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Create an endpoint to connect to a database.
 */
@Getter
@Setter
public class AstraApiEndpoint {

    /**
     * Current Environment
     */
    AstraEnvironment env;

    /**
     * Database Identifier
     */
    UUID databaseId;

    /**
     * Database Region
     */
    String databaseRegion;

    /**
     * Default constructor.
     */
    public AstraApiEndpoint() {
        // left blank intentionally
    }

    /**
     * Parse an endpoint URL to know information on the DB.
     *
     * @param endpointUrl
     *      endpoint URL copy from UI
     * @return
     *      astra db endpoint parsed
     */
    public static AstraApiEndpoint parse(String endpointUrl) {
        Assert.notNull(endpointUrl, "endpoint");
        AstraApiEndpoint endpoint = new AstraApiEndpoint();
        String tmpUrl;
        if (endpointUrl.contains(AstraEnvironment.PROD.getAppsSuffix())) {
            endpoint.env = AstraEnvironment.PROD;
            tmpUrl= endpointUrl.replaceAll(AstraEnvironment.PROD.getAppsSuffix(), "");
        } else if (endpointUrl.contains(AstraEnvironment.TEST.getAppsSuffix())) {
            endpoint.env = AstraEnvironment.TEST;
            tmpUrl= endpointUrl.replaceAll(AstraEnvironment.TEST.getAppsSuffix(), "");
        } else if (endpointUrl.contains(AstraEnvironment.DEV.getAppsSuffix())) {
            endpoint.env = AstraEnvironment.DEV;
            tmpUrl= endpointUrl.replaceAll(AstraEnvironment.DEV.getAppsSuffix(), "");
        } else {
            throw new IllegalArgumentException("Unable to detect environment from endpoint");
        }
        tmpUrl = tmpUrl.replace("https://", "");
        endpoint.databaseId = UUID.fromString(tmpUrl.substring(0,36));
        endpoint.databaseRegion = tmpUrl.substring(37);
        return endpoint;
    }

    /**
     * Constructor with chunk of the URL.
     *
     * @param databaseId
     *      database identifier
     * @param databaseRegion
     *      database region
     * @param env
     *      environment
     */
    public AstraApiEndpoint(UUID databaseId, String databaseRegion, AstraEnvironment env) {
        this.databaseId     = databaseId;
        this.databaseRegion = databaseRegion;
        this.env = env;
    }

    /**
     * Return the endpoint URL based on the chunks.
     *
     * @return
     *      endpoint URL.
     */
    public String getApiEndPoint() {
        return ApiLocator.getApiJsonEndpoint(env, databaseId.toString(), databaseRegion);
    }

    /**
     * Return the endpoint URL based on the chunks.
     *
     * @return
     *      endpoint URL.
     */
    public String getOriginalEndPoint() {
        return getApiEndPoint().replace("/api/json", "");
    }

}

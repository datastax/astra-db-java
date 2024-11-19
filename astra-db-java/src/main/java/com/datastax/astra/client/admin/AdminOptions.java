package com.datastax.astra.client.admin;

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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.internal.utils.Assert;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
public class AdminOptions {

    private String adminToken;

    private DataAPIClientOptions dataAPIClientOptions;

    public AdminOptions() {
        this(new DataAPIClientOptions());
    }

    public AdminOptions(DataAPIClientOptions options) {
        Assert.notNull(options, "options");
        this.dataAPIClientOptions = options.clone();
    }

    /**
     * Gets token
     *
     * @return value of token
     */
    public String getAdminToken() {
        return adminToken;
    }

    /**
     * Gets dataAPIClientOptions
     *
     * @return value of dataAPIClientOptions
     */
    public DataAPIClientOptions getDataAPIClientOptions() {
        return dataAPIClientOptions;
    }
}

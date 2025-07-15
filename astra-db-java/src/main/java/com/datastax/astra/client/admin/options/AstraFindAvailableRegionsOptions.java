package com.datastax.astra.client.admin.options;

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

import lombok.Data;

/**
 * Options for finding available regions in Astra.
 * This class allows users to specify whether to filter regions
 * based on organization enabled status.
 */
@Data
public class AstraFindAvailableRegionsOptions {

    /**
     * Flag to indicate whether to return only regions enabled for the organization.
     * Default is true, meaning only organization enabled regions will be returned.
     */
    private boolean onlyOrgEnabledRegions = true;

    /**
     * Constructs an instance of AstraFindAvailableRegionsOptions with default settings.
     *
     * @param onlyOrgEnabledRegions
     *        If true, only regions enabled for the organization will be returned.
     */
    public AstraFindAvailableRegionsOptions onlyOrgEnabledRegions(boolean onlyOrgEnabledRegions) {
        this.onlyOrgEnabledRegions = onlyOrgEnabledRegions;
        return this;
    }

}

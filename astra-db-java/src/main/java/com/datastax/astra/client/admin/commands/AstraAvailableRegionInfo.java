package com.datastax.astra.client.admin.commands;

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

import com.dtsx.astra.sdk.db.domain.DatabaseRegionServerless;
import lombok.Data;

/**
 * Information about an available region for Astra DB.
 * <p>
 * This class encapsulates details about a specific region where Astra DB can be deployed,
 * including the region name, cloud provider, display name, zone, classification, and whether
 * the region is enabled or reserved for qualified users.
 * </p>
 */
@Data
public class AstraAvailableRegionInfo {

    /** Region Name. */
    private String name;

    /** Cloud provider. */
    private String cloudProvider;

    /** Name of the region picked. */
    private String displayName;

    /** Zone. */
    private String zone;

    /** Classification. */
    private String classification;

    /** working region. */
    private boolean enabled;

    /** limited ? */
    private boolean reservedForQualifiedUsers;

    /**
     * Default Constructor.
     */
    public AstraAvailableRegionInfo() {
    }

    /**
     * Constructor from DatabaseRegionServerless.
     *
     * @param dbRegionServerless
     *      the database region serverless object
     */
    public AstraAvailableRegionInfo(DatabaseRegionServerless dbRegionServerless) {
        this.classification = dbRegionServerless.getClassification();
        this.cloudProvider = dbRegionServerless.getCloudProvider();
        this.displayName = dbRegionServerless.getDisplayName();
        this.enabled = dbRegionServerless.isEnabled();
        this.name = dbRegionServerless.getName();
        this.reservedForQualifiedUsers = dbRegionServerless.isReservedForQualifiedUsers();
        this.zone = dbRegionServerless.getZone();
    }
}

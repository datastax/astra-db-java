package com.datastax.astra.client.model;

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

import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bean representing the database information.
 */
@Getter @Setter
public class DatabaseInfo {

    /** Identifier for the database */
    private UUID id;

    /** common name of the database. */
    private String name;

    /** cloud provider. */
    private CloudProviderType cloud;

    /** Main Region for the database. */
    private String region;

    /** Default namespace for the database. */
    @Deprecated
    private String namespace;

    /** Default keyspace for the database. */
    @Deprecated
    private String keyspace;

    /** List of Namespace for the db. */
    @Deprecated
    private String namespaceList;

    private String keyspaceList;

    /** List of regions where the database is deployed. */
    private Set<String> regionList;

    /** Creation time. */
    private String creationTime;

    /** Devops Api response. */
    private Database rawDevopsResponse;

    /**
     * Initializing a database information representation by leveraging devops internal API
     * and omitting technical internal information.
     *
     * @param db
     *      devops db
     */
    public DatabaseInfo(Database db) {
        Assert.notNull(db, "Database");
        this.rawDevopsResponse = db;
        this.id           = UUID.fromString(db.getId());
        this.name         = db.getInfo().getName();
        this.keyspace    = db.getInfo().getKeyspace();
        this.region       = db.getInfo().getRegion();
        this.cloud        = db.getInfo().getCloudProvider();
        this.creationTime = db.getCreationTime();
        this.regionList   = db.getInfo().getDatacenters().stream()
                .map(Datacenter::getRegion).collect(Collectors.toSet());
        this.keyspaceList = String.join(",", db.getInfo().getKeyspaces());

        this.namespace     = keyspace;
        this.namespaceList = keyspaceList;
    }

}

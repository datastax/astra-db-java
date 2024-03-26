package com.datastax.astra.client.model;

import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bean representing the database information.
 */
@Data
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
    private String namespace;

    /** List of Namespace for the db. */
    private String namespaceList;

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
        this.namespace    = db.getInfo().getKeyspace();
        this.region       = db.getInfo().getRegion();
        this.cloud        = db.getInfo().getCloudProvider();
        this.creationTime = db.getCreationTime();
        this.regionList   = db.getInfo().getDatacenters().stream()
                .map(Datacenter::getRegion).collect(Collectors.toSet());
        this.namespaceList = String.join(",", db.getInfo().getKeyspaces());
    }

}

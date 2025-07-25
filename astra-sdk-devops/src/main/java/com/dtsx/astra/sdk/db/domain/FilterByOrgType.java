package com.dtsx.astra.sdk.db.domain;

/**
 * Encoded all values for 'filter-by-org' parameter when listing serverless databases.
 */
public enum FilterByOrgType {

    /**
     * Filter by organization ID.
     */
    DISABLED,

    /**
     * Filter by organization type.
     */
    ENABLED
}

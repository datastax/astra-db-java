package com.dtsx.astra.sdk.org.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Create a token with a description and org.
 */
@Data
public class CreateTokenRequest implements Serializable {

    /** Description. */
    String description;

    /** Organization Id. */
    UUID orgId;

    /** Expiration Date. */
    Instant expirationDate;

    /** List of Roles to include. */
    Set<String> roles;

    /**
     * Default Constructor.
     */
    public CreateTokenRequest() {
        roles = new HashSet<>();
    }

}

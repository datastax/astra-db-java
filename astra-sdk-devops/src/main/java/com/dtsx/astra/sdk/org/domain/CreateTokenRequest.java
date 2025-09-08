package com.dtsx.astra.sdk.org.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreateTokenRequest implements Serializable {

    private String description;
    private UUID orgId;
    private Instant expirationDate;
    private  Set<String> roles;

    public CreateTokenRequest() {
        roles = new HashSet<>();
    }

    /**
     * Gets description
     *
     * @return value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set value for description
     *
     * @param description new value for description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets orgId
     *
     * @return value of orgId
     */
    public UUID getOrgId() {
        return orgId;
    }

    /**
     * Set value for orgId
     *
     * @param orgId new value for orgId
     */
    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    /**
     * Gets expirationDate
     *
     * @return value of expirationDate
     */
    public Instant getExpirationDate() {
        return expirationDate;
    }

    /**
     * Set value for expirationDate
     *
     * @param expirationDate new value for expirationDate
     */
    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets roles
     *
     * @return value of roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Set value for roles
     *
     * @param roles new value for roles
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

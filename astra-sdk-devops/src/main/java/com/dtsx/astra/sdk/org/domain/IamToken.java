package com.dtsx.astra.sdk.org.domain;

import java.util.List;

/**
 * Token used to work with Astra.
 */
public class IamToken {
    /** client id. */
    private String clientId;
    /** roles list. */
    private List<String> roles;
    /** generated date. */
    private String generatedOn;
    /** description (maybe empty). */
    private String description;
    /** expiry date, if applicable. */
    private String tokenExpiry;

    /**
     * Default constructor.
     */
    public IamToken() {}

    /**
     * Getter accessor for attribute 'clientId'.
     *
     * @return
     *       current value of 'clientId'
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter accessor for attribute 'clientId'.
     * @param clientId
     * 		new value for 'clientId '
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter accessor for attribute 'roles'.
     *
     * @return
     *       current value of 'roles'
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Setter accessor for attribute 'roles'.
     * @param roles
     * 		new value for 'roles '
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Getter accessor for attribute 'generatedOn'.
     *
     * @return
     *       current value of 'generatedOn'
     */
    public String getGeneratedOn() {
        return generatedOn;
    }

    /**
     * Setter accessor for attribute 'generatedOn'.
     * @param generatedOn
     * 		new value for 'generatedOn '
     */
    public void setGeneratedOn(String generatedOn) {
        this.generatedOn = generatedOn;
    }

    /**
     * Getter accessor for attribute 'description'.
     *
     * @return
     *       current value of 'description'
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter accessor for attribute 'description'.
     * @param description
     * 		new value for 'description '
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter accessor for attribute 'tokenExpiry'.
     *
     * @return
     *       current value of 'tokenExpiry'
     */
    public String getTokenExpiry() {
        return tokenExpiry;
    }

    /**
     * Setter accessor for attribute 'tokenExpiry'.
     * @param tokenExpiry
     * 		new value for 'tokenExpiry '
     */
    public void setTokenExpiry(String tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
}

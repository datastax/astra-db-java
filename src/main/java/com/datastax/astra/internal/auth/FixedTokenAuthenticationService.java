package com.datastax.astra.internal.auth;

/**
 * Static token, never expires..
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FixedTokenAuthenticationService implements TokenProvider {

    /** Reference to token. */
    private String token;
    
    /**
     * Constructor with all parameters.
     *
     * @param token
     *      static token to be used
     */
    public FixedTokenAuthenticationService(String token) {
        this.token = token;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getToken() {
        return token;
    }

}

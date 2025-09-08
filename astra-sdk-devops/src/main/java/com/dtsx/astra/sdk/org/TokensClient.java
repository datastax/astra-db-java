package com.dtsx.astra.sdk.org;

import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.org.domain.CreateTokenRequest;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.DefaultRoles;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.ResponseAllIamTokens;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.ApiResponseHttp;
import com.dtsx.astra.sdk.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Group token operations.
 */
public class TokensClient extends AbstractApiClient {

    /** useful with tokens interactions. */
    private final RolesClient rolesClient;

    /**
     * As immutable object use builder to initiate the object.
     *
     * @param token
     *      authenticated token
     */
    public TokensClient(String token) {
        this(token, AstraEnvironment.PROD);
    }

    /**
     * As immutable object use builder to initiate the object.
     *
     * @param env
     *      define target environment to be used
     * @param token
     *      authenticated token
     */
    public TokensClient(String token, AstraEnvironment env) {
        super(token, env);
        rolesClient = new RolesClient(token, env);
    }

    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "tokens";
    }

    /**
     * List tokens
     *
     * @return
     *      list of tokens for this organization
     */
    public Stream<IamToken> findAll() {
        // Invoke endpoint
        ApiResponseHttp res = GET(getEndpointTokens(), getOperationName("find"));
        // Marshall
        return JsonUtils.unmarshallBean(res.getBody(), ResponseAllIamTokens.class).getClients().stream();
    }

    /**
     * Retrieve role information from its id.
     *
     * @param tokenId
     *      token identifier
     * @return
     *      role information
     */
    public Optional<IamToken> findById(String tokenId) {
        return findAll()
                .filter(t -> t.getClientId().equalsIgnoreCase(tokenId))
                .findFirst();
    }

    /**
     * Check in existence of a token.
     *
     * @param tokenId
     *      token identifier
     * @return
     *      if the provided token exist
     */
    public boolean exist(String tokenId) {
        return findById(tokenId).isPresent();
    }

    /**
     * Revoke a token.
     *
     * @param tokenId
     *      token identifier
     */
    public void delete(String tokenId) {
        if (!exist(tokenId)) {
            throw new RuntimeException("Token '"+ tokenId + "' has not been found");
        }
        DELETE(getEndpointToken(tokenId), getOperationName("delete"));
    }

    /**
     * Create token
     *
     * @param role
     *      create a token with dedicated role
     * @return
     *      created token
     */
    public CreateTokenResponse create(String role) {
        Assert.hasLength(role, "role");
        // Role should exist
        Optional<Role> optRole = rolesClient.findByName(role);
        String roleId;
        if (optRole.isPresent()) {
            roleId = optRole.get().getId();
        } else {
            roleId = rolesClient.get(role).getId();
        }
        // Building request
        String body = "{ \"roles\": [ \"" + JsonUtils.escapeJson(roleId) + "\"]}";
        // Invoke endpoint
        ApiResponseHttp res = POST(getEndpointTokens(), body, getOperationName("create"));
        // Marshall response
        return JsonUtils.unmarshallBean(res.getBody(), CreateTokenResponse.class);
    }

    /**
     * Create token
     *
     * @param ctr
     *      request to create a token with all the elements
     * @return
     *      created token
     */
    public CreateTokenResponse create(CreateTokenRequest ctr) {

        Assert.notNull(ctr, "request");
        Map<String, Object> tokenCreationPayload = new HashMap<>();

        // Role should exist

        Map<String, String> availableRoles =
         rolesClient.findAll().collect(Collectors.toMap(Role::getId, Role::getName));

        List<String> roleIds = new ArrayList<>();
        ctr.getRoles().forEach(role -> {
            if (availableRoles.containsKey(role)) {
                roleIds.add(role);
            } else if (availableRoles.containsValue(role)) {
                roleIds.add(availableRoles.entrySet().stream()
                        .filter(e -> role.equalsIgnoreCase(e.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null));
            } else {
                throw new IllegalArgumentException("Role '" + role + "' does not exist");
            }
        });
        tokenCreationPayload.put("roles", roleIds);

        if (ctr.getDescription() != null && !ctr.getDescription().isBlank()) {
            tokenCreationPayload.put("description", ctr.getDescription());
        }
        if (ctr.getOrgId() != null) {
            tokenCreationPayload.put("orgId", ctr.getOrgId());
        }
        if (ctr.getExpirationDate() != null) {
            tokenCreationPayload.put("tokenExpiry", ctr.getExpirationDate().toString());
        }

        // Invoke endpoint
        ApiResponseHttp res = POST(
                getEndpointTokens(),
                JsonUtils.marshall(tokenCreationPayload),
                getOperationName("create"));
        // Marshall response
        return JsonUtils.unmarshallBean(res.getBody(), CreateTokenResponse.class);
    }

    /**
     * Create token
     *
     * @param role
     *      create a token with dedicated role
     * @return
     *      created token
     */
    public CreateTokenResponse create(DefaultRoles role) {
        return create(role.getName());
    }

    /**
     * Endpoint to access schema for namespace.
     *
     * @return
     *      endpoint
     */
    public String getEndpointTokens() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/clientIdSecrets";
    }

    /**
     * Endpoint to access dbs (static)
     *
     * @param tokenId
     *      token identifier
     * @return
     *      token endpoint
     */
    public String getEndpointToken(String tokenId) {
        return getEndpointTokens() + "/" + tokenId;
    }
}

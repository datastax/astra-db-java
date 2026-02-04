package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupsNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Client for managing PCU (Processing Capacity Units) Groups in Astra.
 * Provides operations for creating, finding, and managing PCU groups.
 */
@Slf4j
public class PcuGroupsClient extends AbstractApiClient {
    private static final TypeReference<List<PcuGroup>> RESPONSE_PCU_GROUPS =
        new TypeReference<>(){};

    /**
     * Constructor with token for production environment.
     *
     * @param token
     *      authentication token
     */
    public PcuGroupsClient(String token) {
        super(token, AstraEnvironment.PROD);
    }

    /**
     * Constructor with token and environment.
     *
     * @param token
     *      authentication token
     * @param env
     *      target Astra environment
     */
    public PcuGroupsClient(String token, AstraEnvironment env) {
        super(token, env);
    }

    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "pcu.groups";
    }

    // ---------------------------------
    // ----        CRUD             ----
    // ---------------------------------

    /**
     * Creates a new PCU group.
     *
     * @param req
     *      PCU group creation request with configuration
     * @return
     *      created PCU group
     * @throws IllegalStateException
     *      if creation fails
     */
    public PcuGroup create(PcuGroupCreationRequest req) {
        val res = POST(getEndpointPcus(), JsonUtils.marshall(List.of(req.withDefaultsAndValidations())), getOperationName("create"));

        if (HttpURLConnection.HTTP_CREATED != res.getCode()) {
            throw new IllegalStateException("Expected code 201 to create pcu group but got " + res.getCode() + "body=" + res.getBody());
        }

        return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).get(0);
    }

    /**
     * Finds a PCU group by its unique identifier.
     *
     * @param id
     *      PCU group UUID
     * @return
     *      optional containing the PCU group if found
     */
    public Optional<PcuGroup> findById(String id) {
        try {
            return findAllImpl(List.of(id), "id", (_e) -> PcuGroupNotFoundException.forId(id)).findFirst();
        } catch (PcuGroupNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds all PCU groups with the specified title.
     *
     * @param title
     *      PCU group title to search for
     * @return
     *      stream of matching PCU groups
     */
    public Stream<PcuGroup> findByTitle(String title) {
        return findAll().filter(pg -> title.equals(pg.getTitle())); // order is important here since pg.title is nullable
    }

    /**
     * Finds the first PCU group with the specified title.
     *
     * @param title
     *      PCU group title to search for
     * @return
     *      optional containing the first matching PCU group
     */
    public Optional<PcuGroup> findFirstByTitle(String title) {
        return findByTitle(title).findFirst();
    }

    /**
     * Finds all PCU groups.
     *
     * @return
     *      stream of all PCU groups
     */
    public Stream<PcuGroup> findAll() {
        return findAll(null);
    }

    /**
     * Finds PCU groups by their identifiers.
     *
     * @param ids
     *      list of PCU group UUIDs to retrieve
     * @return
     *      stream of matching PCU groups
     * @throws PcuGroupsNotFoundException
     *      if any of the specified groups are not found
     */
    public Stream<PcuGroup> findAll(List<String> ids) {
        return findAllImpl(ids, "ids[%d]", (e) -> new PcuGroupsNotFoundException(e.getErrors().get(0).getMessage()));
    }

    protected interface FindAll404Handler {
        RuntimeException getError(ApiResponseError res);
    }

    private record FindAllReqBody(List<String> pcuGroupUUIDs) {}

    protected Stream<PcuGroup> findAllImpl(List<String> ids, String validationErrorFmtStr, FindAll404Handler on404) {
        if (ids != null) {
            if (ids.isEmpty()) {
                return Stream.of(); // TODO throw error or just return empty list or return all pcu groups? (devops api does the third)
            }

            for (var i = 0; i < ids.size(); i++) {
                Assert.isUUID(ids.get(i), validationErrorFmtStr.formatted(i));
            }
        }

        val reqBody = JsonUtils.marshall(new FindAllReqBody(ids));
        val res = POST(getEndpointPcus() + "/actions/get", reqBody, getOperationName("find"));

        try {
            return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).stream();
        } catch(Exception e) {
            ApiResponseError responseError = null;

            try {
                responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
            } catch (Exception ignored) {}


            if (responseError != null && res.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw on404.getError(responseError);
            }

            if (responseError != null && responseError.getErrors() != null && !responseError.getErrors().isEmpty()) {
                if (responseError.getErrors().get(0).getId() == 340018) { // TODO is this the right error code? also why does find all get special treatment for auth errors?
                    throw new IllegalArgumentException("You have provided an invalid token, please check", e);
                }
            }

            throw e;
        }
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    /**
     * Creates an operations client for a specific PCU group.
     *
     * @param pcuGroupId
     *      PCU group UUID
     * @return
     *      operations client for the specified PCU group
     */
    public PcuGroupOpsClient group(String pcuGroupId) {
        return new PcuGroupOpsClient(getToken(), getEnvironment(), pcuGroupId);
    }

    /**
     * Gets the PCU groups API endpoint.
     *
     * @return
     *      PCU groups endpoint URL
     */
    public String getEndpointPcus() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus";
    }
}

package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import com.dtsx.astra.sdk.pcu.domain.PCUTypeLocationFilter;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupsNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.*;
import com.dtsx.astra.sdk.utils.observability.ApiRequestObserver;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Client for managing PCU (Processing Capacity Units) Groups in Astra.
 * Provides operations for creating, finding, and managing PCU groups.
 */
@Slf4j
public class PCUGroupsOpsClient extends AbstractApiClient {

    private static final TypeReference<List<PCUGroup>> RESPONSE_PCU_GROUPS =
        new TypeReference<>(){};

    /**
     * Constructor with token for production environment.
     *
     * @param token       authentication token
     */
    public PCUGroupsOpsClient(String token) {
        this(token, AstraEnvironment.PROD);
    }

    /**
     * Constructor with token for different environment
     *
     * @param token       authentication token
     * @param environment astra environment
     */
    public PCUGroupsOpsClient(String token, AstraEnvironment environment) {
        super(token, environment);
    }

    /**
     * As immutable object use builder to initiate the object.
     *
     * @param env
     *      define target environment to be used
     * @param token
     *      authenticated token
     * @param observers
     *     list of observers
     */
    public PCUGroupsOpsClient(String token, AstraEnvironment env, Map<String, ApiRequestObserver> observers) {
        super(token, env, observers);
        HttpClientWrapper.registerObservers(observers);
    }

    // ---------------------------------
    // ----        TYPES             ----
    // ---------------------------------

    private static final TypeReference<List<PCUType>> RESPONSE_PCU_TYPES = new TypeReference<>(){};

    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "pcu.groups";
    }

    // ---------------------------------
    // ----     PCU TYPES           ----
    // ---------------------------------
    public Stream<PCUType> listPcuTypes() {
        return listPcuTypes(null);
    }
    public Stream<PCUType> listPcuTypes(PCUTypeLocationFilter request) {
        String contextPath = "/types";
        boolean first = true;
        if (request != null) {
            if (Utils.hasLength(request.getProvider())) {
                first = false;
                contextPath = contextPath + "?provider=" + request.getProvider();
            }
            if (Utils.hasLength(request.getRegion())) {
                if (!first) {
                    contextPath = contextPath + "&region=" + request.getRegion();
                } else  {
                    contextPath = contextPath + "?region=" + request.getRegion();
                }
            }
        }

        val res = GET(getEndpointPcus() + contextPath, getOperationName("find"));
        try {
            return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_TYPES).stream();
        } catch (Exception e) {
            ApiResponseError responseError = null;
            try {
                responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
                System.out.println(responseError.toString());
            } catch (Exception ignored) {}
            throw e;
        }
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
    public PCUGroup create(PCUGroupCreationRequest req) {
        String payload = JsonUtils.marshall(List.of(req.withDefaultsAndValidations()));
        System.out.println(payload);
        val res = POST(getEndpointPcus(), payload, getOperationName("create"));

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
    public Optional<PCUGroup> findById(UUID id) {
        try {
            return findAllImpl(Collections.singletonList(id), "id",
                    (_e) -> PcuGroupNotFoundException.forId(id)).findFirst();
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
    public Stream<PCUGroup> findByTitle(String title) {
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
    public Optional<PCUGroup> findFirstByTitle(String title) {
        return findByTitle(title).findFirst();
    }

    /**
     * Finds all PCU groups.
     *
     * @return
     *      stream of all PCU groups
     */
    public Stream<PCUGroup> findAll() {
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
    public Stream<PCUGroup> findAll(List<UUID> ids) {
        return findAllImpl(ids, "ids[%d]", (e) -> new PcuGroupsNotFoundException(e.getErrors().get(0).getMessage()));
    }

    protected interface FindAll404Handler {
        RuntimeException getError(ApiResponseError res);
    }

    private record FindAllReqBody(List<UUID> pcuGroupUUIDs) {}

    protected Stream<PCUGroup> findAllImpl(List<UUID> ids, String validationErrorFmtStr, FindAll404Handler on404) {
        if (ids != null) {
            if (ids.isEmpty()) {
                return Stream.of(); // TODO throw error or just return empty list or return all pcu groups? (devops api does the third)
            }
        }

        val reqBody = JsonUtils.marshall(new FindAllReqBody(ids));
        val res = POST(getEndpointPcus() + "/actions/get", reqBody, getOperationName("find"));
        System.out.println(res.getBody());

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
    public PCUGroupOpsClient group(UUID pcuGroupId) {
        return new PCUGroupOpsClient(getToken(), getEnvironment(), pcuGroupId);
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

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
    /**
     * Lists available PCU types with optional filtering by provider and region.
     *
     * @param request
     *      optional filter for provider and region
     * @return
     *      stream of available PCU types
     */
    public Stream<PCUType> listPcuTypes(PCUTypeLocationFilter request) {
        StringBuilder contextPath = new StringBuilder("/types");
        
        if (request != null) {
            boolean hasParams = false;
            
            if (Utils.hasLength(request.getProvider())) {
                contextPath.append("?provider=").append(request.getProvider());
                hasParams = true;
            }
            
            if (Utils.hasLength(request.getRegion())) {
                contextPath.append(hasParams ? "&" : "?")
                          .append("region=").append(request.getRegion());
            }
        }

        log.debug("Listing PCU types with path: {}", contextPath);
        val res = GET(getEndpointPcus() + contextPath, getOperationName("find"));
        
        try {
            return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_TYPES).stream();
        } catch (Exception e) {
            try {
                ApiResponseError responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
                log.error("Error listing PCU types: {}", responseError);
            } catch (Exception ignored) {
                log.error("Error listing PCU types, unable to parse error response", e);
            }
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
        log.debug("Creating PCU group with payload: {}", payload);
        val res = POST(getEndpointPcus(), payload, getOperationName("create"));

        if (HttpURLConnection.HTTP_CREATED != res.getCode()) {
            log.error("Failed to create PCU group. Expected 201 but got {}. Response body: {}", res.getCode(), res.getBody());
            throw new IllegalStateException("Expected code 201 to create pcu group but got " + res.getCode() + " body=" + res.getBody());
        }

        PCUGroup createdGroup = JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).get(0);
        log.info("Successfully created PCU group with ID: {}", createdGroup.getId());
        return createdGroup;
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
            return findAllImpl(Collections.singletonList(id),
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
     * Finds all PCU groups associated with a specific datacenter.
     * 
     * @param datacenterUUID
     *      the UUID of the datacenter to search for
     * @param on404
     *      error handler for 404 responses
     * @return
     *      stream of PCU groups associated with the datacenter
     * @throws IllegalArgumentException
     *      if datacenterUUID is null
     */
    public Stream<PCUGroup> findByDataCenterUuid(UUID datacenterUUID, FindAll404Handler on404) {
        if (datacenterUUID == null) {
            throw new IllegalArgumentException("datacenterUUID cannot be null");
        }
        log.debug("Finding PCU groups for datacenter: {}", datacenterUUID);
        ApiResponseHttp res = GET(getEndpointPcus() + "/actions/get/" + datacenterUUID, getOperationName("find"));

        try {
            return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).stream();
        } catch(Exception e) {
            manageException(res, on404, e);
            throw e;
        }
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
        return findAllImpl(ids, (e) -> new PcuGroupsNotFoundException(e.getErrors().get(0).getMessage()));
    }

    protected interface FindAll404Handler {
        RuntimeException getError(ApiResponseError res);
    }

    private record FindAllReqBody(List<UUID> pcuGroupUUIDs) {}

    /**
     * Internal implementation for finding PCU groups by IDs.
     * 
     * @param ids
     *      list of PCU group UUIDs to retrieve, null to retrieve all groups
     * @param on404
     *      error handler for 404 responses
     * @return
     *      stream of matching PCU groups
     */
    protected Stream<PCUGroup> findAllImpl(List<UUID> ids, FindAll404Handler on404) {
        // When ids is explicitly provided as empty list, return empty stream
        // When ids is null, the API will return all PCU groups
        if (ids != null && ids.isEmpty()) {
            log.debug("Empty ID list provided, returning empty stream");
            return Stream.empty();
        }

        val reqBody = JsonUtils.marshall(new FindAllReqBody(ids));
        log.debug("Finding PCU groups with request body: {}", reqBody);
        val res = POST(getEndpointPcus() + "/actions/get", reqBody, getOperationName("find"));

        try {
            List<PCUGroup> groups = JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS);
            log.debug("Found {} PCU group(s)", groups.size());
            return groups.stream();
        } catch(Exception e) {
            log.error("Error finding PCU groups", e);
            manageException(res, on404, e);
            throw e;
        }
    }


    protected void manageException(ApiResponseHttp res, FindAll404Handler on404, Exception ex) {
        ApiResponseError responseError = null;

        try {
            responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
        } catch (Exception ignored) {}


        if (responseError != null && res.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw on404.getError(responseError);
        }

        if (responseError != null && responseError.getErrors() != null && !responseError.getErrors().isEmpty()) {
            if (responseError.getErrors().get(0).getId() == 340018) {
                throw new IllegalArgumentException("You have provided an invalid token, please check", ex);
            }
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

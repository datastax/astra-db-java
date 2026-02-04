package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupStatusType;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupUpdateRequest;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

/**
 * Operations client for managing a specific PCU (Processing Capacity Units) Group.
 * Provides CRUD operations, maintenance actions, and datacenter association management.
 */
@Slf4j
public class PcuGroupOpsClient extends AbstractApiClient {
    /**
     * PCU group unique identifier.
     */
    @Getter
    private final String pcuGroupId;

    /**
     * Constructor with token and PCU group ID for production environment.
     *
     * @param token
     *      authentication token
     * @param pcuGroupId
     *      PCU group UUID
     */
    public PcuGroupOpsClient(String token, String pcuGroupId) {
        this(token, AstraEnvironment.PROD, pcuGroupId);
    }

    /**
     * Constructor with token, environment, and PCU group ID.
     *
     * @param token
     *      authentication token
     * @param env
     *      target Astra environment
     * @param pcuGroupId
     *      PCU group UUID
     */
    public PcuGroupOpsClient(String token, AstraEnvironment env, String pcuGroupId) {
        super(token, env);
        this.pcuGroupId = pcuGroupId;
    }

    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "pcu.group";
    }

    // ---------------------------------
    // ----       READ              ----
    // ---------------------------------

    /**
     * Finds the PCU group.
     *
     * @return
     *      optional containing the PCU group if found
     */
    public Optional<PcuGroup> find() {
        try {
            return Optional.of(get());
        } catch (PcuGroupNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the PCU group or throws an exception if not found.
     *
     * @return
     *      the PCU group
     * @throws PcuGroupNotFoundException
     *      if the PCU group does not exist
     */
    public PcuGroup get() {
        return new PcuGroupsClient(token, environment).findById(pcuGroupId).orElseThrow(() -> PcuGroupNotFoundException.forId(pcuGroupId));
    }

    /**
     * Checks if the PCU group exists.
     *
     * @return
     *      true if the PCU group exists
     */
    public boolean exist() {
        return find().isPresent();
    }

    /**
     * Checks if the PCU group is in ACTIVE status.
     *
     * @return
     *      true if the PCU group status is ACTIVE
     */
    public boolean isActive() {
        return PcuGroupStatusType.ACTIVE == get().getStatus();
    }

    /**
     * Checks if the PCU group is in CREATED or ACTIVE status.
     *
     * @return
     *      true if the PCU group status is CREATED or ACTIVE
     */
    public boolean isCreatedOrActive() {
        return PcuGroupStatusType.CREATED == get().getStatus() || isActive();
    }

    // ---------------------------------
    // ----       UPDATE            ----
    // ---------------------------------

    /**
     * Updates the PCU group configuration.
     *
     * @param req
     *      PCU group update request with new configuration
     */
    public void update(PcuGroupUpdateRequest req) {
        val base = get();
        PUT(getEndpointPcus(), JsonUtils.marshall(List.of(req.withDefaultsAndValidations(base))), getOperationName("update"));
    }

    // ---------------------------------
    // ----       MAINTENANCE       ----
    // ---------------------------------

    /**
     * Parks the PCU group, reducing resource consumption.
     *
     * @throws IllegalStateException
     *      if parking fails
     */
    public void park() {
        val res = POST(getEndpointPcus() + "/park/" + pcuGroupId, getOperationName("park"));

        if (res.getCode() >= 300) {
            throw new IllegalStateException("Expected code 200 to park pcu group but got " + res.getCode() + "body=" + res.getBody());
        }
    }

    /**
     * Unparks the PCU group, restoring full resource availability.
     *
     * @throws IllegalStateException
     *      if unparking fails
     */
    public void unpark() {
        val res = POST(getEndpointPcus() + "/unpark/" + pcuGroupId, getOperationName("unpark"));

        if (res.getCode() >= 300) {
            throw new IllegalStateException("Expected code 200 to unpark pcu group but got " + res.getCode() + "body=" + res.getBody());
        }
    }

    /**
     * Deletes the PCU group.
     *
     * @throws PcuGroupNotFoundException
     *      if the PCU group does not exist
     */
    public void delete() {
        if (!exist()) {
            throw PcuGroupNotFoundException.forId(pcuGroupId);
        }
        DELETE(getEndpointPcus() + "/" + pcuGroupId, getOperationName("delete"));
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    /**
     * Creates a client for managing datacenter associations for this PCU group.
     *
     * @return
     *      datacenter associations client
     */
    public PcuGroupDatacenterAssociationsClient datacenterAssociations() {
        return new PcuGroupDatacenterAssociationsClient(token, environment, pcuGroupId);
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

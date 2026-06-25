package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PcuGroupDatacenterAssociation;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupDbAssociationNotFound;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.stream.Stream;

/**
 * Client for managing datacenter associations for a PCU (Processing Capacity Units) Group.
 * Provides operations to associate, dissociate, transfer, and query datacenter associations.
 */
@Slf4j
public class PcuGroupDatacenterAssociationsClient extends AbstractApiClient {
    private static final TypeReference<List<PcuGroupDatacenterAssociation>> PCU_GROUP_DB_ASSOCIATIONS =
        new TypeReference<>() {};

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
    public PcuGroupDatacenterAssociationsClient(String token, String pcuGroupId) {
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
    public PcuGroupDatacenterAssociationsClient(String token, AstraEnvironment env, String pcuGroupId) {
        super(token, env);
        this.pcuGroupId = pcuGroupId;
    }

    /** {@inheritDoc} */
    @Override
    public String getServiceName() {
        return "pcu.group.associations.datacenter";
    }

    // ---------------------------------
    // ----        CRUD             ----
    // ---------------------------------

    /**
     * Checks if a datacenter is associated with this PCU group.
     *
     * @param datacenterId
     *      datacenter UUID
     * @return
     *      true if the datacenter is associated with this PCU group
     */
    public boolean exist(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        return findAll()
            .anyMatch((assoc) -> assoc.getDatacenterUUID().equals(datacenterId));
    }

    /**
     * Finds the datacenter association by datacenter ID.
     *
     * @param datacenterId
     *      datacenter UUID
     * @return
     *      the datacenter association
     * @throws PcuGroupDbAssociationNotFound
     *      if the datacenter is not associated with this PCU group
     */
    public PcuGroupDatacenterAssociation findByDatacenterId(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        return findAll()
            .filter((assoc) -> assoc.getDatacenterUUID().equals(datacenterId))
            .findFirst()
            .orElseThrow(() -> new PcuGroupDbAssociationNotFound(pcuGroupId, datacenterId));
    }

    /**
     * Finds all datacenter associations for this PCU group.
     *
     * @return
     *      stream of datacenter associations
     */
    public Stream<PcuGroupDatacenterAssociation> findAll() {
        val res = GET(getEndpointPcuAssociations() + "/" + pcuGroupId, getOperationName("findAll"));

        return unmarshallOrThrow(res, PCU_GROUP_DB_ASSOCIATIONS, "get pcu group db associations").stream();
    }

    /**
     * Associates a datacenter with this PCU group.
     *
     * @param datacenterId
     *      datacenter UUID to associate
     * @return
     *      the created datacenter association
     */
    public PcuGroupDatacenterAssociation associate(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        val res = POST(getEndpointPcuAssociations() + "/" + pcuGroupId + "/" + datacenterId, getOperationName("associate"));

        return unmarshallOrThrow(res, new TypeReference<List<PcuGroupDatacenterAssociation>>() {}, "associate db to pcu group").get(0);
    }

    private record TransferReqBody(String fromPCUGroupUUID, String toPCUGroupUUID, String datacenterUUID) {}

    /**
     * Transfers a datacenter association from this PCU group to another PCU group.
     *
     * @param toPcuGroup
     *      target PCU group UUID
     * @param datacenterId
     *      datacenter UUID to transfer
     * @return
     *      the updated datacenter association
     */
    public PcuGroupDatacenterAssociation transfer(@NonNull String toPcuGroup, @NonNull String datacenterId) {
        Assert.isUUID(toPcuGroup, "target pcu group id");
        Assert.isDatacenterID(datacenterId, "datacenter id");

        val reqBody = JsonUtils.marshall(new TransferReqBody(this.pcuGroupId, toPcuGroup, datacenterId));
        val res = POST(getEndpointPcuAssociations() + "/transfer/" + pcuGroupId, reqBody, getOperationName("transfer"));

        return unmarshallOrThrow(res, new TypeReference<List<PcuGroupDatacenterAssociation>>() {}, "transfer db to pcu group").get(0);
    }

    /**
     * Dissociates a datacenter from this PCU group.
     *
     * @param datacenterId
     *      datacenter UUID to dissociate
     */
    public void dissociate(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");
        DELETE(getEndpointPcuAssociations() + "/" + pcuGroupId + "/" + datacenterId, getOperationName("dissociate"));
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    /**
     * Gets the PCU associations API endpoint.
     *
     * @return
     *      PCU associations endpoint URL
     */
    public String getEndpointPcuAssociations() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus/association";
    }

    /**
     * Unmarshalls the API response or throws an appropriate exception.
     *
     * @param res
     *      API response
     * @param clazz
     *      target type reference for unmarshalling
     * @param operation
     *      operation name for error messages
     * @param <T>
     *      target type
     * @return
     *      unmarshalled object
     * @throws PcuGroupNotFoundException
     *      if the PCU group is not found
     * @throws IllegalStateException
     *      if unmarshalling fails
     */
    private <T> T unmarshallOrThrow(ApiResponseHttp res, TypeReference<T> clazz, String operation) {
        try {
            System.out.println(res.getBody());
            return JsonUtils.unmarshallType(res.getBody(), clazz);
        } catch (Exception e) {
            ApiResponseError responseError = null;

            try {
                responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
            } catch (Exception ignored) {}

            if (responseError != null && responseError.getErrors() != null && !responseError.getErrors().isEmpty()) {
                if (responseError.getErrors().get(0).getId() == 2000367) {
                    throw PcuGroupNotFoundException.forId(pcuGroupId);
                }
            }

            throw new IllegalStateException("Expected code 2xx to " + operation + " but got " + res.getCode() + "body=" + res.getBody());
        }
    }
}

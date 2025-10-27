package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupDbAssociation;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupDbAssociationNotFound;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class PcuGroupDatacenterAssociationsClient extends AbstractApiClient {
    private static final TypeReference<List<PcuGroupDbAssociation>> PCU_GROUP_DB_ASSOCIATIONS =
        new TypeReference<>() {};

    @Getter
    private final String pcuGroupId;

    public PcuGroupDatacenterAssociationsClient(String token, String pcuGroupId) {
        this(token, AstraEnvironment.PROD, pcuGroupId);
    }

    public PcuGroupDatacenterAssociationsClient(String token, AstraEnvironment env, String pcuGroupId) {
        super(token, env);
        this.pcuGroupId = pcuGroupId;
    }

    @Override
    public String getServiceName() {
        return "pcu.group.associations.datacenter";
    }

    // ---------------------------------
    // ----        CRUD             ----
    // ---------------------------------

    public boolean exist(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        return findAll()
            .anyMatch((assoc) -> assoc.getDatacenterUUID().equals(datacenterId));
    }

    public boolean exist(@NonNull Datacenter datacenter) {
        return exist(datacenter.getId());
    }

    public PcuGroupDbAssociation findByDatacenterId(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        return findAll()
            .filter((assoc) -> assoc.getDatacenterUUID().equals(datacenterId))
            .findFirst()
            .orElseThrow(() -> new PcuGroupDbAssociationNotFound(pcuGroupId, datacenterId));
    }

    public PcuGroupDbAssociation findByDatacenter(@NonNull Datacenter datacenter) {
        return findByDatacenterId(datacenter.getId());
    }

    public Stream<PcuGroupDbAssociation> findAll() {
        val res = GET(getEndpointPcuAssociations() + "/" + pcuGroupId, getOperationName("findAll"));

        return unmarshallOrThrow(res, PCU_GROUP_DB_ASSOCIATIONS, 200, "get pcu group db associations").stream();
    }

    public PcuGroupDbAssociation associate(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");

        val res = POST(getEndpointPcuAssociations() + "/" + pcuGroupId + "/" + datacenterId, getOperationName("associate"));

        return unmarshallOrThrow(res, new TypeReference<>() {}, 201, "associate db to pcu group");
    }

    public PcuGroupDbAssociation associate(@NonNull Datacenter datacenter) {
        return associate(datacenter.getId());
    }

    private record TransferReqBody(String fromPCUGroupUUID, String toPCUGroupUUID, String datacenterUUID) {}

    public PcuGroupDbAssociation transfer(@NonNull String toPcuGroup, @NonNull String datacenterId) {
        Assert.isUUID(toPcuGroup, "target pcu group id");
        Assert.isDatacenterID(datacenterId, "datacenter id");

        val reqBody = JsonUtils.marshall(new TransferReqBody(this.pcuGroupId, toPcuGroup, datacenterId));
        val res = POST(getEndpointPcuAssociations() + "/transfer/" + pcuGroupId, reqBody, getOperationName("transfer"));

        return unmarshallOrThrow(res, new TypeReference<>() {}, 200, "transfer db to pcu group");
    }

    public PcuGroupDbAssociation transfer(@NonNull PcuGroup toPcuGroup, @NonNull Datacenter datacenter) {
        return transfer(toPcuGroup.getUuid(), datacenter.getId());
    }

    public void dissociate(@NonNull String datacenterId) {
        Assert.isDatacenterID(datacenterId, "datacenter id");
        DELETE(getEndpointPcuAssociations() + "/" + pcuGroupId + "/" + datacenterId, getOperationName("dissociate"));
    }

    public void dissociate(@NonNull Datacenter datacenter) {
        dissociate(datacenter.getId());
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    public String getEndpointPcuAssociations() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus/association";
    }

    private <T> T unmarshallOrThrow(ApiResponseHttp res, TypeReference<T> clazz, int expectedCode, String operation) {
        try {
            return JsonUtils.unmarshallType(res.getBody(), clazz);
        } catch (Exception e) {
            ApiResponseError responseError = null;

            try {
                responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
            } catch (Exception ignored) {}

            if (responseError != null && responseError.getErrors() != null && !responseError.getErrors().isEmpty()) {
                if (responseError.getErrors().getFirst().getId() == 2000367) {
                    throw PcuGroupNotFoundException.forId(pcuGroupId);
                }
            }

            throw new IllegalStateException("Expected code " + expectedCode + " to " + operation + " but got " + res.getCode() + "body=" + res.getBody());
        }
    }
}

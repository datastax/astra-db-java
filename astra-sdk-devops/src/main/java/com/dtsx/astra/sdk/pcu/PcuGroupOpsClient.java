package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupStatusType;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Slf4j
public class PcuGroupOpsClient extends AbstractApiClient {
    @Getter
    private final String pcuGroupId;

    public PcuGroupOpsClient(String token, String pcuGroupId) {
        this(token, AstraEnvironment.PROD, pcuGroupId);
    }

    public PcuGroupOpsClient(String token, AstraEnvironment env, String pcuGroupId) {
        super(token, env);
        this.pcuGroupId = pcuGroupId;
    }

    @Override
    public String getServiceName() {
        return "pcu.group";
    }

    public Optional<PcuGroup> find() {
        try {
            return Optional.of(get());
        } catch (PcuGroupNotFoundException e) {
            return Optional.empty();
        }
    }

    public PcuGroup get() {
        return new PcuGroupsClient(token, environment).findById(pcuGroupId);
    }

    public boolean exist() {
        return find().isPresent();
    }

    public boolean isActive() {
        return PcuGroupStatusType.ACTIVE == get().getStatus();
    }

    public boolean isCreatedOrActive() {
        return PcuGroupStatusType.CREATED == get().getStatus() || isActive();
    }

    // ---------------------------------
    // ----       MAINTENANCE       ----
    // ---------------------------------

    public void park() {
        val res = POST(getEndpointPcus() + "/park/" + pcuGroupId, getOperationName("park"));
        assertHttpCodeAccepted(res, "park", pcuGroupId);
    }

    public void unpark() {
        val res = POST(getEndpointPcus() + "/unpark/" + pcuGroupId, getOperationName("unpark"));
        assertHttpCodeAccepted(res, "unpark", pcuGroupId);
    }

    public void delete() {
        if (!exist()) {
            throw PcuGroupNotFoundException.forId(pcuGroupId);
        }
        DELETE(getEndpointPcus() + "/" + pcuGroupId, getOperationName("delete"));
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    public PcuGroupDbAssociationsClient dbAssociations() {
        return new PcuGroupDbAssociationsClient(token, environment, pcuGroupId);
    }

    public String getEndpointPcus() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus";
    }
}

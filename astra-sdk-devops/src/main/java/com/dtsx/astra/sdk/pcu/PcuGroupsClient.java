package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.pcu.domain.PcuGroup;
import com.dtsx.astra.sdk.pcu.domain.PcuGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.pcu.exception.PcuGroupsNotFoundException;
import com.dtsx.astra.sdk.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class PcuGroupsClient extends AbstractApiClient {
    private static final TypeReference<List<PcuGroup>> RESPONSE_PCU_GROUPS =
        new TypeReference<>(){};

    public PcuGroupsClient(String token) {
        super(token, AstraEnvironment.PROD);
    }

    public PcuGroupsClient(String token, AstraEnvironment env) {
        super(token, env);
    }

    @Override
    public String getServiceName() {
        return "pcu.groups";
    }

    // ---------------------------------
    // ----        CRUD             ----
    // ---------------------------------

    public PcuGroup create(PcuGroupCreationRequest req) {
        val res = POST(getEndpointPcus(), JsonUtils.marshall(req), getOperationName("create"));

        if (HttpURLConnection.HTTP_CREATED != res.getCode()) {
            throw new IllegalStateException("Expected code 201 to create db but got " + res.getCode() + "body=" + res.getBody());
        }

        return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).getFirst();
    }

    public PcuGroup findById(String id) {
        return findAllImpl(List.of(id), "id", (_e) -> PcuGroupNotFoundException.forId(id)).findFirst().orElseThrow(); // it can never throw unless the API itself is broken
    }

    public PcuGroup findByTitle(String title) {
        return findAll()
                .filter(pg -> pg.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> PcuGroupNotFoundException.forTitle(title));
    }

    public Stream<PcuGroup> findAll() {
        return findAll(null);
    }

    public Stream<PcuGroup> findAll(List<String> ids) {
        return findAllImpl(ids, "ids[%d]", (e) -> new PcuGroupsNotFoundException(e.getErrors().getFirst().getMessage()));
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
                if (responseError.getErrors().getFirst().getId() == 340018) { // TODO is this the right error code? also why does find all get special treatment for auth errors?
                    throw new IllegalArgumentException("You have provided an invalid token, please check", e);
                }
            }

            throw e;
        }
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    public PcuGroupOpsClient group(String pcuGroupId) {
        return new PcuGroupOpsClient(getToken(), getEnvironment(), pcuGroupId);
    }

    public String getEndpointPcus() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus";
    }
}

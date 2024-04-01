package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIOptions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class DataApiOptionsTest {

    @Test
    void shouldPopulateOptions() {
        DataAPIOptions options = DataAPIOptions.builder()
                .withHttpProxy(new DataAPIOptions.HttpProxy("localhost", 8080))
                .withApiVersion("v1")
                .withHttpRedirect(HttpClient.Redirect.NORMAL)
                .withHttpRetryCount(5)
                .withHttpRetryDelayMillis(1000)
                .withDestination(DataAPIOptions.DataAPIDestination.DSE)
                .build();
        assertThat(options.getHttpClientOptions().getProxy().getHostname()).isEqualTo("localhost");
    }
}

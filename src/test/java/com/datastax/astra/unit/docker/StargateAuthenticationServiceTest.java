package com.datastax.astra.unit.docker;

import com.datastax.astra.client.internal.auth.StargateAuthenticationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StargateAuthenticationServiceTest {

    @Test
    public void shouldGetAToken() {
        System.out.println(new StargateAuthenticationService().getToken());
        assertThat(new StargateAuthenticationService().getToken()).isNotNull();
    }

}

package com.datastax.astra.test.unit.docker;

import com.datastax.astra.internal.auth.TokenProviderStargate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StargateAuthenticationServiceTest {

    @Test
    public void shouldGetAToken() {
        System.out.println(new TokenProviderStargate().getToken());
        assertThat(new TokenProviderStargate().getToken()).isNotNull();
    }

}

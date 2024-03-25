package com.datastax.astra.unit.docker;

import com.datastax.astra.internal.TokenProviderStargate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StargateAuthenticationServiceTest {

    @Test
    public void shouldGetAToken() {
        System.out.println(new TokenProviderStargate().getToken());
        assertThat(new TokenProviderStargate().getToken()).isNotNull();
    }

}

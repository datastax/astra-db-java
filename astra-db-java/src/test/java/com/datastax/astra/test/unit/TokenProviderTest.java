package com.datastax.astra.test.unit;

import com.datastax.astra.internal.auth.TokenProviderFixed;
import com.datastax.astra.internal.auth.TokenProviderStargate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for Token Providers
 */
public class TokenProviderTest {

    @Test
    void shouldFixedTokenProviderReturnToken() {
        assertThat(new TokenProviderFixed("token").getToken()).isEqualTo("token");
        assertThat(new TokenProviderFixed("token").get()).isEqualTo("token");
    }

    @Test
    void shouldGetMeAToken() {
        System.out.println(new TokenProviderStargate().getToken());
    }

}

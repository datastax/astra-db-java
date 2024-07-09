package com.datastax.astra.test.unit;

import com.datastax.astra.client.auth.StaticTokenProvider;
import com.datastax.astra.client.auth.UsernamePasswordTokenProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for Token Providers
 */
class TokenProviderTest {

    @Test
    void shouldFixedTokenProviderReturnToken() {
        assertThat(new StaticTokenProvider("token").getToken()).isEqualTo("token");
        assertThat(new StaticTokenProvider("token").get()).isEqualTo("token");
    }

    @Test
    void shouldGetMeAToken() {
        String token = new UsernamePasswordTokenProvider().getToken();
        System.out.println(token);
        assertThat(token).isNotNull();
    }

}

package com.datastax.astra.unit.docker;

import com.datastax.astra.internal.auth.StargateAuthenticationService;
import org.junit.jupiter.api.Test;

public class StargateAuthenticationServiceTest {

    @Test
    public void shouldGetAToken() {
        System.out.println(new StargateAuthenticationService().getToken());


    }

}

package com.datastax.astra.test.unit;

import com.datastax.astra.client.exception.DataAPIFaultyResponseException;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.internal.api.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Tests on exceptions.
 */
class DataAPIExceptionsTest {

    @Test
    void shouldSerializeCommandInFaultyException() {
        // Given
        Command c = new Command("createCollection").append("name", "demo");
        ApiResponse res = new ApiResponse();
        assertThatThrownBy(() -> { throw new DataAPIFaultyResponseException(c, res, "demo message");} )
                .isInstanceOf(DataAPIFaultyResponseException.class)
                .hasMessageContaining("demo message")
                .extracting("command", "response", "message")
                .containsExactly(c, res, "[CLIENT_ERROR] - demo message");
    }

}

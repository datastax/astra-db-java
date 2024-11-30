package com.datastax.astra.test.unit;

import com.datastax.astra.client.exceptions.UnexpectedDataAPIResponseException;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.internal.api.DataAPIResponse;
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
        DataAPIResponse res = new DataAPIResponse();
        assertThatThrownBy(() -> { throw new UnexpectedDataAPIResponseException(c, res, "demo message");} )
                .isInstanceOf(UnexpectedDataAPIResponseException.class)
                .hasMessageContaining("demo message")
                .extracting("command", "response", "message")
                .containsExactly(c, res, "[CLIENT_ERROR] - demo message");
    }

}

package com.datastax.astra.test.unit;

import com.datastax.astra.client.exception.DataAPIFaultyResponseException;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.auth.TokenProviderStargate;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.client.exception.DataApiException.DEFAULT_ERROR_CODE;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
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

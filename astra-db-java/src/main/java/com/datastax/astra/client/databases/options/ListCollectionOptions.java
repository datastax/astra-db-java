package com.datastax.astra.client.databases.options;

import com.datastax.astra.client.core.options.TimeoutOptions;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class ListCollectionOptions {

    long timeoutMillis = TimeoutOptions.DEFAULT_COLLECTION_ADMIN_TIMEOUT_MILLIS;

    /**
     * Gets timeoutMillis
     *
     * @return value of timeoutMillis
     */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}

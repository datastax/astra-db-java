package com.datastax.astra.client.admin.options;

import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
public class CreateDatabaseOptions extends BaseOptions<CreateDatabaseOptions> {

    boolean waitForDb = false;

    /**
     * Gets waitForDb
     *
     * @return value of waitForDb
     */
    public boolean isWaitForDb() {
        return waitForDb;
    }
}

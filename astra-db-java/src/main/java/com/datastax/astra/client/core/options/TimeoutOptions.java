package com.datastax.astra.client.core.options;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.internal.utils.Assert;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Duration;

/**
 * This class is used to define the timeout options for the client.
 */
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class TimeoutOptions implements Cloneable {

    public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS          =  10000L;
    public static final long DEFAULT_REQUEST_TIMEOUT_MILLIS          =  10000L;
    public static final long DEFAULT_GENERAL_METHOD_TIMEOUT_MILLIS   =  30000L;
    public static final long DEFAULT_COLLECTION_ADMIN_TIMEOUT_MILLIS =  60000L;
    public static final long DEFAULT_TABLE_ADMIN_TIMEOUT_MILLIS      =  30000L;
    public static final long DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS   = 600000L;
    public static final long DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS   =  30000L;

    /**
     * Lower level request timeout (http request)
     */
    long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;

    /**
     * Lower level request timeout (http request)
     */
    long requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;

    /**
     * Data operation timeout (find*, insert*, update*, delete*)
     */
    long generalMethodTimeoutMillis = DEFAULT_GENERAL_METHOD_TIMEOUT_MILLIS;

    /**
     * Database admin timeout (create, delete, list)
     */
    long databaseAdminTimeoutMillis = DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS;

    /**
     * Keyspace admin timeout (create, delete, list)
     */
    long keyspaceAdminTimeoutMillis = DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS;

    /**
     * Schema operation timeout (create*, alter*, drop*)
     */
    long collectionAdminTimeoutMillis = DEFAULT_COLLECTION_ADMIN_TIMEOUT_MILLIS;

    /**
     * Schema operation timeout (create*, alter*, drop*)
     */
    long tableAdminTimeoutMillis = DEFAULT_TABLE_ADMIN_TIMEOUT_MILLIS;

    @Override
    public TimeoutOptions clone() {
        try {
            return (TimeoutOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    public TimeoutOptions connectTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.connectTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions requestTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.requestTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions generalMethodTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.generalMethodTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions databaseAdminTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.databaseAdminTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions keyspaceAdminTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.keyspaceAdminTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions collectionAdminTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.collectionAdminTimeoutMillis = timeout.toMillis();
        return this;
    }

    public TimeoutOptions tableAdminTimeout(Duration timeout) {
        Assert.notNull(timeout, "Timeout");
        this.tableAdminTimeoutMillis = timeout.toMillis();
        return this;
    }

    /**
     * Gets connectTimeoutMillis
     *
     * @return value of connectTimeoutMillis
     */
    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /**
     * Gets requestTimeoutMillis
     *
     * @return value of requestTimeoutMillis
     */
    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    /**
     * Gets dataOperationTimeoutMillis
     *
     * @return value of dataOperationTimeoutMillis
     */
    public long getGeneralMethodTimeoutMillis() {
        return generalMethodTimeoutMillis;
    }

    /**
     * Gets schemaOperationTimeoutMillis
     *
     * @return value of schemaOperationTimeoutMillis
     */
    public long getTableAdminTimeoutMillis() {
        return tableAdminTimeoutMillis;
    }

    /**
     * Gets databaseAdminTimeoutMillis
     *
     * @return value of databaseAdminTimeoutMillis
     */
    public long getDatabaseAdminTimeoutMillis() {
        return databaseAdminTimeoutMillis;
    }

    /**
     * Gets keyspaceAdminTimeoutMillis
     *
     * @return value of keyspaceAdminTimeoutMillis
     */
    public long getKeyspaceAdminTimeoutMillis() {
        return keyspaceAdminTimeoutMillis;
    }

    /**
     * Gets collectionAdminTimeoutMillis
     *
     * @return value of collectionAdminTimeoutMillis
     */
    public long getCollectionAdminTimeoutMillis() {
        return collectionAdminTimeoutMillis;
    }
}

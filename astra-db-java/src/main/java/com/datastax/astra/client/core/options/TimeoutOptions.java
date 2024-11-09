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

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This class is used to define the timeout options for the client.
 */
@Getter @Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class TimeoutOptions implements Cloneable {

    public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS          =  10000L;
    public static final long DEFAULT_REQUEST_TIMEOUT_MILLIS          =  30000L;
    public static final long DEFAULT_DATA_OPERATION_TIMEOUT_MILLIS   =  30000L;
    public static final long DEFAULT_SCHEMA_OPERATION_TIMEOUT_MILLIS =  45000L;
    public static final long DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS   = 600000L;
    public static final long DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS   =  20000L;

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
    long dataOperationTimeoutMillis = DEFAULT_DATA_OPERATION_TIMEOUT_MILLIS;

    /**
     * Schema operation timeout (create*, alter*, drop*)
     */
    long schemaOperationTimeoutMillis = DEFAULT_SCHEMA_OPERATION_TIMEOUT_MILLIS;

    /**
     * Database admin timeout (create, delete, list)
     */
    long databaseAdminTimeoutMillis = DEFAULT_DATABASE_ADMIN_TIMEOUT_MILLIS;

    /**
     * Keyspace admin timeout (create, delete, list)
     */
    long keyspaceAdminTimeoutMillis = DEFAULT_KEYSPACE_ADMIN_TIMEOUT_MILLIS;

    @Override
    public TimeoutOptions clone() {
        try {
            return (TimeoutOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
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
    public long getDataOperationTimeoutMillis() {
        return dataOperationTimeoutMillis;
    }

    /**
     * Gets schemaOperationTimeoutMillis
     *
     * @return value of schemaOperationTimeoutMillis
     */
    public long getSchemaOperationTimeoutMillis() {
        return schemaOperationTimeoutMillis;
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
}

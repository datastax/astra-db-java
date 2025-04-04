package com.datastax.astra.internal.command;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.exceptions.DataAPIException;

public class CursorError extends DataAPIException {

    // The underlying cursor which caused this error.
    public final AbstractCursor<?, ?> cursor;

    // The state of the cursor when the error occurred.
    public final CursorState state;

    public CursorError(String message, AbstractCursor<?, ?> cursor) {
        super(DEFAULT_ERROR_CODE, message);
        this.cursor = cursor;
        this.state = cursor.getState();
    }
}

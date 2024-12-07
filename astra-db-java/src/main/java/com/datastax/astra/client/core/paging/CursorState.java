package com.datastax.astra.client.core.paging;

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
/**
 * Represents the possible states of a database cursor.
 * <p>
 * A cursor can be in one of the following states:
 * </p>
 * <ul>
 *   <li>{@link #IDLE} - The cursor is created but not yet started.</li>
 *   <li>{@link #STARTED} - The cursor has been started and is active.</li>
 *   <li>{@link #CLOSED} - The cursor has been closed and is no longer usable.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CursorState state = CursorState.IDLE;
 *
 * switch (state) {
 *     case IDLE:
 *         System.out.println("The cursor is idle.");
 *         break;
 *     case STARTED:
 *         System.out.println("The cursor is active.");
 *         break;
 *     case CLOSED:
 *         System.out.println("The cursor is closed.");
 *         break;
 * }
 * }</pre>
 */
public enum CursorState {

    /**
     * Indicates that the cursor is created but not yet started.
     */
    IDLE,

    /**
     * Indicates that the cursor has been started and is active.
     */
    STARTED,

    /**
     * Indicates that the cursor has been closed and is no longer usable.
     */
    CLOSED
}

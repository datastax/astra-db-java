package com.datastax.astra.internal.utils;

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
 * Utility class for internal usage
 */
public class Utils {

    /**
     * Hide constructor for utilities
     */
    private Utils() {
    }

    /**
     * Check if a string has length
     * @param str the string to check
     * @return true if not null and not empty
     */
    public static boolean hasLength(String str) {
        return (null != str && !"".equals(str));
    }
}

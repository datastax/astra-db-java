package com.datastax.astra.internal.utils;

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

import com.datastax.astra.client.exceptions.InvalidFieldExpressionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Escaper for special characters like dot and and.
 */
public class EscapeUtils {

    /**
     * Hide default.
     */
    private EscapeUtils() {}

    /**
     * Escape a field path.
     *
     * @param inputPath
     *      field path
     * @return
     *      escaped field path
     */
    public static String[] unEscapeFieldPath(String inputPath) {
        Assert.notNull(inputPath, "Path must not be null");
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escape = false;

        for (int i = 0; i < inputPath.length(); i++) {
            char c = inputPath.charAt(i);

            if (escape) {
                if (c == '.' || c == '&') {
                    current.append(c);
                    escape = false;
                } else {
                   InvalidFieldExpressionException.throwInvalidField(inputPath, "Invalid escape sequence at position " + i);
                }
            } else if (c == '&') {
                escape = true;
            } else if (c == '.') {
                if (i == inputPath.length() - 1) {
                    InvalidFieldExpressionException.throwInvalidField(inputPath,"Expression cannot end with an unescaped dot");
                }
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (escape) {
            InvalidFieldExpressionException.throwInvalidField(inputPath, "Dangling escape character at end of string");
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Escape a field path.
     *
     * @param segments
     *      field path segments
     * @return
     *      escaped field path
     */
    public static String escapeFieldNames(String... segments) {
        if (segments == null || segments.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            result.append(escapeSingleExpression(segments[i]));
            if (i < segments.length - 1) {
                result.append('.');
            }
        }
        return result.toString();
    }

    /**
     * Escape a single expression.
     *
     * @param segment
     *      expression
     * @return
     *      escaped expression
     */
    public static String escapeSingleExpression(String segment) {
        StringBuilder result = new StringBuilder();
        for (char c : segment.toCharArray()) {
            if (c == '&' || c == '.') {
                result.append('&');
            }
            result.append(c);
        }
        return result.toString();
    }

}

package com.datastax.astra.test.integration.model;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generator for test data with composite primary keys.
 */
public final class TableCompositeRowGenerator {

    private static final int NAME_LENGTH = 8;

    private TableCompositeRowGenerator() {
        // utility class
    }

    /**
     * Generate unique random rows for testing.
     *
     * @param count number of rows to generate
     * @return list of unique TableCompositeRow instances
     */
    public static List<TableCompositeRow> generateUniqueRandomRows(int count) {
        List<TableCompositeRow> rows = new ArrayList<>();
        Set<String> generatedNames = new HashSet<>();
        Random random = new Random();
        while (rows.size() < count) {
            int age = random.nextInt(90);
            String firstName = generateRandomAlphabeticString(NAME_LENGTH, random);
            String lastName = generateRandomAlphabeticString(NAME_LENGTH, random);
            String uniqueKey = firstName + "_" + lastName;
            if (!generatedNames.contains(uniqueKey)) {
                generatedNames.add(uniqueKey);
                rows.add(new TableCompositeRow(age, firstName, lastName));
            }
        }
        return rows;
    }

    private static String generateRandomAlphabeticString(int length, Random random) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) ('A' + random.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }
}

package com.datastax.astra.test.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TableCompositeRowGenerator {

    private static final int NAME_LENGTH = 8;

    public static List<TableCompositeRow> generateUniqueRandomRows(int x) {
        List<TableCompositeRow> rows = new ArrayList<>();
        Set<String> generatedNames = new HashSet<>();
        Random random = new Random();

        while (rows.size() < x) {
            int age = random.nextInt(100); // Age between 0 and 99
            String firstName = generateRandomAlphabeticString(NAME_LENGTH, random);
            //String lastName = generateRandomAlphabeticString(NAME_LENGTH, random);
            String lastName = "lunven";
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
        for(int i = 0; i < length; i++) {
            char c = (char) ('A' + random.nextInt(26)); // Generates a character from 'A' to 'Z'
            sb.append(c);
        }
        return sb.toString();
    }
}


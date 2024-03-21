package com.datastax.astra.documentation.db;

import com.datastax.astra.AstraDatabase;

public class DropNamespace {

    public static void main(String[] args) {
        // Default initialization
        AstraDatabase db = new AstraDatabase("API_ENDPOINT", "TOKEN");

        // Drop a Namespace
        db.dropNamespace("<namespace_name>");
    }
}

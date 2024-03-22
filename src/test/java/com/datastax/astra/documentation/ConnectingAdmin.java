package com.datastax.astra.documentation;


import com.datastax.astra.client.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;

public class ConnectingAdmin {
    public static void main(String[] args) {
        // Default Initialization
        DataAPIClient client = new DataAPIClient("TOKEN");

        // Accessing admin with current token
        AstraDBAdmin astradbAdmin = client.getAdmin();

        // Accessing admin providing a new token possibly with stronger permissions
        AstraDBAdmin astradbAdmin1 = client.getAdmin("SUPER_USER_TOKEN");
    }
}
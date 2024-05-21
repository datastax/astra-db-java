package com.datastax.astra.client.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class EmbeddingProvider {

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_NONE          = "NONE";

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_SHARED_SECRET = "SHARED_SECRET";

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_HEADER        = "HEADER";

   /** Display name of the provider */
   private String displayName;

   /** Display name of the provider */
   private String url;

   /** Authentication */
   private Map<String, AuthenticationMethod> supportedAuthentication;

   /** Parameters fo the Servuce */
   private List<Parameter> parameters;

   /** List of models support. */
   private List<Model> models;

    /**
     * Accessor for the Share Secret Authentication.
     *
     * @return
     *      shared Secret authentication
     */
   public Optional<AuthenticationMethod> getSharedSecretAuthentication() {
       return Optional.ofNullable(supportedAuthentication.get(AUTHENTICATION_METHOD_SHARED_SECRET));
   }

    /**
     * Accessor for the Share Secret Authentication.
     *
     * @return
     *      shared Secret authentication
     */
    public Optional<AuthenticationMethod> getHeaderAuthentication() {
        return Optional.ofNullable(supportedAuthentication.get(AUTHENTICATION_METHOD_HEADER));
    }

    /**
     * Model for the service.
     */
    @Data
    @NoArgsConstructor
    public static class Model {
        private String name;
        private Integer vectorDimension;
        private List<Parameter> parameters;
    }

    /**
     * Authentication method.
     */
   @Data
   @NoArgsConstructor
   public static class AuthenticationMethod {
    private boolean enabled;
    private List<Token> tokens;
   }

    /**
     * Token method.
     */
    @Data
    @NoArgsConstructor
    public static class Token {
        private String forwarded;
        private String accepted;
    }

    /**
     * Parameters for the service.
     */
    @Data
    @NoArgsConstructor
    public static class Parameter {
        private String name;
        private String type;
        private boolean required;
        private String defaultValue;
        private Validation validation;
        private String help;
    }

    /**
     * Validation Component for the parameter.
     */
    @Data
    @NoArgsConstructor
    public static class Validation {
        private List<Integer> numericRange;
    }



}

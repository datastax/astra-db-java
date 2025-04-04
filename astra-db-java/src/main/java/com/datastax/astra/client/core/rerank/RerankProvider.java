package com.datastax.astra.client.core.rerank;

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

import com.datastax.astra.client.collections.definition.documents.Document;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class representing the Embedding Provider returned by findEmbeddingProviders command.
 */
@Data
public class RerankProvider {

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_NONE          = "NONE";

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_SHARED_SECRET = "SHARED_SECRET";

   /** Keys for the supported authentication methods */
   public static final String AUTHENTICATION_METHOD_HEADER        = "HEADER";

   private Boolean isDefault;

   /** Display name of the provider */
   private String displayName;

   /** Authentication */
   private Map<String, AuthenticationMethod> supportedAuthentication;

   /** Parameters fo the Servuce */
   private List<Parameter> parameters;

   /** List of models support. */
   private List<Model> models;

    /**
     * Default constructor.
     */
    public RerankProvider() {
    }

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
     * Accessor for the Share Secret Authentication.
     *
     * @return
     *      shared Secret authentication
     */
    public Optional<AuthenticationMethod> getAuthenticationNone() {
        return Optional.ofNullable(supportedAuthentication.get(AUTHENTICATION_METHOD_NONE));
    }

    /**
     * Model for the service.
     */
    @Data
    public static class Model {

        /** Model name */
        private String name;

        private String url;

        private Boolean isDefault;

        /** Parameters for the model. */
        private List<Parameter> parameters;

        /**
         * Default constructor.
         */
        public Model() {}
    }

    /**
     * Authentication method.
     */
   @Data
   public static class AuthenticationMethod {

       /** If this method is enabled. */
       private boolean enabled;

       /** List of tokens. */
       private List<Token> tokens;

        /**
         * Default constructor.
         */
        public AuthenticationMethod() {}
   }

    /**
     * Token method.
     */
    @Data
    public static class Token {

        /** If token is forwarded. */
        private String forwarded;

        /** Accept token. */
        private String accepted;

        /**
         * Default constructor.
         */
        public Token() {}
    }

    /**
     * Parameters for the service.
     */
    @Data
    public static class Parameter {
        private String name;
        private String type;
        private boolean required;
        private String defaultValue;
        private Validation validation;
        private String help;
        private String displayName;
        private String hint;

        /**
         * Default constructor.
         */
        public Parameter() {}
    }

    /**
     * Validation Component for the parameter.
     */
    @Data
    public static class Validation {
        private List<Integer> numericRange;
        /**
         * Default constructor.
         */
        public Validation() {}
    }



}

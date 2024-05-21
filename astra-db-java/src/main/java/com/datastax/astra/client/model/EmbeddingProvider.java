package com.datastax.astra.client.model;

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

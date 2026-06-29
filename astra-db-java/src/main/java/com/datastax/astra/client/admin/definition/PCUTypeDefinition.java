package com.datastax.astra.client.admin.definition;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2026 DataStax
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

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PCUTypeDefinition {
    String type;
    String region;
    CloudProviderType cloudProvider;
    Map<String, Object> details;
    boolean enabled;

    /**
     * Constructor to map from PCUType to PCUTypeDefinition.
     *
     * @param pcuType the PCUType from devops API
     */
    public PCUTypeDefinition(PCUType pcuType) {
        if (pcuType != null) {
            this.type = pcuType.getType();
            this.region = pcuType.getRegion();
            // Map provider string to CloudProviderType enum
            if (pcuType.getProvider() != null) {
                try {
                    this.cloudProvider = CloudProviderType.valueOf(pcuType.getProvider().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // If provider string doesn't match enum, leave cloudProvider as null
                    this.cloudProvider = null;
                }
            }
            this.details = pcuType.getDetails();
            this.enabled = pcuType.isEnabled();
        }
    }
}

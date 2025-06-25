package com.datastax.astra.client.core.vectorize;

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

/**
 * Enum representing the status of an embedding provider.
 * <p>
 * - SUPPORTED: The provider is currently supported and available for use.
 * - DEPRECATED: The provider is deprecated and may be removed in future versions.
 * - END_OF_LIFE: The provider has reached its end of life and is no longer supported.
 */
public enum SupportModelStatus {
    SUPPORTED,DEPRECATED, END_OF_LIFE
}

package com.datastax.astra.test.integration.utils;

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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable tests only when reranking feature is enabled.
 * Tests will be skipped (not failed) if reranking is not enabled in test configuration.
 * <p>
 * The reranking feature is controlled by the {@code test.reranking} property in test configuration.
 * <p>
 * Usage:
 * <pre>
 * {@literal @}EnabledIfReranking
 * {@literal @}Test
 * void should_test_reranking_feature() {
 *     // test runs only if test.reranking=true
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RerankingEnabledCondition.class)
public @interface EnabledIfReranking {
}

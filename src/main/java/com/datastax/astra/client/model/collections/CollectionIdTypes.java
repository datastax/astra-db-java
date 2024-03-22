package com.datastax.astra.client.model.collections;

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

/**
 * List of possible types for the collection 'defaultId'.
 */
public enum CollectionIdTypes {

    /** represent a bson ObjectId. */
    objectId,

    /** uuid in version v6 allowing natural ordering. */
    uuidv6,

    /** uuid in version v7 random and time-based. */
    uuidv7,

    /** uuid v4, the default random uuid, */
    uuid
}

package com.datastax.astra.internal.utils;

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


import lombok.Getter;

@Getter
public class FieldPathSegment {

    private final String strValue;

    private final Integer idxValue;

    public FieldPathSegment(String strValue) {
        Assert.notNull(strValue, "strValue cannot be null");
        this.strValue = strValue;
        this.idxValue = null;
    }

    public FieldPathSegment(Integer idxValue) {
        Assert.notNull(idxValue, "idxValue cannot be null");
        this.idxValue = idxValue;
        this.strValue = null;
    }

    public static FieldPathSegment of(String strValue) {
        return new FieldPathSegment(strValue);
    }

    public static FieldPathSegment of(Integer idxValue) {
        return new FieldPathSegment(idxValue);
    }

    public boolean isString() {
        return strValue != null;
    }


}

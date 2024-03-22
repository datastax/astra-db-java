package com.datastax.astra.client.model.filter;

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

import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

/**
 * Filter Builder.
 */
@Getter
public class Filter extends Document {

    /**
     * Default constructor.
     */
    public Filter() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param json
     *      filter expression as JSON
     */
    @SuppressWarnings("unchecked")
    public Filter(String json) {
        super();
        this.documentMap.putAll(JsonUtils.unmarshallBeanForDataApi(json, Map.class));
    }

    /**
     * Default constructor.
     *
     * @param obj
     *      filter expression as JSON
     */
    public Filter(Map<String, Object> obj) {
        super();
        this.documentMap.putAll(obj);
    }

    /**
     * Create a filter from a where clause.
     *
     * @param fieldName
     *      fieldName
     * @param cond
     *      condition
     * @param value
     *      object value
     */
    public Filter(@NonNull String fieldName, @NonNull FilterOperator cond, @NonNull Object value) {
        super();
        documentMap.put(fieldName, Map.of(cond.getOperator(), value));
    }

    /**
     * Work with arguments.
     *
     * @param fieldName
     *      current field name.
     * @return
     *      builder for the filter
     */
    public FilterBuilder where(String fieldName) {
        Assert.hasLength(fieldName, "fieldName");
        return new FilterBuilder(this, fieldName);
    }

    /**
     * Build where clause with operator
     *
     * @param fieldName
     *      current field name
     * @param cond
     *      current condition
     * @param value
     *      value for the condition
     * @return
     *      current
     */
    public Filter where(String fieldName, FilterOperator cond, Object value) {
        documentMap.put(fieldName, Map.of(cond.getOperator(), value));
        return this;
    }

    /**
     * Adding a ADD keyword.
     *
     * @return
     *      current list
     */
    public FilterBuilderList and() {
        return new FilterBuilderList(this, "$and");
    }

    /**
     * Adding a OR keyword.
     *
     * @return
     *      current list
     */
    public FilterBuilderList or() {
        return new FilterBuilderList(this, "$or");
    }

    /**
     * Adding a NOT keyword.
     *
     * @return
     *      current list
     */
    public FilterBuilderList not() {
        return new FilterBuilderList(this, "$not");
    }

    /**
     * Build a filter for find by id.
     *
     * @param id
     *      identifier
     * @return
     *      filter
     */
    public static Filter findById(String id) {
        return new Filter().where("_id").isEqualsTo(id);
    }

    /* {@inheritDoc} */
    @Override
    public String toString() {
       return toJson();
    }

    /**
     * Express the json filter as a string.
     *
     * @return
     *      json expression
     */
    public String toJson() {
        return JsonUtils.marshallForDataApi(this);
    }

}

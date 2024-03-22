package com.datastax.astra.client.model.update;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.utils.JsonUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Update extends Document {

    /**
     * Default constructor.
     */
    public Update() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param json
     *      filter expression as JSON
     */
    @SuppressWarnings("unchecked")
    public Update(String json) {
        super();
        //this.filter = JsonUtils.unmarshallBean(json, Map.class);
        this.documentMap.putAll(JsonUtils.unmarshallBeanForDataApi(json, Map.class));
    }

    /**
     * Default constructor.
     *
     * @param obj
     *      filter expression as JSON
     */
    public Update(Map<String, Object> obj) {
        super();
        this.documentMap.putAll(obj);
    }

    public static Update create() {
        return new Update();
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param offset
     *      increment value
     * @return
     *      reference to self
     */
    public Update inc(String key, Integer offset) {
        return update("$inc", key, offset);
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields map to inccrement which each time the value
     * @return
     *      reference to self
     */
    public Update inc(Map<String, Integer> fields) {
        if (fields !=null) fields.forEach(this::inc);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fieldName
     *      field name
     * @return
     *      reference to self
     */
    public Update unset(String fieldName) {
        return update("$unset", fieldName, "");
    }

    /**
     * Builder pattern
     *
     * @param fieldNames
     *      list of fields to unset
     * @return
     *      reference to self
     */
    public Update unset(List<String> fieldNames) {
        if (fieldNames != null) fieldNames.forEach(this::unset);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update set(String key, Object value) {
        return update("$set", key, value);
    }

    public Update set(Map<String, Integer> fields) {
        if (fields !=null) fields.forEach(this::set);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update min(String key, Object value) {
        return update("$min", key, value);
    }

    public Update min(Map<String, Integer> fields) {
        if (fields !=null) fields.forEach(this::min);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update push(String key, Object value) {
        return update("$push", key, value);
    }

    public Update push(Map<String, Integer> fields) {
        if (fields !=null) fields.forEach(this::push);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update pop(String key, Object value) {
        return update("$pop", key, value);
    }

    public Update pop(Map<String, Integer> fields) {
        if (fields !=null) fields.forEach(this::pop);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param key
     *      field name
     * @param values
     *      filed list values
     * @param position
     *      where to push in the list
     * @return
     *      reference to self
     */
    public Update pushEach(String key, List<Object> values, Integer position) {
        // The value need "$each"
        Map<String, Object> value = new HashMap<>();
        value.put("$each", values);
        if (null != position) {
            value.put("$position", position);
        }
        return update("$push", key, value);
    }

    /**
     * Builder pattern
     *
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    public Update addToSet(String key, Object value) {
        return update("$addToSet", key, value);
    }

    public Update rename(String key, Object value) {
        return update("$rename", key, value);
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update rename(Map<String, String> fields) {
        if (fields !=null) fields.forEach(this::rename);
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateCurrentDate(List<String> fields) {
        fields.stream().forEach(key -> update("$currentDate", key, true));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateMul(Map<String, Double> fields) {
        fields.forEach((key, value) -> update("$mul", key, value));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param fields
     *      fields to rename
     * @return
     *      reference to self
     */
    public Update updateSetOnInsert(Map<String, Double> fields) {
        fields.forEach((key, value) -> update("$setOnInsert", key, value));
        return this;
    }

    /**
     * Builder pattern
     *
     * @param operation
     *      operation on update
     * @param key
     *      field name
     * @param value
     *      filed value
     * @return
     *      reference to self
     */
    @SuppressWarnings("unchecked")
    private Update update(String operation, String key, Object value) {
        documentMap.computeIfAbsent(operation, k -> new LinkedHashMap<>());
        ((Map<String, Object>) documentMap.get(operation)).put(key, value);
        return this;
    }


}

package com.datastax.astra.internal.api;

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class DataAPIDocumentResponse implements Serializable {

    @JsonProperty("_id")
    private List<Object> id;

    private String status;

    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}

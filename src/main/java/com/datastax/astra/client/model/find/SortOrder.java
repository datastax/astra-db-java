package com.datastax.astra.client.model.find;

public enum SortOrder {
    ASCENDING(1),
    DESCENDING(-1);
    private SortOrder(Integer order) {
        this.order = order;
    }

    private Integer order;
    public Integer getOrder() {
        return order;
    }

}
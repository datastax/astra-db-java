package com.datastax.astra.test.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProductString  extends Product<String> {

    public ProductString(String id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
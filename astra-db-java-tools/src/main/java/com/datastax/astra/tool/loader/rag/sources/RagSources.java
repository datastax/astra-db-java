package com.datastax.astra.tool.loader.rag.sources;


/**
 * Based on the source we will add the RAG.
 */
public enum RagSources {

    SETTINGS("settings"),

    DOCUMENT("document"),

    ASSET("asset"),

    CHAT("chat"),

    URL("url"),

    FILE("file");

    RagSources(String key) {
        this.key = key;
    }

    private String key;

    public String getKey() {
        return key;
    }
}

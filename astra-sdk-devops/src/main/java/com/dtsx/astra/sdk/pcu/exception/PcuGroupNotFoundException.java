package com.dtsx.astra.sdk.pcu.exception;

import lombok.Getter;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // what a stupid rule ~ sincerely, a haskeller
public class PcuGroupNotFoundException extends RuntimeException {
    @Getter
    private final Optional<String> title;

    @Getter
    private final Optional<String> id;

    private PcuGroupNotFoundException(Optional<String> title, Optional<String> id) {
        super("PCU group " + title.or(() -> id).map(s -> "'" + s + "' ").orElse("") + "has not been found.");
        this.title = title;
        this.id = id;
    }

    public static PcuGroupNotFoundException forTitle(String title) {
        return new PcuGroupNotFoundException(Optional.of(title), Optional.empty());
    }

    public static PcuGroupNotFoundException forId(String id) {
        return new PcuGroupNotFoundException(Optional.empty(), Optional.of(id));
    }
}

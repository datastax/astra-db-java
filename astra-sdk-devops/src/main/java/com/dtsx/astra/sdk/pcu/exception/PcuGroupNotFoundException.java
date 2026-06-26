package com.dtsx.astra.sdk.pcu.exception;

import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

/**
 * Exception thrown when a PCU (Processing Capacity Units) Group cannot be found.
 * Can be constructed with either a title or an ID for more specific error messages.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // what a stupid rule ~ sincerely, a haskeller
public class PcuGroupNotFoundException extends RuntimeException {
    /**
     * Optional title of the PCU group that was not found.
     */
    @Getter
    private final Optional<String> title;

    /**
     * Optional ID of the PCU group that was not found.
     */
    @Getter
    private final Optional<UUID> id;

    /**
     * Private constructor for creating the exception.
     *
     * @param title
     *      optional title of the PCU group
     * @param id
     *      optional ID of the PCU group
     */
    private PcuGroupNotFoundException(Optional<String> title, Optional<UUID> id) {
        super(buildErrorMessage(title, id));
        this.title = title;
        this.id = id;
    }

    /**
     * Build Error Message.
     * @param title
     *      title
     * @param id
     *      PUC group identifier
     * @return
     *      exception
     */
    private static String buildErrorMessage(Optional<String> title, Optional<UUID> id) {
        String identifier = title
                .map(t -> "'" + t + "'")
                .orElseGet(() -> id.map(uuid -> "'" + uuid + "'").orElse(""));
        return "PCU group " + identifier + " has not been found.";
    }

    /**
     * Creates an exception for a PCU group not found by title.
     *
     * @param title
     *      the title of the PCU group that was not found
     * @return
     *      exception instance
     */
    public static PcuGroupNotFoundException forTitle(String title) {
        return new PcuGroupNotFoundException(Optional.of(title), Optional.empty());
    }

    /**
     * Creates an exception for a PCU group not found by ID.
     *
     * @param id
     *      the ID of the PCU group that was not found
     * @return
     *      exception instance
     */
    public static PcuGroupNotFoundException forId(UUID id) {
        return new PcuGroupNotFoundException(Optional.empty(), Optional.of(id));
    }
}

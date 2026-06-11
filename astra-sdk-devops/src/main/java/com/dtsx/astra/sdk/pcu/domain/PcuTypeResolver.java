package com.dtsx.astra.sdk.pcu.domain;

import java.util.List;

/**
 * Utility class for resolving default PCU types based on availability and configuration.
 */
public class PcuTypeResolver {

    /**
     * Resolves the default PCU type based on available types and mini PCU configuration.
     * 
     * <p>Selection priority when mini PCU is enabled:
     * SMALL > MEDIUM > GENERAL_PURPOSE > CACHE_OPTIMIZED
     * 
     * <p>Selection priority when mini PCU is disabled:
     * GENERAL_PURPOSE > CACHE_OPTIMIZED (SMALL and MEDIUM are ignored)
     *
     * @param availableTypes list of available PCU family types
     * @param miniPcuEnabled whether mini PCU is enabled
     * @return the selected PCU family, or null if no suitable type is available
     */
    public static PcuInstanceType resolveDefaultPcuType(List<PcuInstanceType> availableTypes, boolean miniPcuEnabled) {
        if (availableTypes == null || availableTypes.isEmpty()) {
            return null;
        }

        if (miniPcuEnabled) {
            // Priority: SMALL > MEDIUM > GENERAL_PURPOSE > CACHE_OPTIMIZED
            if (availableTypes.contains(PcuInstanceType.SMALL)) {
                return PcuInstanceType.SMALL;
            }
            if (availableTypes.contains(PcuInstanceType.MEDIUM)) {
                return PcuInstanceType.MEDIUM;
            }
            if (availableTypes.contains(PcuInstanceType.GENERAL_PURPOSE)) {
                return PcuInstanceType.GENERAL_PURPOSE;
            }
            if (availableTypes.contains(PcuInstanceType.CACHE_OPTIMIZED)) {
                return PcuInstanceType.CACHE_OPTIMIZED;
            }
        } else {
            // Priority: GENERAL_PURPOSE > CACHE_OPTIMIZED (ignore SMALL and MEDIUM)
            if (availableTypes.contains(PcuInstanceType.GENERAL_PURPOSE)) {
                return PcuInstanceType.GENERAL_PURPOSE;
            }
            if (availableTypes.contains(PcuInstanceType.CACHE_OPTIMIZED)) {
                return PcuInstanceType.CACHE_OPTIMIZED;
            }
        }

        return null;
    }

    private PcuTypeResolver() {
        // Utility class - prevent instantiation
    }
}

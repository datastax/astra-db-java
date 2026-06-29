package com.dtsx.astra.sdk.pcu.domain;

import java.util.List;

/**
 * Utility class for resolving default PCU types based on availability and configuration.
 */
public class PCUTypeResolver {

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
    public static PCUInstanceType resolveDefaultPcuType(List<PCUInstanceType> availableTypes, boolean miniPcuEnabled) {
        if (availableTypes == null || availableTypes.isEmpty()) {
            return null;
        }

        if (miniPcuEnabled) {
            // Priority: SMALL > MEDIUM > GENERAL_PURPOSE > CACHE_OPTIMIZED
            if (availableTypes.contains(PCUInstanceType.small)) {
                return PCUInstanceType.small;
            }
            if (availableTypes.contains(PCUInstanceType.medium)) {
                return PCUInstanceType.medium;
            }
            if (availableTypes.contains(PCUInstanceType.generalPurpose)) {
                return PCUInstanceType.generalPurpose;
            }
            if (availableTypes.contains(PCUInstanceType.cacheOptimized)) {
                return PCUInstanceType.cacheOptimized;
            }
        } else {
            // Priority: GENERAL_PURPOSE > CACHE_OPTIMIZED (ignore SMALL and MEDIUM)
            if (availableTypes.contains(PCUInstanceType.generalPurpose)) {
                return PCUInstanceType.generalPurpose;
            }
            if (availableTypes.contains(PCUInstanceType.cacheOptimized)) {
                return PCUInstanceType.cacheOptimized;
            }
        }

        return null;
    }

    private PCUTypeResolver() {
        // Utility class - prevent instantiation
    }
}

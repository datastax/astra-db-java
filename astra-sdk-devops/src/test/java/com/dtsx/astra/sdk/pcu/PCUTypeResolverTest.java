package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PCUInstanceType;
import com.dtsx.astra.sdk.pcu.domain.PCUTypeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for PcuTypeResolver.
 */
@DisplayName("PcuTypeResolver Tests")
class PCUTypeResolverTest {

    @Nested
    @DisplayName("Mini PCU Enabled")
    class MiniPcuEnabled {

        @Test
        @DisplayName("Selects SMALL when SMALL is available")
        void selectsSmallWhenSmallIsAvailable() {
            List<PCUInstanceType> availableTypes = Arrays.asList(
                PCUInstanceType.small,
                PCUInstanceType.medium,
                PCUInstanceType.generalPurpose,
                PCUInstanceType.cacheOptimized
            );
            
            assertEquals(
                PCUInstanceType.small,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects MEDIUM when SMALL is unavailable but MEDIUM exists")
        void selectsMediumWhenSmallUnavailableButMediumExists() {
            List<PCUInstanceType> availableTypes = Arrays.asList(
                PCUInstanceType.medium,
                PCUInstanceType.generalPurpose
            );
            
            assertEquals(
                PCUInstanceType.medium,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects GENERAL_PURPOSE when only GENERAL_PURPOSE and CACHE_OPTIMIZED exist")
        void selectsGeneralPurposeWhenOnlyGeneralPurposeAndCacheOptimizedExist() {
            List<PCUInstanceType> availableTypes = Arrays.asList(
                PCUInstanceType.generalPurpose,
                PCUInstanceType.cacheOptimized
            );
            
            assertEquals(
                PCUInstanceType.generalPurpose,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects CACHE_OPTIMIZED when it is the only available type")
        void selectsCacheOptimizedWhenItIsTheOnlyAvailableType() {
            List<PCUInstanceType> availableTypes = Collections.singletonList(
                PCUInstanceType.cacheOptimized
            );
            
            assertEquals(
                PCUInstanceType.cacheOptimized,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Returns null when no types are available")
        void returnsNullWhenNoTypesAreAvailable() {
            assertNull(PCUTypeResolver.resolveDefaultPcuType(Collections.emptyList(), true));
        }
    }

    @Nested
    @DisplayName("Mini PCU Disabled")
    class MiniPcuDisabled {

        @Test
        @DisplayName("Selects GENERAL_PURPOSE when GENERAL_PURPOSE is available")
        void selectsGeneralPurposeWhenGeneralPurposeIsAvailable() {
            List<PCUInstanceType> availableTypes = Arrays.asList(
                PCUInstanceType.generalPurpose,
                PCUInstanceType.cacheOptimized
            );
            
            assertEquals(
                PCUInstanceType.generalPurpose,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, false)
            );
        }

        @Test
        @DisplayName("Selects CACHE_OPTIMIZED when it is the only available type")
        void selectsCacheOptimizedWhenItIsTheOnlyAvailableType() {
            List<PCUInstanceType> availableTypes = Collections.singletonList(
                PCUInstanceType.cacheOptimized
            );
            
            assertEquals(
                PCUInstanceType.cacheOptimized,
                PCUTypeResolver.resolveDefaultPcuType(availableTypes, false)
            );
        }

        @Test
        @DisplayName("Returns null when only SMALL or MEDIUM are available")
        void returnsNullWhenOnlySmallOrMediumAreAvailable() {
            List<PCUInstanceType> availableTypes = Arrays.asList(
                PCUInstanceType.small,
                PCUInstanceType.medium
            );
            
            assertNull(PCUTypeResolver.resolveDefaultPcuType(availableTypes, false));
        }
    }
}

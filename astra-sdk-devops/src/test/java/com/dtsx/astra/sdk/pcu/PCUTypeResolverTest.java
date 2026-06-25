package com.dtsx.astra.sdk.pcu;

import com.dtsx.astra.sdk.pcu.domain.PcuInstanceType;
import com.dtsx.astra.sdk.pcu.domain.PcuTypeResolver;
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
class PcuTypeResolverTest {

    @Nested
    @DisplayName("Mini PCU Enabled")
    class MiniPcuEnabled {

        @Test
        @DisplayName("Selects SMALL when SMALL is available")
        void selectsSmallWhenSmallIsAvailable() {
            List<PcuInstanceType> availableTypes = Arrays.asList(
                PcuInstanceType.SMALL,
                PcuInstanceType.MEDIUM,
                PcuInstanceType.GENERAL_PURPOSE,
                PcuInstanceType.CACHE_OPTIMIZED
            );
            
            assertEquals(
                PcuInstanceType.SMALL,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects MEDIUM when SMALL is unavailable but MEDIUM exists")
        void selectsMediumWhenSmallUnavailableButMediumExists() {
            List<PcuInstanceType> availableTypes = Arrays.asList(
                PcuInstanceType.MEDIUM,
                PcuInstanceType.GENERAL_PURPOSE
            );
            
            assertEquals(
                PcuInstanceType.MEDIUM,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects GENERAL_PURPOSE when only GENERAL_PURPOSE and CACHE_OPTIMIZED exist")
        void selectsGeneralPurposeWhenOnlyGeneralPurposeAndCacheOptimizedExist() {
            List<PcuInstanceType> availableTypes = Arrays.asList(
                PcuInstanceType.GENERAL_PURPOSE,
                PcuInstanceType.CACHE_OPTIMIZED
            );
            
            assertEquals(
                PcuInstanceType.GENERAL_PURPOSE,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Selects CACHE_OPTIMIZED when it is the only available type")
        void selectsCacheOptimizedWhenItIsTheOnlyAvailableType() {
            List<PcuInstanceType> availableTypes = Collections.singletonList(
                PcuInstanceType.CACHE_OPTIMIZED
            );
            
            assertEquals(
                PcuInstanceType.CACHE_OPTIMIZED,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, true)
            );
        }

        @Test
        @DisplayName("Returns null when no types are available")
        void returnsNullWhenNoTypesAreAvailable() {
            assertNull(PcuTypeResolver.resolveDefaultPcuType(Collections.emptyList(), true));
        }
    }

    @Nested
    @DisplayName("Mini PCU Disabled")
    class MiniPcuDisabled {

        @Test
        @DisplayName("Selects GENERAL_PURPOSE when GENERAL_PURPOSE is available")
        void selectsGeneralPurposeWhenGeneralPurposeIsAvailable() {
            List<PcuInstanceType> availableTypes = Arrays.asList(
                PcuInstanceType.GENERAL_PURPOSE,
                PcuInstanceType.CACHE_OPTIMIZED
            );
            
            assertEquals(
                PcuInstanceType.GENERAL_PURPOSE,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, false)
            );
        }

        @Test
        @DisplayName("Selects CACHE_OPTIMIZED when it is the only available type")
        void selectsCacheOptimizedWhenItIsTheOnlyAvailableType() {
            List<PcuInstanceType> availableTypes = Collections.singletonList(
                PcuInstanceType.CACHE_OPTIMIZED
            );
            
            assertEquals(
                PcuInstanceType.CACHE_OPTIMIZED,
                PcuTypeResolver.resolveDefaultPcuType(availableTypes, false)
            );
        }

        @Test
        @DisplayName("Returns null when only SMALL or MEDIUM are available")
        void returnsNullWhenOnlySmallOrMediumAreAvailable() {
            List<PcuInstanceType> availableTypes = Arrays.asList(
                PcuInstanceType.SMALL,
                PcuInstanceType.MEDIUM
            );
            
            assertNull(PcuTypeResolver.resolveDefaultPcuType(availableTypes, false));
        }
    }
}

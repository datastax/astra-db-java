package com.datastax.astra.client.core.lexical;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;

/**
 * Enum representing different types of analyzers.
 * Each enum constant corresponds to a specific analyzer type.
 */
@Getter
public enum AnalyzerTypes {

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    STANDARD("standard"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    LETTER("letter"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    LOWERCASE("lowercase"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    WHITESPACE("whitespace"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    N_GRAM("n-gram"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    EDGE_N_GRAM("edge_n-gram"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    KEYWORD("keyword"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    SIMPLE("simple"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    STOP("stop"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    CLASSIC("classic"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    PATH_HIERARCHY("pathHierarchy"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    PATTERN("pattern"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    SIMPLE_PATTERN("simplePattern"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    SIMPLE_PATTERN_SPLIT("simplePatternSplit"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    THAI("thai"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    UAX29_URL_EMAIL("uax29UrlEmail"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    WIKIPEDIA("wikipedia"),

    // --- Language Analyzers ---

    /**
     * Language analyzers
     */
    ARABIC("arabic"),

    /**
     * Language analyzers
     */
    ARMENIAN("armenian"),

    /**
     * Language analyzers
     */
    BASQUE("basque"),

    /**
     * Language analyzers
     */
    BENGALI("bengali"),

    /**
     * Language analyzers
     */
    BRAZILIAN("brazilian"),

    /**
     * Language analyzers
     */
    BULGARIAN("bulgarian"),

    /**
     * Language analyzers
     */
    CATALAN("catalan"),

    /**
     * Language analyzers
     */
    CJK("cjk"),

    /**
     * Language analyzers
     */
    CZECH("czech"),

    /**
     * Language analyzers
     */
    DANISH("danish"),

    /**
     * Language analyzers
     */
    DUTCH("dutch"),

    /**
     * Language analyzers
     */
    ENGLISH("english"),

    /**
     * Language analyzers
     */
    ESTONIAN("estonian"),

    /**
     * Language analyzers
     */
    FINNISH("finnish"),

    /**
     * Language analyzers
     */
    FRENCH("french"),

    /**
     * Language analyzers
     */
    GALICIAN("galician"),

    /**
     * Language analyzers
     */
    GERMAN("german"),

    /**
     * Language analyzers
     */
    GREEK("greek"),

    /**
     * Language analyzers
     */
    HINDI("hindi"),

    /**
     * Language analyzers
     */
    HUNGARIAN("hungarian"),

    /**
     * Language analyzers
     */
    INDONESIAN("indonesian"),

    /**
     * Language analyzers
     */
    IRISH("irish"),

    /**
     * Language analyzers
     */
    ITALIAN("italian"),

    /**
     * Language analyzers
     */
    LATVIAN("latvian"),

    /**
     * Language analyzers
     */
    LITHUANIAN("lithuanian"),

    /**
     * Language analyzers
     */
    NORWEGIAN("norwegian"),

    /**
     * Language analyzers
     */
    PERSIAN("persian"),

    /**
     * Language analyzers
     */
    PORTUGUESE("portuguese"),

    /**
     * Language analyzers
     */
    ROMANIAN("romanian"),

    /**
     * Language analyzers
     */
    RUSSIAN("russian"),

    /**
     * Language analyzers
     */
    SORANI("sorani"),

    /**
     * Language analyzers
     */
    SPANISH("spanish"),

    /**
     * Language analyzers
     */
    SWEDISH("swedish"),

    /**
     * Language analyzers
     */
    TURKISH("turkish"),

    // --- Filters and Normalizers ---

    /**
     * Filters and Normalizers
     */
    CJK_WIDTH("cjkWidth"),

    /**
     * Filters and Normalizers
     */
    HTML_STRIP("htmlstrip"),

    /**
     * Filters and Normalizers
     */
    MAPPING("mapping"),

    /**
     * Filters and Normalizers
     */
    PERSIAN_NORMALIZATION("persianNormalization"),

    /**
     * Filters and Normalizers
     */
    PATTERN_REPLACE("patternReplace"),

    /**
     * Filters and Normalizers
     */
    APOSTROPHE("apostrophe"),

    /**
     * Filters and Normalizers
     */
    WORD_DELIMITER_GRAPH("wordDelimiterGraph"),

    /**
     * Filters and Normalizers
     */
    PORTUGUESE_LIGHT_STEM("portugueseLightStem"),

    /**
     * Filters and Normalizers
     */
    LATVIAN_STEM("latvianStem"),

    /**
     * Filters and Normalizers
     */
    DROP_IF_FLAGGED("dropIfFlagged"),

    /**
     * Filters and Normalizers
     */
    KEEP_WORD("keepWord"),

    /**
     * Filters and Normalizers
     */
    INDIC_NORMALIZATION("indicNormalization"),

    /**
     * Filters and Normalizers
     */
    BENGALI_STEM("bengaliStem"),

    /**
     * Filters and Normalizers
     */
    TURKISH_LOWERCASE("turkishLowercase"),

    /**
     * Filters and Normalizers
     */
    GALICIAN_STEM("galicianStem"),

    /**
     * Filters and Normalizers
     */
    BENGALI_NORMALIZATION("bengaliNormalization"),

    /**
     * Filters and Normalizers
     */
    PORTUGUESE_MINIMAL_STEM("portugueseMinimalStem"),

    /**
     * Filters and Normalizers
     */
    GALICIAN_MINIMAL_STEM("galicianMinimalStem"),

    /**
     * Filters and Normalizers
     */
    SWEDISH_MINIMAL_STEM("swedishMinimalStem"),

    /**
     * Filters and Normalizers
     */
    LIMIT_TOKEN_COUNT("limitTokenCount"),

    /**
     * Filters and Normalizers
     */
    ITALIAN_LIGHT_STEM("italianLightStem"),

    /**
     * Filters and Normalizers
     */
    WORD_DELIMITER("wordDelimiter"),

    /**
     * Filters and Normalizers
     */
    TELUGU_STEM("teluguStem"),

    /**
     * Filters and Normalizers
     */
    HUNGARIAN_LIGHT_STEM("hungarianLightStem"),


    /**
     * Filters and Normalizers
     */
    PROTECTED_TERM("protectedTerm"),

    /**
     * Filters and Normalizers
     */
    CAPITALIZATION("capitalization"),

    /**
     * Filters and Normalizers
     */
    HYPHENATED_WORDS("hyphenatedWords"),

    /**
     * Filters and Normalizers
     */
    TYPE("type"),

    /**
     * Filters and Normalizers
     */
    KEYWORD_MARKER("keywordMarker"),

    /**
     * Filters and Normalizers
     */
    FRENCH_MINIMAL_STEM("frenchMinimalStem"),

    /**
     * Filters and Normalizers
     */
    K_STEM("kStem"),

    /**
     * Filters and Normalizers
     */
    SWEDISH_LIGHT_STEM("swedishLightStem"),

    /**
     * Filters and Normalizers
     */
    SORANI_NORMALIZATION("soraniNormalization"),

    /**
     * Filters and Normalizers
     */
    COMMON_GRAMS_QUERY("commonGramsQuery"),

    /**
     * Filters and Normalizers
     */
    NUMERIC_PAYLOAD("numericPayload"),

    /**
     * Filters and Normalizers
     */
    PERSIAN_STEM("persianStem"),

    /**
     * Filters and Normalizers
     */
    LIMIT_TOKEN_OFFSET("limitTokenOffset"),

    /**
     * Filters and Normalizers
     */
    HUNSPELL_STEM("hunspellStem"),

    /**
     * Filters and Normalizers
     */
    SORANI_STEM("soraniStem"),

    /**
     * Filters and Normalizers
     */
    CZECH_STEM("czechStem"),

    /**
     * Filters and Normalizers
     */
    NORWEGIAN_MINIMAL_STEM("norwegianMinimalStem"),

    /**
     * Filters and Normalizers
     */
    ENGLISH_MINIMAL_STEM("englishMinimalStem"),

    /**
     * Filters and Normalizers
     */
    NORWEGIAN_LIGHT_STEM("norwegianLightStem"),

    /**
     * Filters and Normalizers
     */
    GERMAN_MINIMAL_STEM("germanMinimalStem"),

    /**
     * Filters and Normalizers
     */
    SNOWBALL_PORTER("snowballPorter"),

    /**
     * Filters and Normalizers
     */
    REMOVE_DUPLICATES("removeDuplicates"),

    /**
     * Filters and Normalizers
     */
    MIN_HASH("minHash"),

    /**
     * Filters and Normalizers
     */
    KEYWORD_REPEAT("keywordRepeat"),

    /**
     * Filters and Normalizers
     */
    GERMAN_NORMALIZATION("germanNormalization"),

    /**
     * Filters and Normalizers
     */
    DICTIONARY_COMPOUND_WORD("dictionaryCompoundWord"),

    /**
     * Filters and Normalizers
     */
    SYNONYM_GRAPH("synonymGraph"),

    /**
     * Filters and Normalizers
     */
    ENGLISH_POSSESSIVE("englishPossessive"),

    /**
     * Filters and Normalizers
     */
    SPANISH_MINIMAL_STEM("spanishMinimalStem"),

    /**
     * Filters and Normalizers
     */
    FIXED_SHINGLE("fixedShingle"),

    /**
     * Filters and Normalizers
     */
    PATTERN_TYPING("patternTyping"),

    /**
     * Filters and Normalizers
     */
    FRENCH_LIGHT_STEM("frenchLightStem"),

    /**
     * Filters and Normalizers
     */
    TRIM("trim"),

    /**
     * Filters and Normalizers
     */
    INDONESIAN_STEM("indonesianStem"),

    /**
     * Filters and Normalizers
     */
    SPANISH_PLURAL_STEM("spanishPluralStem"),

    /**
     * Filters and Normalizers
     */
    HINDI_STEM("hindiStem"),

    /**
     * Filters and Normalizers
     */
    SCANDINAVIAN_FOLDING("scandinavianFolding"),

    /**
     * Filters and Normalizers
     */
    DELIMITED_BOOST("delimitedBoost"),

    /**
     * Filters and Normalizers
     */
    COMMON_GRAMS("commonGrams"),

    /**
     * Filters and Normalizers
     */
    REVERSE_STRING("reverseString"),

    /**
     * Filters and Normalizers
     */
    FINGERPRINT("fingerprint"),

    /**
     * Filters and Normalizers
     */
    FINNISH_LIGHT_STEM("finnishLightStem"),

    /**
     * Filters and Normalizers
     */
    GREEK_STEM("greekStem"),

    /**
     * Filters and Normalizers
     */
    PORTER_STEM("porterStem"),

    /**
     * Filters and Normalizers
     */
    LIMIT_TOKEN_POSITION("limitTokenPosition"),

    /**
     * Filters and Normalizers
     */
    TYPE_AS_SYNONYM("typeAsSynonym"),

    /**
     * Filters and Normalizers
     */
    TOKEN_OFFSET_PAYLOAD("tokenOffsetPayload"),

    /**
     * Filters and Normalizers
     */
    CODEPOINT_COUNT("codepointCount"),

    /**
     * Filters and Normalizers
     */
    BULGARIAN_STEM("bulgarianStem"),

    /**
     * Filters and Normalizers
     */
    SYNONYM("synonym"),

    /**
     * Filters and Normalizers
     */
    GERMAN_STEM("germanStem"),

    /**
     * Filters and Normalizers
     */
    ASCII_FOLDING("asciiFolding"),

    /**
     * Filters and Normalizers
     */
    DECIMAL_DIGIT("decimalDigit"),

    /**
     * Filters and Normalizers
     */
    WORD2VEC_SYNONYM("Word2VecSynonym"),

    /**
     * Filters and Normalizers
     */
    SCANDINAVIAN_NORMALIZATION("scandinavianNormalization"),

    /**
     * Filters and Normalizers
     */
    RUSSIAN_LIGHT_STEM("russianLightStem"),

    /**
     * Filters and Normalizers
     */
    SERBIAN_NORMALIZATION("serbianNormalization"),

    /**
     * Filters and Normalizers
     */
    ELISION("elision"),

    /**
     * Filters and Normalizers
     */
    PORTUGUESE_STEM("portugueseStem"),

    /**
     * Filters and Normalizers
     */
    ARABIC_NORMALIZATION("arabicNormalization"),

    /**
     * Filters and Normalizers
     */
    LENGTH("length"),

    /**
     * Filters and Normalizers
     */
    GREEK_LOWERCASE("greekLowercase"),

    /**
     * Filters and Normalizers
     */
    CONCATENATE_GRAPH("concatenateGraph"),

    /**
     * Filters and Normalizers
     */
    FLATTEN_GRAPH("flattenGraph"),

    /**
     * Filters and Normalizers
     */
    FIX_BROKEN_OFFSETS("fixBrokenOffsets"),

    /**
     * Filters and Normalizers
     */
    TRUNCATE("truncate"),

    /**
     * Filters and Normalizers
     */
    CJK_BIGRAM("cjkBigram"),

    /**
     * Filters and Normalizers
     */
    BRAZILIAN_STEM("brazilianStem"),

    /**
     * Filters and Normalizers
     */
    UPPERCASE("uppercase"),

    /**
     * Filters and Normalizers
     */
    DATE_RECOGNIZER("dateRecognizer"),

    /**
     * Filters and Normalizers
     */
    TELUGU_NORMALIZATION("teluguNormalization"),

    /**
     * Filters and Normalizers
     */
    SHINGLE("shingle"),

    /**
     * Filters and Normalizers
     */
    NORWEGIAN_NORMALIZATION("norwegianNormalization"),

    /**
     * Filters and Normalizers
     */
    HINDI_NORMALIZATION("hindiNormalization"),

    /**
     * Filters and Normalizers
     */
    DELIMITED_PAYLOAD("delimitedPayload"),

    /**
     * Filters and Normalizers
     */
    SPANISH_LIGHT_STEM("spanishLightStem"),

    /**
     * Filters and Normalizers
     */
    STEMMER_OVERRIDE("stemmerOverride"),

    /**
     * Filters and Normalizers
     */
    PATTERN_CAPTURE_GROUP("patternCaptureGroup"),

    /**
     * Filters and Normalizers
     */
    HYPHENATION_COMPOUND_WORD("hyphenationCompoundWord"),

    /**
     * Filters and Normalizers
     */
    GERMAN_LIGHT_STEM("germanLightStem"),

    /**
     * Filters and Normalizers
     */
    TYPE_AS_PAYLOAD("typeAsPayload"),

    /**
     * Filters and Normalizers
     */
    IRISH_LOWERCASE("irishLowercase"),

    /**
     * Filters and Normalizers
     */
    DELIMITED_TERM_FREQUENCY("delimitedTermFrequency"),

    /**
     * Filters and Normalizers
     */
    ARABIC_STEM("arabicStem");

    /**
     * The string value of the analyzer type.
     */
    final String value;

    /**
     * Constructor for the enum.
     *
     * @param value
     *      string value
     */
    AnalyzerTypes(String value) {
        this.value = value;
    }

    /**
     * Build from the key.
     *
     * @param value
     *      string value
     * @return
     *      enum value
     */
    public static LexicalFilters fromValue(String value) {
        for (LexicalFilters filter : LexicalFilters.values()) {
            if (filter.getValue().equalsIgnoreCase(value)) {
                return filter;
            }
        }
        throw new IllegalArgumentException("Unknown LexicalFilters: " + value);
    }

}

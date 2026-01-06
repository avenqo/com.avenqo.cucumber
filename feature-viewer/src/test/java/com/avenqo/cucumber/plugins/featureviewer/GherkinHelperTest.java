package com.avenqo.cucumber.plugins.featureviewer;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GherkinHelperTest {

    @Test
    void extractDialect_shouldReturnGermanDialect() {
        String content = "# language: de\nFeature: Funktionalität";
        Optional<GherkinDialect> dialect = GherkinHelper.extractDialect(content);

        assertTrue(dialect.isPresent());
        assertEquals("de", dialect.get().getLanguage());
    }

    @Test
    void extractDialect_shouldDefaultToEnglishIfNoLanguageTag() {
        String content = "Feature: Some feature";
        Optional<GherkinDialect> dialect = GherkinHelper.extractDialect(content);

        assertTrue(dialect.isPresent());
        assertEquals("en", dialect.get().getLanguage());
    }

    @Test
    void extractDialect_shouldReturnEmptyForInvalidLanguage() {
        String content = "# language: xx\nFeature: Irgendwas";
        Optional<GherkinDialect> dialect = GherkinHelper.extractDialect(content);

        assertFalse(dialect.isPresent());
    }

    @Test
    void collectAllKeywords_shouldIncludeGermanKeywords() {
        Optional<GherkinDialect> dialect = new GherkinDialectProvider().getDialect("de");
        assertTrue(dialect.isPresent());

        List<String> keywords = GherkinHelper.collectAllKeywords(dialect);

        assertTrue(keywords.contains("Angenommen "));
        assertTrue(keywords.contains("Wenn "));
        assertTrue(keywords.contains("Dann "));
        assertTrue(keywords.contains("Und "));
    }

    @Test
    void collectAllKeywords_shouldReturnEmptyListForEmptyOptional() {
        List<String> keywords = GherkinHelper.collectAllKeywords(Optional.empty());
        assertTrue(keywords.isEmpty());
    }
}

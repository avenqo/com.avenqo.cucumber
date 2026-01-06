package com.avenqo.cucumber.plugins.featureviewer;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeatureViewerPluginTest {

    private FeatureViewerPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new FeatureViewerPlugin();
    }

    @Test
    void parseStepLinesPerScenario_shouldParseSimpleScenario() {
        String feature = """
                Feature: Example Feature

                  Scenario: Login
                    Given a user exists
                    When the user logs in
                    Then the user sees the dashboard
                """;

        var map = plugin.parseStepLinesPerScenario(feature);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Login"));
        List<List<Integer>> occurrences = map.get("Login");
        assertEquals(1, occurrences.size());
        List<Integer> lines = occurrences.get(0);
        assertEquals(List.of(4, 5, 6), lines); // 1-based line numbers
    }

    @Test
    void parseStepLinesPerScenario_shouldHandleBackground() {
        String feature = """
                Feature: Example

                  Background:
                    Given database is seeded

                  Scenario: Foo
                    When user triggers action
                    Then result is visible
                """;

        var map = plugin.parseStepLinesPerScenario(feature);
        List<Integer> lines = map.get("Foo").get(0);
        assertEquals(List.of(4, 7, 8), lines);
    }

    @Test
    void parseStepLinesPerScenario_shouldHandleMultipleOccurrences() {
        String feature = """
                Feature: Multi

                  Scenario: Repeat
                    Given a thing

                  Scenario: Repeat
                    Given another thing
                """;

        Map<String, List<List<Integer>>> map = plugin.parseStepLinesPerScenario(feature);
        assertTrue(map.containsKey("Repeat"));
        assertEquals(2, map.get("Repeat").size());
        assertEquals(1, map.get("Repeat").get(0).size());
        assertEquals(1, map.get("Repeat").get(1).size());
    }

    @Test
    void parseStepLinesPerScenario_shouldIgnoreStepsInExamples() {
        String feature = """
                Feature: Outline test

                  Scenario Outline: Login
                    Given user <name> exists

                  Examples:
                    | name |
                    | admin |
                """;

        var map = plugin.parseStepLinesPerScenario(feature);
        List<Integer> lines = map.get("Login").get(0);
        assertEquals(List.of(4), lines);
    }

    @Test
    void parseStepLinesPerScenario_shouldIgnoreCommentsAndEmptyLines() {
        String feature = """
                # language: en
                Feature: Ignore

                  # Comment
                  Scenario: Commented
                    # Another comment
                    Given something
                """;

        var map = plugin.parseStepLinesPerScenario(feature);
        assertEquals(List.of(7), map.get("Commented").get(0));
    }

    @Test
    void parseStepLinesPerScenario_shouldReturnEmptyMapOnUnknownLanguage() {
        String feature = """
                # language: zz
                Feature: Unknown

                  Scenario: Test
                    Given something
                """;

        var map = plugin.parseStepLinesPerScenario(feature);
        assertTrue(map.isEmpty());
    }

}
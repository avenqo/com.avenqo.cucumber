package com.avenqo.cucumber.plugins.featureviewer;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GherkinHelper {

    public static Optional<GherkinDialect> extractDialect(String source) {
        String language = "en"; // Default

        for (String line : source.split("\r?\n")) {
            line = line.trim();
            if (line.startsWith("#") && line.contains("language:")) {
                language = line.substring(line.indexOf("language:") + 9).trim();
                break;
            }
        }

        return new GherkinDialectProvider().getDialect(language);
    }

    public static List<String> collectAllKeywords(Optional<GherkinDialect> maybeDialect) {
        return maybeDialect.map(dialect -> {
            List<String> all = new ArrayList<>();
            all.addAll(dialect.getFeatureKeywords());
            all.addAll(dialect.getBackgroundKeywords());
            all.addAll(dialect.getScenarioKeywords());
            all.addAll(dialect.getScenarioOutlineKeywords());
            all.addAll(dialect.getExamplesKeywords());
            all.addAll(dialect.getGivenKeywords());
            all.addAll(dialect.getWhenKeywords());
            all.addAll(dialect.getThenKeywords());
            all.addAll(dialect.getAndKeywords());
            all.addAll(dialect.getButKeywords());
            return all;
        }).orElse(Collections.emptyList());
    }


}


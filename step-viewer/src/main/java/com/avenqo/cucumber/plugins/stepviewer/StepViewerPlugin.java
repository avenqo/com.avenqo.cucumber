package com.avenqo.cucumber.plugins.stepviewer;

import java.util.Optional;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

public class StepViewerPlugin implements EventListener {

    public static boolean AUTO_SHOW_WINDOW = true;

    private boolean enabled;
    private long pauseTimeoutMs;

    private TestCase currentTestCase;
    private String featureText = "";

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        this.enabled = Boolean.parseBoolean(System.getProperty("bdd.overlay", "true"))
                && !GraphicsEnvironment.isHeadless();

        this.pauseTimeoutMs = Long.getLong("bdd.overlay.timeout.ms", 0L); // 0 = kein Timeout

        publisher.registerHandlerFor(TestRunStarted.class, e -> onRunStart());
        publisher.registerHandlerFor(TestCaseStarted.class, this::onCaseStart);
        publisher.registerHandlerFor(TestStepStarted.class, this::onStepStart);
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, e -> onRunFinished());

        publisher.registerHandlerFor(TestSourceRead.class, this::onSourceRead);
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    private void onRunStart() {
        if (enabled && AUTO_SHOW_WINDOW) {
            StepOverlay.get().showWindow();
        }
    }

    private void onRunFinished() {
        if (enabled && AUTO_SHOW_WINDOW) {
            StepOverlay.get().releaseIfWaiting();
            StepOverlay.get().dispose();
        }
    }

    private void onCaseStart(TestCaseStarted e) {
        if (!enabled) return;
        currentTestCase = e.getTestCase();
    }

    private void onSourceRead(TestSourceRead event) {
        String src = event.getSource();
        featureText = extractFeatureTitle(src);
    }

    private void onStepStart(TestStepStarted e) {
        if (!enabled) return;

        if (e.getTestStep() instanceof PickleStepTestStep step) {
            String keyword = step.getStep().getKeyword();
            String text = step.getStep().getText();
            String args = extractArguments(step);

            StepOverlay overlay = StepOverlay.get();
            overlay.resetCancel();

            String html = StepHelpRepository.get().findHtmlFor(step);

            overlay.setStep(
                    getFeatureFileName(),
                    featureText,
                    getScenarioName(),
                    keyword,
                    text,
                    args,
                    html
            );

            CountDownLatch latch = overlay.armPauseButton();
            try {
                if (pauseTimeoutMs > 0) {
                    latch.await(pauseTimeoutMs, TimeUnit.MILLISECONDS);
                } else {
                    latch.await();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            if (overlay.isCancelled()) {
                System.err.println("[Overlay] Szenario vom Benutzer abgebrochen.");
                throw new UserAbortException();
            } else if (overlay.isStopped()) {
                System.err.println("[Overlay] Testlauf vollständig beendet.");
                System.exit(1);
            }
        }
    }

    private void onStepFinished(TestStepFinished e) {
        if (!enabled) return;
        Status s = e.getResult().getStatus();
        if (s == Status.PASSED) {
            StepOverlay.get().setStatusPassed();
        } else if (s == Status.FAILED) {
            StepOverlay.get().setStatusFailed();
        }
        // andere Status bleiben neutral
    }

    // ---------------------------------------------------------------------
    // Argument-Extraktion
    // ---------------------------------------------------------------------

    private String extractArguments(PickleStepTestStep step) {
        StepArgument arg = step.getStep().getArgument();
        if (arg == null) return "";

        if (arg instanceof DocStringArgument ds) {
            String media;
            try {
                media = ds.getMediaType();
            } catch (NoSuchMethodError err) {
                try {
                    media = ds.getContentType();
                } catch (NoSuchMethodError err2) {
                    media = "";
                }
            }
            if (media == null) media = "";
            return (media.isEmpty() ? "" : (media + ":\n")) + ds.getContent();
        }

        if (arg instanceof DataTableArgument dt) {
            StringBuilder sb = new StringBuilder("DataTable:\n");
            for (var row : dt.cells()) {
                for (int i = 0; i < row.size(); i++) {
                    sb.append(row.get(i));
                    if (i < row.size() - 1) sb.append(" | ");
                }
                sb.append('\n');
            }
            return sb.toString();
        }
        return "";
    }

    // ---------------------------------------------------------------------
    // Feature/Scenario-Infos
    // ---------------------------------------------------------------------

    private String getFeatureFileName() {
        if (currentTestCase == null) return "";
        URI uri = currentTestCase.getUri();
        if (uri == null) return "(URI ist NULL)";

        String fileName = "(nicht ermittelt)";

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            Path path = Paths.get(uri);
            fileName = path.getFileName().toString();
        } else if ("classpath".equalsIgnoreCase(uri.getScheme())) {
            String resourcePath = uri.getSchemeSpecificPart();
            String[] s = resourcePath.split("/");
            fileName = s[s.length - 1];
        }

        return fileName;
    }

    private String getScenarioName() {
        return currentTestCase != null ? currentTestCase.getName() : "";
    }

    // ---------------------------------------------------------------------
    // Feature-Titel & Sprache
    // ---------------------------------------------------------------------

    private static final Pattern LANGUAGE_LINE =
            Pattern.compile("^\\s*#\\s*language\\s*:\\s*(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);

    private static String detectLanguage(String src) {
        if (src == null) {
            return "en";
        }
        String[] lines = src.split("\\R");

        for (int i = 0; i < Math.min(lines.length, 10); i++) {
            Matcher m = LANGUAGE_LINE.matcher(lines[i]);
            if (m.find()) {
                return m.group(1);  // z.B. "de", "en", "fr"
            }
        }
        return "en";
    }
    private static String extractFeatureTitle(String src) {
        if (src == null) {
            return "(kein Feature)";
        }

        String language = detectLanguage(src);

        GherkinDialectProvider provider = new GherkinDialectProvider();

        // NEU: Optional-API verwenden
        Optional<GherkinDialect> dialectOpt = provider.getDialect(language);
        GherkinDialect dialect = dialectOpt.orElseGet(provider::getDefaultDialect);

        // z.B. ["Feature", "Funktionalität", ...]
        List<String> featureKeywords = dialect.getFeatureKeywords();

        String[] lines = src.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            // Sobald ein Szenario beginnt, hören wir auf zu suchen
            if (trimmed.startsWith("Scenario:")
                    || trimmed.startsWith("Szenario:")
                    || trimmed.startsWith("Scenario Outline:")
                    || trimmed.startsWith("Szenariogrundriss:")) {
                break;
            }

            for (String kw : featureKeywords) {
                String kwColon   = kw + ":";   // "Feature:"
                String kwColonSp = kw + " :";  // "Feature :"

                if (trimmed.startsWith(kwColon) || trimmed.startsWith(kwColonSp)) {
                    int idx = trimmed.indexOf(':');
                    if (idx >= 0 && idx < trimmed.length() - 1) {
                        return trimmed.substring(idx + 1).trim();
                    } else {
                        return "(leerer Feature-Titel)";
                    }
                }
            }
        }

        return "(kein Feature)";
    }
}
package com.avenqo.cucumber.plugins.featureviewer;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FeatureViewerPlugin implements EventListener {

    public static boolean AUTO_SHOW_WINDOW = true;

    // uri -> (scenarioName -> List of finding; jede Vorkommnis ist Liste von Step-Zeilennummern)
    private final Map<String, Map<String, List<List<Integer>>>> stepLinesByUri = new ConcurrentHashMap<>();

    // Laufzeit-State je TestCase
    private final Map<String, String>  uriByCase          = new HashMap<>();
    private final Map<String, String>  scenarioKeyByCase  = new HashMap<>();
    private final Map<String, Integer> occurrenceByCase   = new HashMap<>();
    private final Map<String, Integer> stepIndexByCase    = new HashMap<>();

    // Zählt, wie oft wir pro (uri, scenarioName) bereits eine Vorkommnis verwendet haben
    private final Map<String, Map<String, Integer>> usedOccurrenceByUriAndScenario = new HashMap<>();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, e -> {
            if (AUTO_SHOW_WINDOW) {
                FeatureViewer.get().showWindow();
            }
        });

        publisher.registerHandlerFor(TestSourceRead.class,   this::onSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class,  this::onCaseStart);
        publisher.registerHandlerFor(TestStepStarted.class,  this::onStepStart);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onCaseFinished);
    }

    private void onSourceRead(TestSourceRead e) {
        String uri = e.getUri().toString();
        String source = e.getSource();

        // Viewer aktualisieren
        FeatureViewer.get().loadSource(uri, source);

        // Parser: baue Map<SzenarioName, List<Vorkommnis(=Stepzeilen)>>
        stepLinesByUri.put(uri, parseStepLinesPerScenario(source));
    }

    private void onCaseStart(TestCaseStarted e) {
        TestCase tc = e.getTestCase();
        String uri  = tc.getUri().toString();
        String name = tc.getName();
        String caseKey = caseKey(tc);

        uriByCase.put(caseKey, uri);
        stepIndexByCase.put(caseKey, 0);

        Map<String, List<List<Integer>>> perScenario = stepLinesByUri.get(uri);
        if (perScenario == null || perScenario.isEmpty()) return;

        // exakte Übereinstimmung?
        if (perScenario.containsKey(name)) {
            int occ = nextOccurrenceIndex(uri, name, perScenario.get(name));
            scenarioKeyByCase.put(caseKey, name);
            occurrenceByCase.put(caseKey, occ);
        } else {
            // Prefix-Match (Outline)
            String best = perScenario.keySet().stream()
                    .filter(n -> name.startsWith(n))
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .findFirst().orElse(null);
            if (best == null) {
                String any = perScenario.keySet().iterator().next();
                int occ = nextOccurrenceIndex(uri, any, perScenario.get(any));
                scenarioKeyByCase.put(caseKey, any);
                occurrenceByCase.put(caseKey, occ);
            } else {
                int occ = nextOccurrenceIndex(uri, best, perScenario.get(best));
                scenarioKeyByCase.put(caseKey, best);
                occurrenceByCase.put(caseKey, occ);
            }
        }

        // >>> HIER: erste Zeile sofort markieren (vor dem 1. Step)
        String scenKey = scenarioKeyByCase.get(caseKey);
        Integer occ = occurrenceByCase.get(caseKey);
        if (scenKey != null && occ != null) {
            List<List<Integer>> occs = perScenario.get(scenKey);
            if (occs != null && !occs.isEmpty()) {
                List<Integer> lines = occs.get(Math.min(occ, occs.size()-1));
                if (lines != null && !lines.isEmpty()) {
                    // Synchron anzeigen, damit man es VOR dem ersten Step sieht
                    FeatureViewer.get().highlightLineNow(uri, lines.get(0));
                    // Optional: wenn du Doppelmarkierung vermeiden willst, hier schon idx=1 setzen:
                    // stepIndexByCase.put(caseKey, 1);
                }
            }
        }
    }


    private void onStepStart(TestStepStarted e) {
        if (!(e.getTestStep() instanceof PickleStepTestStep)) return;

        TestCase tc = e.getTestCase();
        String caseKey = caseKey(tc);

        String uri = uriByCase.get(caseKey);
        String scenarioKey = scenarioKeyByCase.get(caseKey);
        Integer occ = occurrenceByCase.get(caseKey);
        Integer idx = stepIndexByCase.get(caseKey);

        if (uri == null || scenarioKey == null || occ == null || idx == null) return;

        Map<String, List<List<Integer>>> perScenario = stepLinesByUri.get(uri);
        if (perScenario == null) return;

        List<List<Integer>> occurrences = perScenario.get(scenarioKey);
        if (occurrences == null || occurrences.isEmpty()) return;

        // Wenn es weniger Vorkommnisse gibt als nötig, clamp auf 0
        if (occ >= occurrences.size()) occ = 0;

        List<Integer> lines = occurrences.get(occ);
        if (lines == null || lines.isEmpty()) return;

        int i = Math.min(idx, lines.size() - 1);
        int line1 = lines.get(i);

        // im Viewer markieren
        FeatureViewer.get().highlightLineNow(uri, line1);

        // Index hochzählen (nur für echte Steps, Hooks ignorieren wir bereits)
        stepIndexByCase.put(caseKey, idx + 1);
    }

    private void onCaseFinished(TestCaseFinished e) {
        String k = caseKey(e.getTestCase());
        uriByCase.remove(k);
        scenarioKeyByCase.remove(k);
        occurrenceByCase.remove(k);
        stepIndexByCase.remove(k);
    }

    private String caseKey(TestCase tc) {
        // ausreichend eindeutig für denselben Lauf
        return tc.getUri() + "::" + tc.getName();
    }

    // ==== Parser ===================================================================


    /** Liefert Map<SzenarioName,
     * Liste von Vorkommnissen; jede ist Liste der Step-Zeilen (1-basiert)>
     **/
     Map<String, List<List<Integer>>> parseStepLinesPerScenario(String source) {
        Map<String, List<List<Integer>>> result = new LinkedHashMap<>();
        String[] lines = source.split("\\R", -1);

        String language = "en";
        for (String line : lines) {
            if (line.startsWith("#") && line.contains("language:")) {
                int idx = line.indexOf("language:");
                language = line.substring(idx + 9).trim();
                break;
            }
        }
        Optional<GherkinDialect> optionalDialect = new GherkinDialectProvider().getDialect(language);
        if (optionalDialect.isEmpty()) return result;
        GherkinDialect dialect = optionalDialect.get();
        Set<String> stepKeywords = dialect.getGivenKeywords().stream()
                .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
        stepKeywords.addAll(dialect.getWhenKeywords().stream().map(String::trim).map(String::toLowerCase).toList());
        stepKeywords.addAll(dialect.getThenKeywords().stream().map(String::trim).map(String::toLowerCase).toList());
        stepKeywords.addAll(dialect.getAndKeywords().stream().map(String::trim).map(String::toLowerCase).toList());
        stepKeywords.addAll(dialect.getButKeywords().stream().map(String::trim).map(String::toLowerCase).toList());

        List<Integer> backgroundSteps = new ArrayList<>();
        List<Integer> currentScenarioSteps = null;
        boolean inExamples = false;
        boolean inBackground = false;

        Pattern SCENARIO_HDR = Pattern.compile(
            "^\\s*(?:Scenario Outline|Scenario|Szenario|Szenariogrundriss|Szenario-Grundriss)\\s*:\\s*(.+)\\s*$",
            Pattern.CASE_INSENSITIVE);
        Pattern BACKGROUND_HDR = Pattern.compile(
            "^\\s*(?:Background|Grundlage)\\s*:\\s*(.*)$",
            Pattern.CASE_INSENSITIVE);
        Pattern EXAMPLES_HDR = Pattern.compile(
            "^\\s*(?:Examples|Beispiele)\\s*:\\s*$",
            Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < lines.length; i++) {

            String raw = lines[i];
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;

            // Header?
            if (BACKGROUND_HDR.matcher(raw).find()) {
                inBackground = true;
                currentScenarioSteps = null;
                inExamples = false;
                backgroundSteps.clear(); // jedes Background-Block überschreibt zuvor (gemäß Gherkin)
                continue;
            }

            var scenM = SCENARIO_HDR.matcher(raw);
            if (scenM.find()) {
                inBackground = false;
                inExamples = false;
                String currentScenario = scenM.group(1).trim();
                currentScenarioSteps = new ArrayList<>(backgroundSteps); // Background voranstellen
                result.computeIfAbsent(currentScenario, k -> new ArrayList<>()).add(currentScenarioSteps);
                continue;
            }

            if (EXAMPLES_HDR.matcher(raw).find()) {
                inExamples = true; // Nach Examples keine Steps mehr sammeln
                continue;
            }

            // Step-Zeile?
            if (!inExamples) {
                String firstWord = firstWordLower(t);
                if (stepKeywords.contains(firstWord) && hasStepText(t)) {
                    int line1 = i + 1; // 1-basiert
                    if (inBackground) {
                        backgroundSteps.add(line1);
                    } else if (currentScenarioSteps != null) {
                        currentScenarioSteps.add(line1);
                    }
                }
            }
        }
        return result;
    }

    private static String firstWordLower(String s) {
        int sp = s.indexOf(' ');
        String w = (sp > 0 ? s.substring(0, sp) : s);
        return w.toLowerCase(Locale.ROOT);
    }

    private static boolean hasStepText(String trimmedLine) {
        // Ein Step braucht hinter dem Keyword noch Text
        int sp = trimmedLine.indexOf(' ');
        return sp > 0 && trimmedLine.length() > sp + 1;
    }

    // ==== Occurrence-Verwaltung =====================================================

    private int nextOccurrenceIndex(String uri, String scenarioName, List<List<Integer>> occurrences) {
        Map<String, Integer> used = usedOccurrenceByUriAndScenario.computeIfAbsent(uri, u -> new HashMap<>());
        int next = used.getOrDefault(scenarioName, 0);
        if (next >= occurrences.size()) next = 0; // falls mehr Instanzen als Duplikate im File
        used.put(scenarioName, next + 1);
        return next;
    }

}

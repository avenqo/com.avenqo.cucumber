package com.avenqo.cucumber.plugins.stepviewer;

import io.cucumber.plugin.event.PickleStepTestStep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;



/**
 * Lädt HTML-Hilfen aus /stephelp auf dem Klassenpfad.
 * Matching-Strategie:
 *  1) EXAKT:  key = "Given: I click the login button"  (keyword + ": " + text)
 *  2) EXAKT:  key = "I click the login button"         (nur text)
 *  3) NORMALISIERT (lowercase, multiple spaces -> single)
 *  4) LONGEST PREFIX MATCH (für längere Texte mit Params)
 *  5) REGEX:  key beginnt mit "regex:" (z.B. regex:^.*login.*$)
 *
 * index.cfg mappt keys -> html resource path (z.B. "stephelp/login.html")
 */
public final class StepHelpRepository {

    // Neue Variante: Pfad relativ im Classpath
    private static final String INDEX_CLASSPATH_PATH = "stephelp/index.cfg";

    private static volatile StepHelpRepository INSTANCE;

    private final Map<String, String> exactMap = new HashMap<>();
    private final Map<String, String> exactMapNorm = new HashMap<>();
    private final List<Map.Entry<String, String>> prefixEntries = new ArrayList<>();
    private final List<Map.Entry<Pattern, String>> regexEntries = new ArrayList<>();

    private final Map<String, String> htmlCache = new ConcurrentHashMap<>();

    private StepHelpRepository() {
        Map<String, String> entries = loadIndexUtf8();

        // Debug-Ausgabe der Keys
        System.out.println("--- Keys:");
        for (var e : entries.entrySet()) {
            System.out.println("'" + e.getKey() + "'");
        }
        System.out.println("--- Keys Ende ---");

        for (var e : entries.entrySet()) {
            String rawKey = e.getKey();
            String target = e.getValue().trim();

            if (rawKey.startsWith("regex:")) {
                String re = rawKey.substring("regex:".length()).trim();
                try {
                    regexEntries.add(Map.entry(Pattern.compile(re, Pattern.CASE_INSENSITIVE), target));
                } catch (Exception ex) {
                    System.err.println("[StepHelpRepository] Ungültiges Regex: " + re + " -> " + ex);
                }
            } else {
                String k = rawKey.trim(); // nur Rand-Whitespace entfernen
                exactMap.put(k, target);
                exactMapNorm.put(normalize(k), target);
                prefixEntries.add(Map.entry(k, target));
            }
        }

        // Longest prefix zuerst
        prefixEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
    }

    /**
     * Neue Implementierung: nutzt zuerst den Thread-Context-ClassLoader (der die Tests + BDD-Module sieht),
     * danach Fallback auf den Klassenlader dieser Klasse.
     */
    private static Map<String, String> loadIndexUtf8() {
        // 1. Bevorzugt: Thread-Context-ClassLoader (sieht i.d.R. alle Test-Module)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = StepHelpRepository.class.getClassLoader();
        }

        InputStream in = cl.getResourceAsStream(INDEX_CLASSPATH_PATH);

        // 2. Fallback: bisherige Variante (ClassLoader des Repository selbst)
        if (in == null) {
            in = StepHelpRepository.class.getResourceAsStream("/" + INDEX_CLASSPATH_PATH);
        }

        if (in == null) {
            System.err.println("[StepHelpRepository] index.cfg nicht gefunden im Classpath unter " + INDEX_CLASSPATH_PATH);
            return Collections.emptyMap();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Map<String, String> result = loadIndexFromResource(reader);
            System.out.println("[StepHelpRepository] index.cfg geladen; Einträge: " + result.size());
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Fehler beim Laden von " + INDEX_CLASSPATH_PATH, e);
        }
    }

    /**
     * Parst index.cfg Zeile für Zeile:
     * - Leere Zeilen und Kommentare (#...) werden ignoriert
     * - Zeilen ohne '=' werden geloggt und ignoriert
     * - key = value wird getrimmt
     */
    private static Map<String, String> loadIndexFromResource(BufferedReader reader) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int idx = trimmed.indexOf('=');
            if (idx < 0) {
                System.err.println("[StepHelpRepository] Zeile ohne '=' ignoriert: " + trimmed);
                continue;
            }
            String key = trimmed.substring(0, idx).trim();
            String value = trimmed.substring(idx + 1).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }

    public static StepHelpRepository get() {
        StepHelpRepository inst = INSTANCE;
        if (inst == null) {
            synchronized (StepHelpRepository.class) {
                if (INSTANCE == null) INSTANCE = new StepHelpRepository();
                inst = INSTANCE;
            }
        }
        return inst;
    }

    /**
     * Delivers
     * @param keyword i.e. When, Given, ...
     * @param text the step text without key word, i.e. "I order a cappuccino"
     * @return HTML page, fragment or null
     */
    public String findHtmlFor(String keyword, String text) {
        if (text == null) text = "";
        if (keyword == null) keyword = "";
        String key1 = keyword.trim() + ": " + text.trim();
        String key2 = text.trim();

        // 1) exakt (keyword + text)
        String target = exactMap.get(key1);
        if (target != null) return loadHtml(target);

        // 2) exakt (nur text)
        target = exactMap.get(key2);
        if (target != null) return loadHtml(target);

        // 3) normalisiert
        String n1 = normalize(key1);
        target = exactMapNorm.get(n1);
        if (target != null) return loadHtml(target);

        String n2 = normalize(key2);
        target = exactMapNorm.get(n2);
        if (target != null) return loadHtml(target);

        // 4) Longest prefix (auf Original-Keys)
        for (var e : prefixEntries) {
            if (startsWithIgnoreCase(key1, e.getKey()) || startsWithIgnoreCase(key2, e.getKey())) {
                return loadHtml(e.getValue());
            }
        }

        // 5) Regex
        for (var e : regexEntries) {
            if (e.getKey().matcher(key1).find() || e.getKey().matcher(key2).find()) {
                return loadHtml(e.getValue());
            }
        }

        return null;
    }

    public String findHtmlFor(PickleStepTestStep pstep) {
       var step = pstep.getStep();
        String kw = step.getKeyword();
        String tx = step.getText();
        String pattern = pstep.getPattern();
        int line = pstep.getStep().getLine();
        var argument = step.getArgument();

        return findHtmlFor(kw, tx);
    }

    private static boolean startsWithIgnoreCase(String s, String prefix) {
        if (s.length() < prefix.length()) return false;
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static String normalize(String s) {
        String out = s.toLowerCase(Locale.ROOT).trim();
        out = out.replaceAll("\\s+", " ");
        return out;
    }
    private String loadHtml(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return null;

        return htmlCache.computeIfAbsent(resourcePath, rp -> {
            String rpNorm = rp.trim();

            // Wenn kein Slash enthalten ist, nehmen wir an, die Datei liegt unter "stephelp/"
            if (!rpNorm.contains("/")) {
                rpNorm = "stephelp/" + rpNorm;
            }

            String rp2 = rpNorm.startsWith("/") ? rpNorm : ("/" + rpNorm);

            try (InputStream in = getClass().getResourceAsStream(rp2)) {
                if (in == null) {
                    System.err.println("[StepHelpRepository] Resource nicht gefunden: " + rp2);
                    return null;
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    return sb.toString();
                }
            } catch (Exception e) {
                System.err.println("[StepHelpRepository] Fehler beim Laden von " + rp2 + ": " + e);
                return null;
            }
        });
    }

}

package com.avenqo.cucumber.plugins.stepviewer;

import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepHelpRepositoryTest {

    @Test
    void get_returnsSameSingletonInstance() {
        StepHelpRepository inst1 = StepHelpRepository.get();
        StepHelpRepository inst2 = StepHelpRepository.get();

        assertSame(inst1, inst2, "get() sollte immer dieselbe Instanz liefern");
    }

    @Test
    void findHtmlFor_exact_keyword_and_text() {
        StepHelpRepository repo = StepHelpRepository.get();

        String html = repo.findHtmlFor("Given", "I click the login button");

        assertNotNull(html, "HTML sollte für exakt passenden Keyword+Text gefunden werden");
        assertTrue(html.contains("EXACT"), "login_exact.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_exact_text_only() {
        StepHelpRepository repo = StepHelpRepository.get();

        String html = repo.findHtmlFor("", "I click the login button");

        assertNotNull(html, "HTML sollte für exakt passenden Text gefunden werden");
        assertTrue(html.contains("TEXT_ONLY"), "login_text_only.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_normalized_match() {
        StepHelpRepository repo = StepHelpRepository.get();

        // index.cfg enthält:
        // Given: I   CLICK   the   LOGIN   button = stephelp/login_normalized.html
        String html = repo.findHtmlFor("Given", "I   CLICK   the   LOGIN   button");

        assertNotNull(html, "HTML sollte über normalisiertes Matching gefunden werden");
        assertTrue(html.contains("NORMALIZED"), "login_normalized.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_longest_prefix_match() {
        StepHelpRepository repo = StepHelpRepository.get();

        // Startet mit "Given: I click", aber NICHT mit "Given: I click the login button"
        String html = repo.findHtmlFor("Given", "I click something else");

        assertNotNull(html, "HTML sollte über Longest-Prefix-Match gefunden werden");
        assertTrue(html.contains("PREFIX"), "login_prefix.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_longest_prefix_match_shorterPrefixWins() {
        StepHelpRepository repo = StepHelpRepository.get();

        // Startet mit "Given: I click", aber NICHT mit "Given: I click the login button"
        String html = repo.findHtmlFor("Given", "I click something else");

        assertNotNull(html, "HTML sollte über Longest-Prefix-Match gefunden werden");
        assertTrue(html.contains("PREFIX"), "login_prefix.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_regex_match() {
        StepHelpRepository repo = StepHelpRepository.get();

        // index.cfg enthält:
        // regex:^.*logout.*$ = stephelp/logout_regex.html
        String html = repo.findHtmlFor("Given", "I logout from the application");

        assertNotNull(html, "HTML sollte über Regex-Matching gefunden werden");
        assertTrue(html.contains("REGEX"), "logout_regex.html sollte geladen werden");
    }

    @Test
    void findHtmlFor_returnsNull_whenNothingMatches() {
        StepHelpRepository repo = StepHelpRepository.get();

        String html = repo.findHtmlFor("UnknownKeyword", "Some completely unknown text");

        assertNull(html, "Wenn nichts matched, sollte null zurückgegeben werden");
    }

    @Test
    void findHtmlFor_handlesNullAndEmptyInputs() {
        StepHelpRepository repo = StepHelpRepository.get();

        assertNull(repo.findHtmlFor(null, null),
                "null/null sollte kein Ergebnis liefern");
        assertNull(repo.findHtmlFor("", null),
                "leeres Keyword + null Text sollte kein Ergebnis liefern");
        assertNull(repo.findHtmlFor(null, ""),
                "null Keyword + leerer Text sollte kein Ergebnis liefern");
    }

    @Test
    void findHtmlFor_PickleStepTestStep_delegatesToStringBasedMethod() {
        StepHelpRepository repo = StepHelpRepository.get();

        // PickleStepTestStep und das darin enthaltene Gherkin-Step-Objekt mocken
        PickleStepTestStep step = mock(PickleStepTestStep.class);
        Step gherkinStep = mock(Step.class);

        when(step.getStep()).thenReturn(gherkinStep);
        when(gherkinStep.getKeyword()).thenReturn("Given");
        when(gherkinStep.getText()).thenReturn("I click the login button");

        String html = repo.findHtmlFor(step);

        assertNotNull(html, "HTML sollte für den PickleStepTestStep gefunden werden");
        assertTrue(html.contains("EXACT"),
                "login_exact.html sollte über die Step-Variante geladen werden");
    }

    @Test
    void loadIndex_ignoresLinesWithoutEquals() {
        StepHelpRepository repo = StepHelpRepository.get();

        // In index.cfg steht "noEqualsLine" ohne '='.
        String html = repo.findHtmlFor("", "noEqualsLine");

        assertNull(html, "Zeilen ohne '=' in index.cfg sollten ignoriert werden");
    }
}


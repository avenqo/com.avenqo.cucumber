package com.avenqo.cucumber.plugins.featureviewer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class FeatureViewerTest {

    private FeatureViewer viewer;

    @BeforeEach
    void setUp() {
        viewer = FeatureViewer.get();
    }

    @Test
    void testLoadSourceAppliesKeywordStyle() throws Exception {
        String feature = """
                # language: de
                Funktionalität: Test
                  Szenario: Einfacher Test
                    Gegeben sei ein Zustand
                    Wenn eine Aktion erfolgt
                    Dann ist das Ergebnis sichtbar
                """;

        viewer.loadSource("test.feature", feature);

        Thread.sleep(200); // Warte auf EDT

        JScrollPane scrollPane = (JScrollPane) viewer.getComponent();
        JViewport viewport = scrollPane.getViewport();
        JTextPane textPane = (JTextPane) viewport.getView();
        StyledDocument doc = (StyledDocument) textPane.getDocument();



                String text = doc.getText(0, doc.getLength());
        assertTrue(text.contains("Gegeben "));

        int keywordOffset = text.indexOf("Gegeben ");
        AttributeSet attrs = doc.getCharacterElement(keywordOffset).getAttributes();

        Color expectedColor = new Color(0x1F618D);
        assertEquals(expectedColor, StyleConstants.getForeground(attrs));
        assertTrue(StyleConstants.isBold(attrs));
    }

    @Test
    void testHighlightLine() throws Exception {
        String source = "Feature: Test\nScenario: One\nGiven something";
        viewer.loadSource("test.feature", source);

        viewer.highlightLineNow("test.feature", 2);
        Thread.sleep(200); // Warte auf EDT


        JScrollPane scrollPane = (JScrollPane) viewer.getComponent();
        JViewport viewport = scrollPane.getViewport();
        JTextPane textPane = (JTextPane) viewport.getView();

        Highlighter.Highlight[] highlights = textPane.getHighlighter().getHighlights();

        assertEquals(1, highlights.length);
        assertTrue(highlights[0].getStartOffset() < highlights[0].getEndOffset());
    }

    @Test
    void testSingletonInstance() {
        FeatureViewer a = FeatureViewer.get();
        FeatureViewer b = FeatureViewer.get();
        assertSame(a, b);
    }
}
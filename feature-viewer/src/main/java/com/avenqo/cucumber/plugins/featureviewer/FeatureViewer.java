package com.avenqo.cucumber.plugins.featureviewer;

import io.cucumber.gherkin.GherkinDialect;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class FeatureViewer {

    private static FeatureViewer INSTANCE;

    private final JFrame frame;
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final LineNumberGutter gutter;

    private final Map<String, List<String>> sourceByUri = new ConcurrentHashMap<>();
    private Object currentHighlightTag;

    private String currentUri;
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 128);
    private final Highlighter.HighlightPainter linePainter =
            new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT_COLOR);

    private List<String> bddKeywords;

    private FeatureViewer() {
        frame = new JFrame("Cucumber Feature Viewer");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(false);
        frame.setResizable(true);

        // --------------------------
        // JTextPane statt JTextArea!
        // --------------------------
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textPane.setMargin(new Insets(6, 6, 6, 6));
        textPane.setHighlighter(new DefaultHighlighter());

        scrollPane = new JScrollPane(textPane);
        gutter = new LineNumberGutter(textPane);
        scrollPane.setRowHeaderView(gutter);

        frame.setContentPane(scrollPane);
        frame.setSize(900, 700);
        frame.setLocation(680, 660);
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public static synchronized FeatureViewer get() {
        if (INSTANCE == null) INSTANCE = new FeatureViewer();
        return INSTANCE;
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.toFront();
        });
    }

    // ----------------------------------------------------------
    // Source laden
    // ----------------------------------------------------------
    public void loadSource(String uri, String source) {
        List<String> lines = Arrays.asList(source.split("\r?\n", -1));
        sourceByUri.put(uri, lines);

        Optional<GherkinDialect> dialect = GherkinHelper.extractDialect(source);
        bddKeywords = GherkinHelper.collectAllKeywords(dialect);

        SwingUtilities.invokeLater(() -> {
            textPane.setText(source);
            highlightKeywordsStyled(bddKeywords);
            textPane.setCaretPosition(0);
            clearHighlight();
            gutter.refreshLineCount();
        });
    }

    // ----------------------------------------------------------
    // Zeilenhighlight
    // ----------------------------------------------------------
    public void highlightLineNow(String uri, int lineNumber1Based) {
        Runnable r = () -> doHighlight(uri, lineNumber1Based);
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else try { SwingUtilities.invokeAndWait(r); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void doHighlight(String uri, int lineNumber1Based) {
        List<String> lines = sourceByUri.get(uri);
        if (lines == null || lineNumber1Based <= 0) return;

        SwingUtilities.invokeLater(() -> {
            try {
                if (currentUri == null || !currentUri.equals(uri)) {
                    textPane.setText(String.join("\n", lines));
                    highlightKeywordsStyled(bddKeywords);
                    currentUri = uri;
                    gutter.refreshLineCount();
                }

                int maxIdx = Math.max(0, textPane.getDocument().getDefaultRootElement().getElementCount() - 1);
                int lineIdx = Math.max(0, Math.min(lineNumber1Based - 1, maxIdx));

                Element root = textPane.getDocument().getDefaultRootElement();
                int start = root.getElement(lineIdx).getStartOffset();
                int end = root.getElement(lineIdx).getEndOffset();

                Highlighter h = textPane.getHighlighter();
                h.removeAllHighlights();
                currentHighlightTag = h.addHighlight(start, end, linePainter);

                highlightKeywordsStyled(bddKeywords);

                textPane.setCaretPosition(start);
                Rectangle r = textPane.modelToView(start);
                if (r != null) textPane.scrollRectToVisible(r);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void clearHighlight() {
        if (currentHighlightTag != null) {
            textPane.getHighlighter().removeHighlight(currentHighlightTag);
            currentHighlightTag = null;
        }
    }

    // ----------------------------------------------------------
    // Keyword-Hervorhebung via StyledDocument
    // ----------------------------------------------------------
    private void highlightKeywordsStyled(List<String> keywords) {
        StyledDocument doc = textPane.getStyledDocument();
        String content = textPane.getText();

        // clear all styles
        doc.setCharacterAttributes(0, content.length(),
                textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

        // Style für Keyword
        SimpleAttributeSet kwStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(kwStyle, new Color(0x1F618D));
        StyleConstants.setBold(kwStyle, true);

        for (String keyword : keywords) {

            String pattern = "(?m)^\\s*" + Pattern.quote(keyword);

            Matcher matcher = Pattern.compile(pattern).matcher(content);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                doc.setCharacterAttributes(start, end - start, kwStyle, false);
            }
        }
    }


    // ----------------------------------------------------------
    // LineNumberGutter aktualisiert für JTextPane
    // ----------------------------------------------------------
    static final class LineNumberGutter extends JComponent {
        private final JTextPane pane;
        private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        LineNumberGutter(JTextPane pane) {
            this.pane = pane;
            setFont(font);
            setForeground(new Color(120,120,120));
            setBackground(new Color(245,245,245));
            setOpaque(true);
        }

        void refreshLineCount() {
            revalidate();
            repaint();
        }

        @Override public Dimension getPreferredSize() {
            int lines = pane.getDocument().getDefaultRootElement().getElementCount();
            String digits = String.valueOf(lines);
            int width = getFontMetrics(font).stringWidth(digits) + 12;
            return new Dimension(width, pane.getHeight());
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Rectangle clip = g.getClipBounds();
            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

            g.setColor(getForeground());
            int lineHeight = pane.getFontMetrics(pane.getFont()).getHeight();

            Element root = pane.getDocument().getDefaultRootElement();
            int lines = root.getElementCount();

            int y = lineHeight;

            for (int i = 0; i < lines; i++) {
                String s = String.valueOf(i + 1);

                int x = getPreferredSize().width -
                        6 - g.getFontMetrics().stringWidth(s);

                g.drawString(s, x, y);
                y += lineHeight;
            }
        }
    }
}
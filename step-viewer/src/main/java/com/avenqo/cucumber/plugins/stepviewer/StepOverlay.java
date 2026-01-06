package com.avenqo.cucumber.plugins.stepviewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public final class StepOverlay {
    private static StepOverlay INSTANCE;

    private final JFrame frame;

    private final JLabel statusDot;
    private final JLabel stepLabel;
    private final JLabel breadcrumbLabel;

    private final JTextArea argArea;
    private final JEditorPane htmlPane;
    private final JSplitPane centerSplit;

    private final JButton nextStepBtn;
    private final JButton skipScenarioBtn;
    private final JButton stopTestBtn;

    private final JPanel root;

    private volatile boolean cancelled = false;
    private volatile boolean stopped = false;

    private URL baseUrl = null;

    // Aktuelles Latch für den "Weiter"-Button
    private final AtomicReference<CountDownLatch> activeLatch = new AtomicReference<>();

    private StepOverlay() {

        // Basis-URL für relative Ressourcen im HTML (Bilder etc.)
        URL pageUrl = getClass().getResource("/stephelp/index.cfg");
        try {
            baseUrl = pageUrl != null ? new URL(pageUrl, ".") : null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        frame = new JFrame("BDD Run Viewer");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(false);
        frame.setType(Window.Type.UTILITY);
        frame.setResizable(true);

        // --- Kopfzeile: Breadcrumb + Status / Step ------------------------

        statusDot = new JLabel("●");
        statusDot.setFont(statusDot.getFont().deriveFont(20f));
        statusDot.setForeground(new Color(0x95A5A6)); // neutral

        stepLabel = new JLabel("—");
        stepLabel.setFont(stepLabel.getFont().deriveFont(Font.BOLD, 14f));
        stepLabel.setOpaque(true);
        stepLabel.setBackground(new Color(0xF4F6F7));
        stepLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        breadcrumbLabel = new JLabel("—");
        breadcrumbLabel.setFont(breadcrumbLabel.getFont().deriveFont(Font.PLAIN, 11f));
        breadcrumbLabel.setForeground(new Color(0x7F8C8D));

        JPanel headerLeft = new JPanel(new BorderLayout(4, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(breadcrumbLabel, BorderLayout.NORTH);
        headerLeft.add(stepLabel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout(12, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(statusDot, BorderLayout.WEST);
        headerPanel.add(headerLeft, BorderLayout.CENTER);

        // --- Mitte: HTML (oben) + Parameter (unten) -----------------------

        // HTML-Beschreibung in Card
        htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setText(defaultNoDescriptionHtml());
        htmlPane.addHyperlinkListener(ev -> {
            if (ev.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(ev.getURL().toURI());
                } catch (Exception ignore) {
                }
            }
        });

        JScrollPane htmlScroll = new JScrollPane(
                htmlPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        htmlScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel htmlCard = new JPanel(new BorderLayout());
        htmlCard.setBorder(createCardBorder("Step-Dokumentation"));
        htmlCard.add(htmlScroll, BorderLayout.CENTER);

        // Parameter-Bereich
        argArea = new JTextArea(5, 40);
        argArea.setEditable(false);
        argArea.setLineWrap(true);
        argArea.setWrapStyleWord(true);
        argArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane argScroll = new JScrollPane(
                argArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        argScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel argCard = new JPanel(new BorderLayout());
        argCard.setBorder(createCardBorder("Step-Parameter & DataTable"));
        argCard.add(argScroll, BorderLayout.CENTER);

        // Split: oben HTML (ca. 75%), unten Parameter (ca. 25%)
        centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, htmlCard, argCard);
        centerSplit.setResizeWeight(0.75);
        centerSplit.setContinuousLayout(true);
        centerSplit.setOneTouchExpandable(true);
        centerSplit.setBorder(BorderFactory.createEmptyBorder());

        // --- Button-Leiste unten ------------------------------------------
        // Buttons: reine Icons (32x32), einheitliche Button-Größe 36x36

        nextStepBtn = makeButton("Nächster Schritt", "icon-next-32.png");
        nextStepBtn.setEnabled(false);
        nextStepBtn.addActionListener(e -> {
            CountDownLatch latch = activeLatch.getAndSet(null);
            if (latch != null) latch.countDown();
            nextStepBtn.setEnabled(false);
        });

        skipScenarioBtn = makeButton("Szenario überspringen", "icon_skip_32.png");
        skipScenarioBtn.addActionListener(e -> {
            cancelled = true;
            CountDownLatch latch = activeLatch.getAndSet(null);
            if (latch != null) latch.countDown();
            nextStepBtn.setEnabled(false);
            skipScenarioBtn.setEnabled(false);
        });

        stopTestBtn = makeButton("Stop Test", "icon-abort-32.png");
        stopTestBtn.addActionListener(e -> {
            stopped = true;
            CountDownLatch latch = activeLatch.getAndSet(null);
            if (latch != null) latch.countDown();
            nextStepBtn.setEnabled(false);
            stopTestBtn.setEnabled(false);
            skipScenarioBtn.setEnabled(false);
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottom.add(stopTestBtn);
        bottom.add(skipScenarioBtn);
        bottom.add(nextStepBtn);

        // --- Root-Panel ----------------------------------------------------

        root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.setBackground(new Color(0xF8F9F9));

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(centerSplit, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setSize(950, 600);
        frame.setLocation(40, 40);

        SwingUtilities.invokeLater(() -> centerSplit.setDividerLocation(0.55));
    }

    // ---------------------------------------------------------------------
    // Singleton
    // ---------------------------------------------------------------------

    public static synchronized StepOverlay get() {
        if (INSTANCE == null) INSTANCE = new StepOverlay();
        return INSTANCE;
    }

    // ---------------------------------------------------------------------
    // Public API (kompatibel)
    // ---------------------------------------------------------------------

    public void showWindow() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public void dispose() {
        SwingUtilities.invokeLater(frame::dispose);
    }

    public void setStep(String fileName, String feature, String scenario,
                        String keyword, String text, String argsPreview) {
        setStep(fileName, feature, scenario, keyword, text, argsPreview, null);
    }

    public void setStep(String fileName, String feature, String scenario,
                        String keyword, String text, String argsPreview,
                        String htmlDescription) {

        SwingUtilities.invokeLater(() -> {
            // nur noch im Breadcrumb anzeigen
            breadcrumbLabel.setText(
                    formatBreadcrumb(fileName, feature, scenario)
            );

            stepLabel.setText(formatStepLabel(keyword, text));

            if (argsPreview == null || argsPreview.isBlank()) {
                argArea.setText("");
            } else {
                argArea.setText(argsPreview);
                argArea.setCaretPosition(0);
            }

            setHtmlDescription(htmlDescription);
            setStatusNeutral();
        });
    }

    public void setStatusPassed() {
        setStatusColor(new Color(0x27AE60));
    }

    public void setStatusFailed() {
        setStatusColor(new Color(0xC0392B));
    }

    public void setStatusNeutral() {
        setStatusColor(new Color(0x95A5A6));
    }

    public CountDownLatch armPauseButton() {
        CountDownLatch latch = new CountDownLatch(1);
        activeLatch.set(latch);
        SwingUtilities.invokeLater(() -> nextStepBtn.setEnabled(true));
        return latch;
    }

    public void releaseIfWaiting() {
        CountDownLatch latch = activeLatch.getAndSet(null);
        if (latch != null) latch.countDown();
        SwingUtilities.invokeLater(() -> nextStepBtn.setEnabled(false));
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void resetCancel() {
        cancelled = false;
    }

    public void setHtmlDescription(String html) {
        SwingUtilities.invokeLater(() -> {
            if (html == null || html.isBlank()) {
                htmlPane.setText(defaultNoDescriptionHtml());
            } else {
                htmlPane.setContentType("text/html");
                htmlPane.setText(wrapIfNeeded(html));
            }

            // Base für relative Ressourcen setzen
            if (baseUrl != null) {
                Document doc = htmlPane.getDocument();
                if (doc instanceof HTMLDocument hd) {
                    hd.setBase(baseUrl);
                }
            }

            htmlPane.setCaretPosition(0);
        });
    }

    public JComponent getComponent() {
        return root;
    }

    public JButton getStopTestButton() {
        return stopTestBtn;
    }

    public JButton getSkipScenarioButton() {
        return skipScenarioBtn;
    }

    public JButton getNextStepButton() {
        return nextStepBtn;
    }

    // ---------------------------------------------------------------------
    // Private Helfer
    // ---------------------------------------------------------------------

    private void setStatusColor(Color c) {
        SwingUtilities.invokeLater(() -> {
            statusDot.setForeground(c);
            root.repaint();
        });
    }

    private static Border createCardBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD5D8DC)),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 11),
                new Color(0x7F8C8D)
        );
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String formatBreadcrumb(String file, String feature, String scenario) {
        String f = file != null ? file : "—";
        String feat = feature != null ? feature : "(kein Feature)";
        String sc = scenario != null ? scenario : "—";
        return "<html><span style='color:#7F8C8D'>"
                + esc(f) + " &rsaquo; " + esc(feat) + " &rsaquo; " + esc(sc)
                + "</span></html>";
    }

    private static String formatStepLabel(String keyword, String text) {
        String k = keyword == null ? "" : keyword.trim();
        String t = text == null ? "" : text.trim();

        return "<html><span style='color:#7F8C8D'><b>"
                + esc(k)
                + "</b></span> "
                + esc(t)
                + "</html>";
    }

    private static String wrapIfNeeded(String html) {
        String h = html.trim();
        if (h.regionMatches(true, 0, "<html", 0, 5)) return h;

        return """
                <html>
                 <head>
                  <style>
                   body { font-family: sans-serif; font-size: 12px; margin: 8px; background:#FAFAFA; }
                   h1,h2,h3 { margin: 0 0 6px 0; }
                   code, pre { font-family: monospace; background:#F2F3F4; padding:2px 4px; border-radius:3px; }
                   ul { margin-top: 4px; }
                  </style>
                 </head>
                 <body>""" + h + "</body></html>";
    }

    private static String defaultNoDescriptionHtml() {
        return """
                <html>
                 <head>
                  <style>
                   body { font-family: sans-serif; font-size: 12px; margin: 8px; background:#FAFAFA; }
                   i { color:#7F8C8D; }
                  </style>
                 </head>
                 <body><i>Keine Beschreibung verfügbar.</i></body>
                </html>
                """;
    }

    // ---------------------------------------------------------------------
    // Button-/Icon-Helfer
    // ---------------------------------------------------------------------

    private JButton makeButton(String tooltip, String iconName) {
        ImageIcon icon = loadIcon(iconName);

        JButton b;
        if (icon != null) {
            b = new JButton(icon);
        } else {
            // Fallback: Text-Button, falls Icon fehlt
            b = new JButton(tooltip);
        }

        b.setToolTipText(tooltip);

        // reine Icon-Buttons ohne Standard-Padding
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(BorderFactory.createEmptyBorder());

        // einheitliche Größe (Button etwas größer als das 32x32-Icon)
        Dimension d = new Dimension(36, 36);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);

        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);

        return b;
    }

    private static ImageIcon loadIcon(String name) {
        URL url = StepOverlay.class.getResource("/icons/" + name);
        if (url == null) {
            System.err.println("Icon nicht gefunden: " + name);
            return null;
        }
        return new ImageIcon(url);
    }
}
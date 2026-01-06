package com.avenqo.cucumber.plugins.unifiedviewer;

import com.avenqo.cucumber.plugins.featureviewer.FeatureViewer;
import com.avenqo.cucumber.plugins.stepviewer.StepOverlay;

import javax.swing.*;
import java.awt.*;

/**
 * Gemeinsames Fenster für FeatureViewer (links) und StepOverlay (rechts).
 * <p>
 * Erwartet, dass FeatureViewer und StepOverlay jeweils eine Methode
 * getComponent() sowie StepOverlay Zugriff auf seine Buttons liefert:
 *
 *   JComponent FeatureViewer.getComponent()
 *   JComponent StepOverlay.getComponent()
 *   JButton    StepOverlay.getStopButton()
 *   JButton    StepOverlay.getCancelButton()
 *   JButton    StepOverlay.getContinueButton()
 *
 * Siehe dazu die vorgeschlagenen Anpassungen in den jeweiligen Modulen.
 */
public final class UnifiedViewerWindow {

    private static volatile UnifiedViewerWindow INSTANCE;

    private final JFrame frame;
    private final FeatureViewer featureViewer;
    private final StepOverlay stepOverlay;

    private UnifiedViewerWindow() {
        this.featureViewer = FeatureViewer.get();
        this.stepOverlay = StepOverlay.get();

        frame = new JFrame("BDD Run Viewer");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(false);
        frame.setLayout(new BorderLayout());
        frame.setSize(1400, 900);
        frame.setLocationRelativeTo(null);

        // Toolbar mit Buttons des StepOverlay
        JToolBar toolbar = createToolbar();

        // SplitPane: links FeatureViewer, rechts StepOverlay
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,      // vertikale Linie → links/rechts
                featureViewer.getComponent(),     // links
                stepOverlay.getComponent()        // rechts
        );
        splitPane.setResizeWeight(0.55);
        splitPane.setOneTouchExpandable(true);

        frame.add(toolbar, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
    }

    public static UnifiedViewerWindow get() {
        UnifiedViewerWindow inst = INSTANCE;
        if (inst == null) {
            synchronized (UnifiedViewerWindow.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnifiedViewerWindow();
                }
                inst = INSTANCE;
            }
        }
        return inst;
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton stopTestButton  = stepOverlay.getStopTestButton();
        JButton skipScenarioButton = stepOverlay.getSkipScenarioButton();
        JButton nextStepButton  = stepOverlay.getNextStepButton();

        // Icons sollten in einem der beteiligten Module unter /icons/... liegen.
        setButtonIcon(stopTestButton,  "/icons/stop.png",   "Test stoppen");
        setButtonIcon(skipScenarioButton, "/icons/abort.png",  "Szenario abbrechen");
        setButtonIcon(nextStepButton,  "/icons/next.png",   "Nächster Schritt");

        toolbar.add(stopTestButton);
        toolbar.add(skipScenarioButton);
        toolbar.add(nextStepButton);

        return toolbar;
    }

    private void setButtonIcon(JButton button, String resourcePath, String tooltip) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                button.setIcon(new ImageIcon(url));
                button.setText(""); // nur Icon anzeigen
            }
            button.setToolTipText(tooltip);
        } catch (Exception e) {
            System.err.println("[UnifiedViewerWindow] Konnte Icon nicht laden: " + resourcePath + " -> " + e);
        }
    }

    private void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public void showWindow() {
        runOnEdt(() -> frame.setVisible(true));
    }

    public void hideWindow() {
        runOnEdt(() -> frame.setVisible(false));
    }
}

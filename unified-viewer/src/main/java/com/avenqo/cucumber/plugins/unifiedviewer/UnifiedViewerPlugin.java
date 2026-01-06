package com.avenqo.cucumber.plugins.unifiedviewer;

import com.avenqo.cucumber.plugins.featureviewer.FeatureViewerPlugin;
import com.avenqo.cucumber.plugins.stepviewer.StepViewerPlugin;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestRunFinished;

public class UnifiedViewerPlugin implements EventListener {

    private final FeatureViewerPlugin featureViewerPlugin = new FeatureViewerPlugin();
    private final StepViewerPlugin stepViewerPlugin       = new StepViewerPlugin();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // 1) Alte Plugins sollen KEINE eigenen Fenster mehr öffnen
        FeatureViewerPlugin.AUTO_SHOW_WINDOW = false;
        StepViewerPlugin.AUTO_SHOW_WINDOW    = false;

        // 2) Event-Handling der alten Plugins weiterverwenden
        featureViewerPlugin.setEventPublisher(publisher);
        stepViewerPlugin.setEventPublisher(publisher);

        // 3) Unified-Fenster an den Testlauf hängen
        publisher.registerHandlerFor(TestRunStarted.class,
                e -> UnifiedViewerWindow.get().showWindow());

        publisher.registerHandlerFor(TestRunFinished.class,
                e -> UnifiedViewerWindow.get().hideWindow());
    }
}

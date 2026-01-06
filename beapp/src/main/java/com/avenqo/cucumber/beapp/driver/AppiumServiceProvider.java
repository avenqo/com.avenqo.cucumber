package com.avenqo.cucumber.beapp.driver;

import org.openqa.selenium.Capabilities;

import java.net.URI;
import java.net.URL;

public class AppiumServiceProvider {

    // ----------- Singleton -----------

    private static AppiumServiceProvider instance;

    private AppiumServiceProvider() {
    }

    public static AppiumServiceProvider instance() {
        if (instance == null) {
            instance = new AppiumServiceProvider();
        }
        return instance;
    }

    public void addCapabilities(Capabilities capabilities) {
        // TODO Auto-generated method stub

    }

    // ============= Methods =============

    URL url() {
        try {
            return new URI("http://127.0.0.1:4723").toURL();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Aborting. See stack trace printed before.");
        }
    }
}

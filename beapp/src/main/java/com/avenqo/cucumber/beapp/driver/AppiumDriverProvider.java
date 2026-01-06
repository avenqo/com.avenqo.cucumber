package com.avenqo.cucumber.beapp.driver;

import com.avenqo.cucumber.beapp.aut.AutProvider;
import com.avenqo.cucumber.beapp.device.DeviceProvider;
import com.avenqo.cucumber.beapp.driver.appium.AppiumFactory;
import com.avenqo.cucumber.beapp.driver.appium.SafariDriverKiller;
import com.avenqo.cucumber.beapp.exceptions.EConfigException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;

/**
 * Singleton providing driver.
 */
@Slf4j
public class AppiumDriverProvider {

    private static AppiumDriverProvider instance;

    private AppiumDriver driver;

    // ----------------- Singleton ---------------------

    private AppiumDriverProvider() {
    }

    public static AppiumDriverProvider instance() {
        if (instance == null) {
            instance = new AppiumDriverProvider();
        }
        return instance;
    }

    /**
     * Create driver if necessary.
     *
     * @return Current AppiumDriver
     * @throws Throwable
     */
    public AppiumDriver getDriver() throws Throwable {

        if (driver == null) {
            log.info("Try to instantiate driver.");
            // bspw: http://127.0.0.1:4723/wd/hub
            String urlAppium = new AppiumFactory(new DeviceProvider().getFileNameWithoutExtension()).ensureAppiumIsRunning();
            if (urlAppium == null) throw new EConfigException("Appium server not found! ", urlAppium!=null ? urlAppium : "urlAppium is NULL");

            // Initialize device
            Capabilities capabilities = new DeviceProvider().getDeviceCapabilities();

            // Add Capabilities according to the App
            capabilities = capabilities.merge(AutProvider.instance().getCapabilities());

            if (new DeviceProvider().isAndroid()) {
                log.info("Creating Android driver.");
                driver = new AndroidDriver(AppiumServiceProvider.instance().url(), capabilities);

            } else {
                log.info("Creating iOS driver.");

                // Alte SafariDriver können Test blockieren
                SafariDriverKiller.perform();

                driver = new IOSDriver(AppiumServiceProvider.instance().url(), capabilities);
            }

            // assertNotNull("Der Appium Treiber wurde nicht erfolgreich initialisiert?!", driver);
            if (driver == null) throw new EConfigException("Driver is null");

            log.info("Driver successfully created.");
        }
        return driver;
    }
}

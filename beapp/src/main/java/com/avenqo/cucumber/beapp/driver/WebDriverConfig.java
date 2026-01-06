package com.avenqo.cucumber.beapp.driver;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.cucumber.spring.ScenarioScope;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is a Bean definition.
 * Instantiate this just once.
 * Us it for registration of Beans within ApplicationContext.
 *
 * @Configuration = Spring configuration with @Bean methods.
 * Dont use this annotation in Glue classes.
 */
@Configuration
public class WebDriverConfig {

    private static final String DRIVER_PROPERTY = "driver"; // i.e. -Ddriver=chrome
    private static final String APPIUM_URL_PROPERTY = "appium.server.url";

    @Bean
    @ScenarioScope
    public WebDriver webDriver() throws MalformedURLException {
        String driverType = System.getProperty(DRIVER_PROPERTY, "android").toLowerCase();

        return switch (driverType) {
            //case "chrome" -> createChromeDriver();
            //case "firefox" -> createFirefoxDriver();
            case "android" -> createAndroidDriver();
            case "ios" -> createIOSDriver();
            default -> throw new IllegalArgumentException("Unknown driver type: " + driverType);
        };
    }

    // ===== Selenium driver =====

    private WebDriver createChromeDriver() {
        // Since Selenium 4: SeleniumManager takes care of binary download
        return null;//new ChromeDriver();
    }

    private WebDriver createFirefoxDriver() {
        return null;
        //  new FirefoxDriver();
    }

    // ===== Appium driver =====

    private WebDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Android Emulator");
        // options.setApp("/path/to/app.apk");
        // weitere Optionen je nach Setup ...

        URL serverUrl = new URL(System.getProperty(APPIUM_URL_PROPERTY,
                "http://127.0.0.1:4723/"));

        // ⚠️ Appium 9.x: AndroidDriver ist NICHT generisch mehr
        AppiumDriver driver = new AndroidDriver(serverUrl, options);
        return driver; // AppiumDriver implementiert WebDriver
    }

    // ---------------- iOS / Appium ----------------

    private WebDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();
        options.setDeviceName("iPhone Simulator");
        options.setPlatformVersion("17.0");
        // options.setApp("/path/to/app.app");

        URL serverUrl = new URL(System.getProperty(
                APPIUM_URL_PROPERTY,
                "http://127.0.0.1:4723/"
        ));

        AppiumDriver driver = new IOSDriver(serverUrl, options);
        return driver;
    }
}
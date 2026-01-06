package com.avenqo.cucumber.examples.pizzasvc.pages;

import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Component;

@Component
// @ScenarioScope
@PageName("Splash Screen")
@Slf4j
public class SplashScreenPage extends AbstractPage {

    @RequiredForVisible
    private final By splashImage = AppiumBy.accessibilityId("splash_screen_image");

    public SplashScreenPage(AppiumDriver driver) {
        super(driver);
    }
}

package com.avenqo.cucumber.examples.pizzasvc.pages.ios;

import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LoginPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Component;

@PageName("Login")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="ios", contract= LoginPage.class)
public class LoginPage4Ios extends AbstractPage implements LoginPage {

    @RequiredForVisible
    private static final By ROOT = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`label == \"Login\"`]");

    public LoginPage4Ios(AppiumDriver driver) {
        super(driver);
    }
}

package com.avenqo.cucumber.examples.pizzasvc.pages.android;

import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.selectors.ByAndroid;
import com.avenqo.cucumber.beapp.widget.IWidgets4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LoginPage;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Component;

@PageName("Login")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="android", contract= LoginPage.class)
public class LoginPage4Android extends AbstractPage implements LoginPage {

    @RequiredForVisible
    private static final By ROOT = ByAndroid.classWithDescription(IWidgets4Android.VIEW, "Login");

    public LoginPage4Android(AppiumDriver driver) {
        super(driver);
    }

}

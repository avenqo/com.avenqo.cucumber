package com.avenqo.cucumber.examples.pizzasvc.pages.android;

import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.selectors.ByAndroid;
import com.avenqo.cucumber.beapp.widget.IWidgets4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.BasketPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@PageName("Basket")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="android", contract= BasketPage.class)
public class BasketPage4Android extends AbstractPage implements BasketPage {

    @RequiredForVisible
    private static final By ROOT = ByAndroid.classWithDescription(IWidgets4Android.VIEW, "Warenkorb");

    public BasketPage4Android(AppiumDriver driver) {
        super(driver);
    }

    public void tryLogin() {
        log.info("");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                        ByAndroid.classWithDescription(IWidgets4Android.VIEW, "Zur Bestellung hinzugefügt.")
                ));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(ByAndroid.classWithDescription(IWidgets4Android.BUTTON, "Login, um zu bestellen")
                )
        );
        button.click();
    }
}

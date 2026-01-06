package com.avenqo.cucumber.examples.pizzasvc.pages.ios;


import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.examples.pizzasvc.pages.BasketPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

// @Component
@PageName("Basket")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="ios", contract= BasketPage.class)
public class BasketPage4Ios extends AbstractPage implements BasketPage {

    @RequiredForVisible
    private static final By ROOT = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`label == \"Warenkorb\"`]");

    public BasketPage4Ios(AppiumDriver driver) {
        super(driver);
    }

    public void tryLogin() {
        log.info("");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                        AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`label == \"Zur Bestellung hinzugefügt\"`]") //AndroidSelectorsBy.classWithDescription(IWidgets4Android.VIEW, "Zur Bestellung hinzugefügt.")
                ));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(AppiumBy.iOSClassChain("**/XCUIElementTypeButton[`label == \"Login, um zu bestellen\"`]"))
        );
        button.click();
    }
}

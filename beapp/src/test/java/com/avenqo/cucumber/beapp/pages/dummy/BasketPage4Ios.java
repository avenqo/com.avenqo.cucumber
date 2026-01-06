package com.avenqo.cucumber.beapp.pages.dummy;


import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;

// @Component
@PageName("Basket")
@Slf4j
@PageFor(platform = "ios", contract = BasketPage.class)
public class BasketPage4Ios extends AbstractPage implements BasketPage {

    public BasketPage4Ios(AppiumDriver driver) {
        super(driver);
    }

    public void tryLogin() {
        log.info("for iOS");
    }
}

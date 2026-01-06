package com.avenqo.cucumber.beapp.widget.finder;


import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Slf4j
public class E {
    @Autowired
    private AppiumDriver drv;

    public List<WebElement> fetchAllElementsByClass(String clazz) {
        return drv.findElements(AppiumBy.className(clazz));
    }
}

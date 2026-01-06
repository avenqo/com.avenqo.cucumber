package com.avenqo.cucumber.examples.pizzasvc.pages.android;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForComplete;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.selectors.ByAndroid;
import com.avenqo.cucumber.examples.pizzasvc.data.PizzaData;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@PageName("Landing")
@Slf4j
@Component
@ScenarioScope
@PageFor(platform="android", contract=LandingPage.class)
public class LandingPage4Android extends AbstractPage implements LandingPage {

    @RequiredForComplete
    @RequiredForVisible
    private static final By ROOT = AppiumBy.accessibilityId("landing page");

    @RequiredForComplete
    private final By byPizzaDesciptions = ByAndroid.partialDescription("pizza_desc_");
    @RequiredForComplete
    private final By byPizzaNames = ByAndroid.partialDescription("pizza_name_");


    public LandingPage4Android(AppiumDriver driver) {
        super(driver);
    }


    public List<PizzaData> getVisiblePizzas() throws EInconsistencyException {
        log.info("");
        List<PizzaData> result = new ArrayList<>();

        List<WebElement> pizzaNames = driver.findElements(byPizzaNames);
        List<WebElement> pizzaDescriptions = driver.findElements(byPizzaDesciptions);

        if (pizzaNames.size() != pizzaDescriptions.size())
            throw new EInconsistencyException("Descriptions and Names should have the same size!");

        for (int i = 0; i < pizzaNames.size(); i++) {
            String name = pizzaNames.get(i).getAttribute("name");
            String description = pizzaDescriptions.get(i).getAttribute("name");
            result.add(new PizzaData(name, description, null));
        }
        log.info("I can see [{}] Pizzas.", pizzaNames.size());
        return result;
    }

    public void selectPizza(String name) {
        log.info("name [{}]", name);
        WebElement element = driver.findElement(ByAndroid.partialDescription(name));//(AppiumBy.accessibilityId(name));
        element.click();
    }
}

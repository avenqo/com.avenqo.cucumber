package com.avenqo.cucumber.examples.pizzasvc.pages.ios;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.widget.CheckBox;
import com.avenqo.cucumber.beapp.widget.IWidgets4iOS;
import com.avenqo.cucumber.examples.pizzasvc.data.PriceData;
import com.avenqo.cucumber.examples.pizzasvc.data.SelectableToppingData;
import com.avenqo.cucumber.examples.pizzasvc.data.ToppingData;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.PizzaDetailsPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ScenarioScope
@PageName("Pizza Details")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="ios", contract= PizzaDetailsPage.class)
public class PizzaDetailsPage4Ios extends AbstractPage implements PizzaDetailsPage {

    @RequiredForVisible
    private final By byPageName = AppiumBy.accessibilityId("pizza_details_page");
    @RequiredForVisible
    private final By byName = AppiumBy.accessibilityId("Pizza Name");
    @RequiredForVisible
    private final By byDescription = AppiumBy.accessibilityId("Pizza Beschreibung");
    @RequiredForVisible
    private final By byPrice = AppiumBy.accessibilityId("Pizza Preis");

    private CheckBox checkBox; // injected

    public PizzaDetailsPage4Ios(AppiumDriver driver) {
        super(driver);
    }


    @Override
    public Map<String, SelectableToppingData> getVisibleToppings() throws EInconsistencyException {

        List<WebElement> toppingSwitches =
                driver.findElements(AppiumBy.className(IWidgets4iOS.SWITCH));

        var mapResult = new LinkedHashMap<String, SelectableToppingData>();
        for (WebElement el : toppingSwitches) {
            var t = ToppingData.parse(el.getAttribute("name"));
            if (t != null) {
                String value = el.getAttribute("value");
                if (value != null) {
                    mapResult.put(t.name(), new SelectableToppingData(t, value.equals("1")));
                } else throw new EInconsistencyException("Value is NULL");
            } else throw new EInconsistencyException("Name is NULL");

        }
        log.info("Found toppings [{}]", mapResult);
        return mapResult;
    }

    @Override
    public boolean isToppingVisible(SelectableToppingData expectedTopping) throws EInconsistencyException {

        log.info("expectedTopping [{}]", expectedTopping);
        Map<String, SelectableToppingData> m = getVisibleToppings();
        return m.containsKey(expectedTopping.topping().name());
    }

    @Override
    public int getIndexOfToppingByName(String topping) throws EInconsistencyException {
        log.info("topping [{}]", topping);
        List<WebElement> toppingSwitches =
                driver.findElements(AppiumBy.className(IWidgets4iOS.SWITCH));

        int index = 0;
        for (WebElement el : toppingSwitches) {
            var name = el.getAttribute("name");
            if (name == null) throw new EInconsistencyException("Name is NULL");
            if (name.contains(topping)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public void selectToppingByIndex(int index) throws Throwable {
        log.info("index [{}]", index);
        driver.findElements(AppiumBy.className(IWidgets4iOS.SWITCH)).get(index).click();
    }

    @Override
    public PriceData getPrice() {
        final var prefix = "Artikelpreis:";
        WebElement el =
                driver.findElement(AppiumBy.iOSClassChain("**/XCUIElementTypeStaticText[`label BEGINSWITH \"" + prefix + "\"`]"));
        String price = el.getAttribute("name");

        var p = PriceData.fromText(price.trim());
        log.info("Preis Data [{}]", p);
        return p;
    }

    @Override
    public void add2Basket() {
        log.info("");
        driver.findElement(AppiumBy.iOSNsPredicateString("label == \"In den Warenkorb\"")).click();
        // todo -> basket increase?
    }

    @Override
    public void goToBasket() {
        log.info("");
        driver.findElement(AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`label BEGINSWITH \"cart_icon\"`]")).click();
    }
}

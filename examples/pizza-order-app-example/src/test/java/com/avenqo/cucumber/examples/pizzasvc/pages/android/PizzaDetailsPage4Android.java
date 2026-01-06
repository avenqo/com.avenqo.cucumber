package com.avenqo.cucumber.examples.pizzasvc.pages.android;

import com.avenqo.cucumber.beapp.pages.AbstractPage;
import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.selectors.ByAndroid;
import com.avenqo.cucumber.beapp.widget.CheckBox;
import com.avenqo.cucumber.beapp.widget.IWidgets4Android;
import com.avenqo.cucumber.examples.pizzasvc.data.PriceData;
import com.avenqo.cucumber.examples.pizzasvc.data.SelectableToppingData;
import com.avenqo.cucumber.examples.pizzasvc.data.ToppingData;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.PizzaDetailsPage;
import com.avenqo.cucumber.examples.pizzasvc.utils.PriceUtil;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @ScenarioScope
@PageName("Pizza Details")
@Slf4j
@Component
//@ScenarioScope
@PageFor(platform="android", contract= PizzaDetailsPage.class)
public class PizzaDetailsPage4Android extends AbstractPage implements PizzaDetailsPage {

    @RequiredForVisible
    private final By byPageName = AppiumBy.accessibilityId("pizza_details_page");
    @RequiredForVisible
    private final By byName = AppiumBy.accessibilityId("Pizza Name");
    @RequiredForVisible
    private final By byDescription = AppiumBy.accessibilityId("Pizza Beschreibung");
    @RequiredForVisible
    private final By byPrice = AppiumBy.accessibilityId("Pizza Preis");

    private final CheckBox checkBox; // injected

    public PizzaDetailsPage4Android(AppiumDriver driver) {
        super(driver);
        this.checkBox = new CheckBox(driver);
    }

    private static ToppingData parseTopping(String raw) {

        String[] parts = raw.split("\n");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unexpected format: " + raw);
        }

        String pricePart = parts[0].trim();  // z.B. "1,00€"
        String name = parts[1].trim();  // z.B. "Zwiebeln"


        return new ToppingData(name, PriceUtil.parse(pricePart));
    }


    /**
     * Liefert alle sichtbaren Toppings in der Form:
     * key = Topping-Name (lowercase)
     * value = Preis (z.B. "2,50€")
     */
    public Map<String, SelectableToppingData> getVisibleToppings() {

        // Alle Knoten finden, die ein Topping repräsentieren
        List<WebElement> toppingContainers =
                driver.findElements(AppiumBy.accessibilityId("Pizza Topping"));

        Map<String, SelectableToppingData> visibleToppings = new LinkedHashMap<>();
        for (WebElement el : toppingContainers) {
            WebElement checkBox = el.findElement(AppiumBy.className("android.widget.CheckBox"));
            String s = checkBox.getAttribute("content-desc"); // "1,00€\nZwiebeln"
            ToppingData topping = parseTopping(s);

            if (topping != null) {
                visibleToppings.put(topping.name(), new SelectableToppingData(topping, checkBox.isSelected()));
            }
        }
        log.info("returning [{}]", visibleToppings);
        return visibleToppings;
    }

    /**
     * check if a topping is visible having the same properties.
     */
    public boolean isToppingVisible(SelectableToppingData expectedTopping) {
        log.info("expectedTopping [{}]", expectedTopping);
        Map<String, SelectableToppingData> foundToppings = getVisibleToppings();
        return foundToppings.containsValue(expectedTopping);
    }

    public int getIndexOfToppingByName(String topping) {
        log.info("topping [{}]", topping);
        List<String> keys = new ArrayList<>(getVisibleToppings().keySet());
        return keys.indexOf(topping);
    }

    public void selectToppingByIndex(int index) throws Throwable {
        log.info("index [{}]", index);
        checkBox.switchOn(index, true);
        //driver.findElements(AppiumBy.className("android."));
    }

    public PriceData getPrice() {
        final String s = "Artikelpreis:";
        String txt = driver.findElement(ByAndroid.classWithPartialDescription(IWidgets4Android.VIEW, s)).getAttribute("content-desc");
        txt = txt.substring(s.length()).trim();
        log.info("returning [{}]", txt);
        return PriceUtil.parse(txt);
    }

    public void add2Basket() {
        log.info("");
        driver.findElement(AppiumBy.accessibilityId("In den Warenkorb")).click();
    }

    public void goToBasket() {
        log.info("");
        driver.findElement(ByAndroid.classWithPartialDescription(IWidgets4Android.VIEW, "cart_icon")).click();
    }
}

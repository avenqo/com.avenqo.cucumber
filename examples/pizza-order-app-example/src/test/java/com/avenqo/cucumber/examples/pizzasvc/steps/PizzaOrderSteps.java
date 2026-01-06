package com.avenqo.cucumber.examples.pizzasvc.steps;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.exceptions.ENotImplementedYet;
import com.avenqo.cucumber.beapp.pages.PageFactory;
import com.avenqo.cucumber.examples.pizzasvc.data.PizzaData;
import com.avenqo.cucumber.examples.pizzasvc.data.PriceData;
import com.avenqo.cucumber.examples.pizzasvc.data.SelectableToppingData;
import com.avenqo.cucumber.examples.pizzasvc.data.ToppingData;
import com.avenqo.cucumber.examples.pizzasvc.pages.*;
import com.avenqo.cucumber.examples.pizzasvc.utils.PriceUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Slf4j
public class PizzaOrderSteps {
    private final AppiumDriver driver;

    @Autowired
    private LandingPage landingPage;
    //@Autowired
    //PageFactory pages;
    @Autowired
    private SplashScreenPage splashScreen;
    @Autowired
    private PizzaDetailsPage pizzaDetailsPage;
    @Autowired
    private BasketPage basketPage;
    @Autowired
    private LoginPage loginPage;

    @Autowired
    public PizzaOrderSteps(AppiumDriver driver) {
        this.driver = driver;
    }

    // ------------------------------------------------------------------------

    @Given("the App is installed")
    public void the_app_is_installed() throws Throwable {
        log.info("");
        boolean installed = ((InteractsWithApps) driver)
                .isAppInstalled("com.avenqo.cucumber.exampleapp.italianfrisbee");
        if (!installed) {
            throw new ENotImplementedYet("The installation process isn't implemented!");
        }
    }

    @When("I open the App")
    public void i_open_the_app() {
        log.info("");
        splashScreen.waitUntilVisible();

        //LandingPage landingPage = pages.create(LandingPage.class);
        landingPage.waitUntilVisible();
    }


    @Then("the page Landing shows at least {int} base pizzas")
    public void landing_shows_at_least_base_pizzas(int expectedCount) throws EInconsistencyException {
        log.info("expectedCount [{}]", expectedCount);
        //LandingPage landingPage = pages.create(LandingPage.class);
        List<PizzaData> pizzas = landingPage.getVisiblePizzas();
        Assertions.assertTrue(pizzas.size() >= expectedCount,
                "Expected at least " + expectedCount + " pizzas, but got " + pizzas.size());
    }

    // ------------------------------------------------------------------------
    // SCENARIO 2: Toppings anzeigen
    // ------------------------------------------------------------------------


    @When("I select the pizza {string}")
    public void i_select_the_pizza(String pizzaName) {
        log.info("pizzaName [{}]", pizzaName);
        //LandingPage landingPage = pages.create(LandingPage.class);
        landingPage.selectPizza(pizzaName);
        pizzaDetailsPage.isVisible();

        // Assertions.assertEquals("Margherita vegana", pizzaName);

    }


    @Then("I can see the following optional pizza toppings")
    public void i_can_see_the_following_optional_pizza_toppings(DataTable table) throws EInconsistencyException {
        log.info("data table [{}]", table);
        Map<String, SelectableToppingData> visibleToppings = pizzaDetailsPage.getVisibleToppings();

        // Jeach row: [0] = Name, [1] = Price
        List<List<String>> rows = table.asLists(String.class);

        for (List<String> row : rows) {
            log.info("checking topping [{}]", row);
            ToppingData expectedTopping = new ToppingData(row.get(0), PriceUtil.parse(row.get(1)));

            // Assertions nie im PageObject!
            Assertions.assertTrue(
                    pizzaDetailsPage.isToppingVisible(new SelectableToppingData(expectedTopping, false)));
        }
    }

    @Then("I cannot see topping options like")
    public void i_cannot_see_topping_options_like(DataTable table) throws EInconsistencyException {
        log.info("data table [{}]", table);
        Map<String, SelectableToppingData> visibleToppings = pizzaDetailsPage.getVisibleToppings();
        List<List<String>> rows = table.asLists(String.class);
        for (List<String> row : rows) {
            Assertions.assertFalse(visibleToppings.containsKey(row.get(0)));
        }
    }

    // ------------------------------------------------------------------------
    // SCENARIO 3: Preis aktualisiert sich
    // ------------------------------------------------------------------------

    @When("I select topping option {string}")
    public void i_select_topping_option(String topping) throws Throwable {
        log.info("topping [{}]", topping);
        int index = pizzaDetailsPage.getIndexOfToppingByName(topping);
        Assertions.assertTrue(index >= 0, "Index of topping '" + topping + "' should be at least 0.");

        pizzaDetailsPage.selectToppingByIndex(index);
    }

    @Then("the price of my order is {string}")
    public void the_price_of_my_order_is(String expectedPrice) {
        log.info("expectedPrice [{}]", expectedPrice);
        PriceData price = pizzaDetailsPage.getPrice();
        Assertions.assertEquals(PriceUtil.parse(expectedPrice), price, "Check the price!");
    }

    @Then("I can add my current Pizza selection to the basket")
    public void add_selection_to_my_basket() {
        log.info("");
        pizzaDetailsPage.add2Basket();
        //TODO: check basket badge
    }

    // ------------------------------------------------------------------------
    // SCENARIO 4: Nutzer ist nicht eingeloggt
    // ------------------------------------------------------------------------


    @Given("the basket is open")
    public void openBasket() {
        log.info("");
        pizzaDetailsPage.goToBasket();
        basketPage.waitUntilVisible();
    }

    @When("I try to order")
    public void i_try_to_order() {
        log.info("");
        basketPage.tryLogin();
        basketPage.waitUntilInvisible();
    }
}


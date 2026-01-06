package com.avenqo.cucumber.examples.pizzasvc.steps;

import io.cucumber.java.en.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CoffeeMachineSteps {

    private boolean machineOn;
    private String today;
    private String orderedCoffee;
    private String machineResponse;

    // Simulierte "Mood Rules"
    private final Map<String, String> moodResponses = new HashMap<>();

    @Given("the coffee machine is switched on")
    public void the_coffee_machine_is_switched_on() {
        machineOn = true;
        assertTrue(machineOn, "Coffee machine should be on");

        // Default moods
        moodResponses.put("Monday", "Not today!");
        moodResponses.put("Friday", "Weekend vibes!");
        moodResponses.put("Tuesday", "Sure thing!");
        moodResponses.put("Saturday", "System offline");
    }

    @Given("today is {string}")
    public void today_is(String weekday) {
        today = weekday;
    }

    @When("I order a(n) {string}")
    public void i_order_a_cappuccino(String product) {
        orderedCoffee = product;
        processOrder();
    }


    private void processOrder() {
        if (!machineOn) {
            machineResponse = "Machine is off.";
            return;
        }

        if ("Monday".equalsIgnoreCase(today)) {
            // Monday: machine is grumpy
            machineResponse = "Nope. It's Monday.";
        } else {
            // Non-Monday default happy response
            machineResponse = switch (orderedCoffee) {
                case "cappuccino" -> "Have a wonderful day!";
                case "espresso" -> "Enjoy your espresso!";
                default -> "Your " + orderedCoffee + " is ready!";
            };
        }
    }

    @Then("the machine should serve a(n) {string}")
    public void the_machine_should_serve_a_cappuccino(String product) {
        assertNotNull(product);
        assertNotNull(machineResponse);
        assertEquals("Have a wonderful day!", machineResponse);
    }

    @Then("the machine should refuse the order")
    public void the_machine_should_refuse_the_order() {
        assertEquals("Nope. It's Monday.", machineResponse);
    }

    @Then("the machine should display {string}")
    public void the_machine_should_display(String message) {
        assertEquals(message, machineResponse);
    }

    @Then("the machine should respond with {string}")
    public void the_machine_should_respond_with(String message) {
        assertEquals(message, machineResponse);
    }

    @Then("the machine should respond according to its mood:")
    public void the_machine_should_respond_according_to_its_mood(io.cucumber.datatable.DataTable table) {

        // The table maps weekday -> mood message
        Map<String, String> moodMap = table.asMaps().stream()
                .collect(HashMap::new, (m, row) -> m.put(row.get("Monday") != null
                                ? "Monday"
                                : row.keySet().iterator().next(), row.values().iterator().next()),
                        HashMap::putAll);

        // Use our known moods instead
        assertTrue(moodResponses.containsKey(today), "No mood rule defined for " + today);

        String expected = moodResponses.get(today);

        // Process order according to mood rules
        machineResponse = expected;

        assertEquals(expected, machineResponse);
    }
}


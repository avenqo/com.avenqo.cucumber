package com.avenqo.cucumber.beapp.glue;

import com.avenqo.cucumber.beapp.pages.IPage;
import com.avenqo.cucumber.beapp.pages.PageRegistry;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class CommonPageSteps {

    private final PageRegistry pages;

    public CommonPageSteps(PageRegistry pages) {
        this.pages = pages;
    }

    @Then("I see the page {string}")
    public void i_see_the_page(String pageName) {
        IPage page = pages.get(pageName);
        page.waitUntilVisible();
        Assertions.assertTrue(
                page.isVisible(),
                "Expected page '" + pageName + "' to be visible");
    }

    @Then("the page {string} is complete")
    public void the_page_is_complete(String pageName) {
        IPage page = pages.get(pageName);
        Assertions.assertTrue(
                page.isComplete(),
                "Expected page '" + pageName + "' to be complete");
    }

    @Given("the page {string} is visible")
    public void the_page_is_visible(String pageName) {
        IPage page = pages.get(pageName);
        Assertions.assertTrue(
                page.isVisible(),
                "Expected page '" + pageName + "' to be visible");
    }
}

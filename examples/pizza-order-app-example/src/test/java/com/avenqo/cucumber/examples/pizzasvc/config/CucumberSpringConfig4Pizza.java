package com.avenqo.cucumber.examples.pizzasvc.config;

import com.avenqo.cucumber.beapp.config.BeAppFrameworkConfig;
import com.avenqo.cucumber.beapp.pages.PageFactory;
import com.avenqo.cucumber.examples.pizzasvc.pages.BasketPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LandingPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.LoginPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.PizzaDetailsPage;
import com.avenqo.cucumber.examples.pizzasvc.pages.android.BasketPage4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.android.LandingPage4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.android.LoginPage4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.android.PizzaDetailsPage4Android;
import com.avenqo.cucumber.examples.pizzasvc.pages.ios.BasketPage4Ios;
import com.avenqo.cucumber.examples.pizzasvc.pages.ios.LandingPage4Ios;
import com.avenqo.cucumber.examples.pizzasvc.pages.ios.LoginPage4Ios;
import com.avenqo.cucumber.examples.pizzasvc.pages.ios.PizzaDetailsPage4Ios;
import io.appium.java_client.AppiumDriver;
import io.cucumber.spring.ScenarioScope;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
        "com.avenqo.cucumber.examples.pizzasvc"
})
@Import(BeAppFrameworkConfig.class)
public class CucumberSpringConfig4Pizza {

    @Autowired
    PageFactory pages;

    @Bean
    @ScenarioScope
    public LandingPage landingPage() {
        return pages.create(LandingPage.class);
    }

    @Bean
    @ScenarioScope
    public BasketPage basketPage() {
        return pages.create(BasketPage.class);
    }

    @Bean
    @ScenarioScope
    public LoginPage loginPage() {
        return pages.create(LoginPage.class);
    }


    @Bean
    @ScenarioScope
    public PizzaDetailsPage pizzaDetailsPage() {
        return pages.create(PizzaDetailsPage.class);
    }
}

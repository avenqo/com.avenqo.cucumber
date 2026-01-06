package com.avenqo.cucumber.beapp.config;

import com.avenqo.cucumber.beapp.driver.AppiumDriverProvider;
import io.appium.java_client.AppiumDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.avenqo.cucumber.beapp")
public class BeAppFrameworkConfig {

    @Bean(destroyMethod = "quit")
    // @ScenarioScope // lifecycle is scenario
    public AppiumDriver appiumDriver() throws Throwable {
        return AppiumDriverProvider.instance().getDriver();
    }
}

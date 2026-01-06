package com.avenqo.cucumber.beapp.driver.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = { TestConfig.class })
public class CucumberSpringConfiguration {
}

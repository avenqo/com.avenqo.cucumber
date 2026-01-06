package com.avenqo.cucumber.examples.pizzasvc.steps;

import com.avenqo.cucumber.beapp.device.DeviceConfig;
import com.avenqo.cucumber.examples.pizzasvc.config.CucumberSpringConfig4Pizza;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = {CucumberSpringConfig4Pizza.class,
        DeviceConfig.class})
public class CucumberSpringTestConfig4Pizza {
    // keine Inhalte nötig
}

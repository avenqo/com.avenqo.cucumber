package com.avenqo.cucumber.beapp.device;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceConfig {

    @Bean
    // @ScenarioScope - it is singleton scope
    public DeviceProvider device() {
        return new DeviceProvider();
    }
}

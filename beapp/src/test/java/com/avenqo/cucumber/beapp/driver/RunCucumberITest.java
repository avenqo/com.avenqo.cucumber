package com.avenqo.cucumber.beapp.driver;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.avenqo.cucumber.beapp.driver.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "junit:target/cucumber-reports/Cucumber.xml," +
        "json:target/cucumber-reports/Cucumber.json," +
        "html:target/cucumber-reports/Cucumber.html," +
        "timeline:target/cucumber-reports/CucumberTimeline"
)
public class RunCucumberITest {
}

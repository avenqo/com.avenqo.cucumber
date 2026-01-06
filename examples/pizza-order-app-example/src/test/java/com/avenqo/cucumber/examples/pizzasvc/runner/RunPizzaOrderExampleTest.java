package com.avenqo.cucumber.examples.pizzasvc.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")

// Wird nur von der JUnit Platform ausgewertet — nicht von Cucumber selbst.
// Wenn man Cucumber über mvn test startet, hat @SelectPackages keinerlei Einfluss darauf,
// welche Feature-Dateien ausgeführt werden.
// @SelectPackages("features.ohne_zvp.done")

@SelectClasspathResource("features")

@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "junit:target/cucumber-reports/Cucumber.xml," +
        "json:target/cucumber-reports/Cucumber.json," +
        "html:target/cucumber-reports/Cucumber.html," +
        "timeline:target/cucumber-reports/CucumberTimeline,"
        //"com.avenqo.cucumber.plugins.featureviewer.FeatureViewerPlugin," +
        //"com.avenqo.cucumber.plugins.stepviewer.StepViewerPlugin"
        //+ "com.avenqo.cucumber.plugins.unifiedviewer.UnifiedViewerPlugin"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com/avenqo/cucumber/examples/pizzasvc,com/avenqo/cucumber/beapp/glue")

@ConfigurationParameter(key = EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")


public class RunPizzaOrderExampleTest {
}

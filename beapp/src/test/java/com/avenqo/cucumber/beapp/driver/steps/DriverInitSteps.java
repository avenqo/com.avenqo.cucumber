package com.avenqo.cucumber.beapp.driver.steps;

import com.avenqo.cucumber.beapp.device.DeviceProvider;
import com.avenqo.cucumber.beapp.driver.AppiumDriverProvider;
import com.avenqo.cucumber.beapp.driver.appium.AppiumFactory;
import com.avenqo.cucumber.beapp.exceptions.EConfigException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DriverInitSteps {

    private MockedConstruction<DeviceProvider> deviceProviderMock;
    private MockedConstruction<AppiumFactory> appiumFactoryMock;

    private Throwable caught;

    @Before
    public void resetSingleton() throws Exception {
        Field instanceField = AppiumDriverProvider.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Given("Appium is not running")
    public void appium_is_not_running() {
        deviceProviderMock = mockConstruction(DeviceProvider.class,
                (mock, ctx) -> when(mock.getFileNameWithoutExtension()).thenReturn("android_emu"));

        appiumFactoryMock = mockConstruction(AppiumFactory.class,
                (mock, ctx) -> when(mock.ensureAppiumIsRunning()).thenReturn(null));
    }

    @When("I request an Appium driver")
    public void i_request_an_appium_driver() {
        try {
            AppiumDriverProvider.instance().getDriver();
        } catch (Throwable t) {
            caught = t;
        }
    }

    @Then("an EConfigException is thrown")
    public void an_e_config_exception_is_thrown() {
        assertNotNull(caught, "Es muss eine Exception auftreten.");
        assertTrue(caught instanceof EConfigException, "Erwartet: EConfigException, war: " + caught);
        assertTrue(caught.getMessage().contains("Appium server not found"));
    }

    @After
    public void tearDown() {
        if (deviceProviderMock != null) deviceProviderMock.close();
        if (appiumFactoryMock != null) appiumFactoryMock.close();
    }
}

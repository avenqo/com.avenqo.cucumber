package com.avenqo.cucumber.beapp.driver;

import com.avenqo.cucumber.beapp.aut.AutProvider;
import com.avenqo.cucumber.beapp.device.DeviceProvider;
import com.avenqo.cucumber.beapp.driver.appium.AppiumFactory;
import com.avenqo.cucumber.beapp.driver.appium.SafariDriverKiller;
import com.avenqo.cucumber.beapp.exceptions.EConfigException;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppiumDriverProviderTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // AppiumDriverProvider.instance = null setzen (damit driver auch null ist)
        Field instanceField = AppiumDriverProvider.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void shouldThrowIfAppiumNotRunning() throws Throwable {
        try (MockedConstruction<DeviceProvider> dp = mockConstruction(DeviceProvider.class,
                (mock, ctx) -> when(mock.getFileNameWithoutExtension()).thenReturn("android_emu"));
             MockedConstruction<AppiumFactory> af = mockConstruction(AppiumFactory.class,
                     (mock, ctx) -> when(mock.ensureAppiumIsRunning()).thenReturn(null))) {

            Throwable ex = assertThrows(EConfigException.class,
                    () -> AppiumDriverProvider.instance().getDriver());

            assertTrue(ex.getMessage().contains("Appium server not found"));
        }
    }

    @Test
    void shouldCreateAndroidDriverAndCacheIt() throws Throwable {
        URL fakeUrl = new URL("http://127.0.0.1:4723/wd/hub");

        DesiredCapabilities deviceCaps = new DesiredCapabilities();
        deviceCaps.setCapability("platformName", "Android");

        DesiredCapabilities autCaps = new DesiredCapabilities();
        autCaps.setCapability("app", "fake.apk");

        // AppiumServiceProvider.instance().url()
        AppiumServiceProvider aspMock = mock(AppiumServiceProvider.class);
        when(aspMock.url()).thenReturn(fakeUrl);

        AutProvider autProviderMock = mock(AutProvider.class);
        when(autProviderMock.getCapabilities()).thenReturn(autCaps);

        try (MockedConstruction<DeviceProvider> dp = mockConstruction(DeviceProvider.class, (mock, ctx) -> {
            when(mock.getFileNameWithoutExtension()).thenReturn("android_emu");
            when(mock.getDeviceCapabilities()).thenReturn(deviceCaps);
            when(mock.isAndroid()).thenReturn(true);
        });
             MockedConstruction<AppiumFactory> af = mockConstruction(AppiumFactory.class,
                     (mock, ctx) -> when(mock.ensureAppiumIsRunning()).thenReturn(fakeUrl.toString()));
             MockedStatic<AppiumServiceProvider> aspStatic = mockStatic(AppiumServiceProvider.class);
             MockedStatic<AutProvider> autStatic = mockStatic(AutProvider.class);
             MockedConstruction<AndroidDriver> androidCtor = mockConstruction(AndroidDriver.class)
        ) {
            aspStatic.when(AppiumServiceProvider::instance).thenReturn(aspMock);
            autStatic.when(AutProvider::instance).thenReturn(autProviderMock);

            var provider = AppiumDriverProvider.instance();

            var d1 = provider.getDriver();
            var d2 = provider.getDriver();

            assertSame(d1, d2, "Driver muss gecached sein (Singleton innerhalb Provider).");
            assertEquals(1, androidCtor.constructed().size(), "AndroidDriver darf nur einmal gebaut werden.");
        }
    }

    @Test
    void shouldCreateIosDriverAndCallSafariKiller() throws Throwable {
        URL fakeUrl = new URL("http://127.0.0.1:4723/wd/hub");

        DesiredCapabilities deviceCaps = new DesiredCapabilities();
        deviceCaps.setCapability("platformName", "iOS");

        DesiredCapabilities autCaps = new DesiredCapabilities();
        autCaps.setCapability("bundleId", "com.example.app");

        AppiumServiceProvider aspMock = mock(AppiumServiceProvider.class);
        when(aspMock.url()).thenReturn(fakeUrl);

        AutProvider autProviderMock = mock(AutProvider.class);
        when(autProviderMock.getCapabilities()).thenReturn(autCaps);

        try (MockedConstruction<DeviceProvider> dp = mockConstruction(DeviceProvider.class, (mock, ctx) -> {
            when(mock.getFileNameWithoutExtension()).thenReturn("ios_sim");
            when(mock.getDeviceCapabilities()).thenReturn(deviceCaps);
            when(mock.isAndroid()).thenReturn(false);
        });
             MockedConstruction<AppiumFactory> af = mockConstruction(AppiumFactory.class,
                     (mock, ctx) -> when(mock.ensureAppiumIsRunning()).thenReturn(fakeUrl.toString()));
             MockedStatic<AppiumServiceProvider> aspStatic = mockStatic(AppiumServiceProvider.class);
             MockedStatic<AutProvider> autStatic = mockStatic(AutProvider.class);
             MockedStatic<SafariDriverKiller> safariStatic = mockStatic(SafariDriverKiller.class);
             MockedConstruction<IOSDriver> iosCtor = mockConstruction(IOSDriver.class)
        ) {
            aspStatic.when(AppiumServiceProvider::instance).thenReturn(aspMock);
            autStatic.when(AutProvider::instance).thenReturn(autProviderMock);

            var provider = AppiumDriverProvider.instance();
            var driver = provider.getDriver();

            assertNotNull(driver);
            assertEquals(1, iosCtor.constructed().size());

            safariStatic.verify(SafariDriverKiller::perform, times(1));
        }
    }

    @Test
    void shouldMergeDeviceAndAutCapabilities() throws Throwable {
        URL fakeUrl = new URL("http://127.0.0.1:4723/wd/hub");

        DesiredCapabilities deviceCaps = new DesiredCapabilities();
        deviceCaps.setCapability("platformName", "Android");
        deviceCaps.setCapability("deviceName", "Pixel");

        DesiredCapabilities autCaps = new DesiredCapabilities();
        autCaps.setCapability("app", "fake.apk");
        autCaps.setCapability("automationName", "UiAutomator2");

        AppiumServiceProvider aspMock = mock(AppiumServiceProvider.class);
        when(aspMock.url()).thenReturn(fakeUrl);

        AutProvider autProviderMock = mock(AutProvider.class);
        when(autProviderMock.getCapabilities()).thenReturn(autCaps);

        // ✅ Mockito 5: constructor arguments via callback capturen
        AtomicReference<Capabilities> capturedCaps = new AtomicReference<>();

        try (MockedConstruction<DeviceProvider> dp = mockConstruction(DeviceProvider.class, (mock, ctx) -> {
            when(mock.getFileNameWithoutExtension()).thenReturn("android_emu");
            when(mock.getDeviceCapabilities()).thenReturn(deviceCaps);
            when(mock.isAndroid()).thenReturn(true);
        });
             MockedConstruction<AppiumFactory> af = mockConstruction(AppiumFactory.class,
                     (mock, ctx) -> when(mock.ensureAppiumIsRunning()).thenReturn(fakeUrl.toString()));
             MockedStatic<AppiumServiceProvider> aspStatic = mockStatic(AppiumServiceProvider.class);
             MockedStatic<AutProvider> autStatic = mockStatic(AutProvider.class);
             MockedConstruction<AndroidDriver> androidCtor = mockConstruction(AndroidDriver.class,
                     (mock, context) -> {
                         // constructor signature: AndroidDriver(URL, Capabilities)
                         Object arg1 = context.arguments().get(1);
                         assertTrue(arg1 instanceof Capabilities, "Second ctor arg must be Capabilities");
                         capturedCaps.set((Capabilities) arg1);
                     })
        ) {
            aspStatic.when(AppiumServiceProvider::instance).thenReturn(aspMock);
            autStatic.when(AutProvider::instance).thenReturn(autProviderMock);

            AppiumDriverProvider.instance().getDriver();

            assertEquals(1, androidCtor.constructed().size());

            Capabilities merged = capturedCaps.get();
            assertNotNull(merged, "Capabilities should have been captured from AndroidDriver constructor");

            assertEquals(
                    "android",
                    String.valueOf(merged.getCapability("platformName")).toLowerCase()
            );
            assertEquals("Pixel", merged.getCapability("deviceName"));
            assertEquals("fake.apk", merged.getCapability("app"));
            assertEquals("UiAutomator2", merged.getCapability("automationName"));
        }
    }
}

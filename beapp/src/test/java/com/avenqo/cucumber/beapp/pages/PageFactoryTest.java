package com.avenqo.cucumber.beapp.pages;

import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import com.avenqo.cucumber.beapp.pages.dummy.BasketPage;
import com.avenqo.cucumber.beapp.pages.dummy.BasketPage4Android;
import com.avenqo.cucumber.beapp.pages.dummy.BasketPage4Ios;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PageFactoryTest {

    @Test
    void shouldReturnAndroidImplementationForAndroidPlatform() {
        // given
        AppiumDriver driver = mock(AppiumDriver.class);
        Capabilities caps = mock(Capabilities.class);
        when(driver.getCapabilities()).thenReturn(caps);
        when(caps.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn("android");

        ApplicationContext ctx = mock(ApplicationContext.class);

        BasketPage4Android androidBean = new BasketPage4Android(null);
        BasketPage4Ios iosBean = new BasketPage4Ios(null);

        // IMPORTANT: PageFactory iteriert über ctx.getBeanNamesForAnnotation(PageFor.class)
        when(ctx.getBeanNamesForAnnotation(PageFor.class))
                .thenReturn(new String[]{"landingAndroid", "landingIos"});

        // PageFactory fragt dann den Typ ab, um die Annotation auszulesen
        doReturn(BasketPage4Android.class).when(ctx).getType("landingAndroid");
        doReturn(BasketPage4Ios.class).when(ctx).getType("landingIos");

        // Und erst beim Match holt sie die Bean
        when(ctx.getBean("landingAndroid")).thenReturn(androidBean);
        when(ctx.getBean("landingIos")).thenReturn(iosBean);

        PageFactory factory = new PageFactory(driver, ctx);

        // when
        BasketPage page = factory.create(BasketPage.class);

        // then
        assertSame(androidBean, page);

        // Optional: Verifiziere, dass nur die Android-Bean wirklich geholt wurde
        verify(ctx).getBean("landingAndroid");
        verify(ctx, never()).getBean("landingIos");
    }

    @Test
    void shouldReturnIosImplementationForIosPlatform() {
        // given
        AppiumDriver driver = mock(AppiumDriver.class);
        Capabilities caps = mock(Capabilities.class);
        when(driver.getCapabilities()).thenReturn(caps);
        when(caps.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn("iOS"); // mixed case

        ApplicationContext ctx = mock(ApplicationContext.class);

        BasketPage4Android androidBean = new BasketPage4Android(null);
        BasketPage4Ios iosBean = new BasketPage4Ios(null);

        when(ctx.getBeanNamesForAnnotation(PageFor.class))
                .thenReturn(new String[]{"landingAndroid", "landingIos"});

        doReturn(BasketPage4Android.class).when(ctx).getType("landingAndroid");
        doReturn(BasketPage4Ios.class).when(ctx).getType("landingIos");

        when(ctx.getBean("landingAndroid")).thenReturn(androidBean);
        when(ctx.getBean("landingIos")).thenReturn(iosBean);

        PageFactory factory = new PageFactory(driver, ctx);

        // when
        BasketPage page = factory.create(BasketPage.class);

        // then
        assertSame(iosBean, page);

        verify(ctx).getBean("landingIos");
        verify(ctx, never()).getBean("landingAndroid");
    }

    @Test
    void shouldThrowIfNoMatchingImplementationFound() {
        // given
        AppiumDriver driver = mock(AppiumDriver.class);
        Capabilities caps = mock(Capabilities.class);
        when(driver.getCapabilities()).thenReturn(caps);
        when(caps.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn("android");

        ApplicationContext ctx = mock(ApplicationContext.class);

        // keine @PageFor beans vorhanden
        when(ctx.getBeanNamesForAnnotation(PageFor.class)).thenReturn(new String[0]);

        PageFactory factory = new PageFactory(driver, ctx);

        // when / then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> factory.create(BasketPage.class));

        assertTrue(ex.getMessage().contains("No Page implementation"));
        assertTrue(ex.getMessage().contains("BasketPage"));
        assertTrue(ex.getMessage().toLowerCase().contains("android"));
    }
}
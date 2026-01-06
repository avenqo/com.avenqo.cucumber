package com.avenqo.cucumber.beapp.pages;

import com.avenqo.cucumber.beapp.pages.annotations.PageFor;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class PageFactory {

    private final AppiumDriver driver;
    private final ApplicationContext ctx;

    public PageFactory(AppiumDriver driver, ApplicationContext ctx) {
        this.driver = driver;
        this.ctx = ctx;
    }

    public <T> T create(Class<T> contract) {
        String platform = String.valueOf(
                driver.getCapabilities().getCapability(CapabilityType.PLATFORM_NAME)
        ).toLowerCase();

        for (String beanName : ctx.getBeanNamesForAnnotation(PageFor.class)) {

            Class<?> beanType = ctx.getType(beanName);
            if (beanType == null) continue;

            PageFor ann = AnnotationUtils.findAnnotation(beanType, PageFor.class);
            if (ann == null) continue;

            if (ann.contract().equals(contract) && ann.platform().equalsIgnoreCase(platform)) {
                // ✅ erst jetzt tatsächliche Bean holen (ScenarioScope ist dann registriert)
                return contract.cast(ctx.getBean(beanName));
            }
        }

        throw new IllegalStateException(
                "No Page implementation for contract=" + contract.getSimpleName() + " platform=" + platform
        );
    }
}
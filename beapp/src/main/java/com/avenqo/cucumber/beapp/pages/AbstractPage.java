package com.avenqo.cucumber.beapp.pages;

import com.avenqo.cucumber.beapp.exceptions.ETimeout;
import com.avenqo.cucumber.beapp.pages.annotations.PageName;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForComplete;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForInvisible;
import com.avenqo.cucumber.beapp.pages.annotations.RequiredForVisible;
import com.avenqo.cucumber.beapp.wait.Wait;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Duration;

@Slf4j
public abstract class AbstractPage implements IPage {

    protected final static String RETURNING = "RETURNING";
    protected final AppiumDriver driver;
    private final Duration defaultTimeout = Duration.ofSeconds(10);

    protected AbstractPage(AppiumDriver driver) {
        this.driver = driver;
    }

    //protected abstract By rootLocator(); // optional – wenn du ein Hauptelement nutzt

    @Override
    public boolean isVisible() {
        return allAnnotatedElementsPresent(RequiredForVisible.class);
    }

    @Override
    public boolean isComplete() {
        log.info("name [{}]", getName());
        return allAnnotatedElementsPresent(RequiredForComplete.class);
    }

    @Override
    public void waitUntilVisible(Duration d) throws ETimeout{
        log.info("Duration [{}]", d);
        Wait.until(
                () -> {
                    try {
                        waitForAnnotatedElementsBecomeVisible(RequiredForVisible.class);
                        return true;
                    } catch (TimeoutException e){
                        // nothing
                    }
                    return false;
                },
                d,
                Duration.ofMillis(200)
        );
    }


    @Override
    public void waitUntilVisible() {
        log.info("");
        waitForAnnotatedElementsBecomeVisible(RequiredForVisible.class);
    }

    @Override
    public void waitUntilInvisible() {
        log.info("");
        waitForAnnotatedElementsBecomeInvinsible(RequiredForInvisible.class);
    }

    @Override
    public void waitUntilInvisible(Duration d) throws ETimeout{
        log.info("Duration [{}]", d);
        Wait.until(
                () -> {
                    try {
                        waitForAnnotatedElementsBecomeInvinsible(RequiredForInvisible.class);
                        return true;
                    } catch (TimeoutException e){
                        // nothing
                    }
                    return false;
                },
                d,
                Duration.ofMillis(200)
        );
    }

    @Override
    public String getName() {
        log.trace("");
        PageName annotation = this.getClass().getAnnotation(PageName.class);
        if (annotation != null) {
            return annotation.value();
        }
        String simple = this.getClass().getSimpleName();
        // Default: "SplashScreenPage" -> "SplashScreen"
        return simple.endsWith("Page")
                ? simple.substring(0, simple.length() - "Page".length())
                : simple;
    }

    // ------------------- protected -------------------------

    protected void waitForAnnotatedElementsBecomeVisible(Class<? extends Annotation> annotationType) {
        // log.info("{}", annotationType);
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationType)) {
                By locator = getBy(field);
                int timeout = getTimeout(field, annotationType);
                new WebDriverWait(driver, Duration.ofSeconds(timeout))
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));
            }
        }
    }

    protected boolean waitForAnnotatedElementsBecomeInvinsible(Class<? extends Annotation> annotationType) {
        // log.info("{}", annotationType);
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationType)) {
                By locator = getBy(field);
                int timeout = getTimeout(field, annotationType);
                new WebDriverWait(driver, Duration.ofSeconds(timeout))
                        .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            }
        }
        return true;
    }


    protected boolean allAnnotatedElementsPresent(Class<? extends Annotation> annotationType) {
        log.info("{}", annotationType);
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationType)) {
                By locator = getBy(field);
                if (driver.findElements(locator).isEmpty()) {
                    log.warn("Required Element not found. Locator [{}].", locator.toString());
                    return false;
                }
            }
        }
        return true;
    }


    // ------------------- private -------------------------

    private int getTimeout(Field field, Class<? extends Annotation> annotationType) {
        log.info("{}", annotationType);
        if (annotationType == RequiredForVisible.class) {
            return field.getAnnotation(RequiredForVisible.class).timeoutSeconds();
        } else if (annotationType == RequiredForComplete.class) {
            return field.getAnnotation(RequiredForComplete.class).timeoutSeconds();
        }
        return 10;
    }

    private By getBy(Field field) {
        try {
            field.setAccessible(true);
            return (By) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access locator field: " + field.getName(), e);
        }
    }
}

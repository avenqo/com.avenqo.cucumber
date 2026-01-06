package com.avenqo.cucumber.beapp.wait;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;

@Slf4j
public class ElementWait extends AbstractWait {

    @Autowired
    AppiumDriver driver;

    public static void seconds(int sec) {
        milliSeconds(sec * 1000);
    }

    public static void milliSeconds(int msec) {
        // driver.manage().timeouts().implicitlyWait(msec, TimeUnit.MILLISECONDS);
        log.info("ms={}", msec);

        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // ------------------- Waiting ------------------------

    /**
     * Waits until the expected condition becomes true or a timeout occurs.
     * <p>
     * <p>
     * <p>
     * public void example() throws Throwable {
     * LOGGER.info("");
     * <p>
     * Wait.until(30, new ExpectedCondition<Boolean>() {
     *
     * @param timeout_sec In seconds.
     * @param condition   Implement this interface returning true on success.
     * @param logText     The text shown by the logger output.
     * @throws Throwable
     * @Override public Boolean apply(WebDriver input) {
     * return false;
     * }
     * }, "Fehler");
     * <p>
     * assertTrue("Missing the editboxes for login", FPages.getPageMerkliste().isLoginVisible());
     * }
     */
    public void until(int timeout_sec, ExpectedCondition<Boolean> condition, String logText) throws Throwable {
        log.info(logText);

        implicitWaitClear();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout_sec)).until(condition);
        } finally {
            implicitWaitSetDefault();
        }
    }

    public void untilListSizeExceedsLimit(int timeout_sec, final List<?> al, final int limit, String logText) throws Throwable {
        log.info(logText);
        until(timeout_sec, new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return al != null && al.size() > limit;
            }
        }, logText);
    }

}

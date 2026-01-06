package com.avenqo.cucumber.beapp.widget;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.exceptions.ENotImplementedYet;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ähnlich dem 'Switch'.
 *
 * @author hko
 */
@Slf4j
@Component
public class CheckBox {

    // Attribute für Android
    private static final String CHECKED = "checked";
    // Attribute für iOS
    private static final String VALUE = "value";

    private final AppiumDriver driver;

    public CheckBox(AppiumDriver d) {
        this.driver = d;
    }

    public void switchOn(WebElement element, boolean val) throws Throwable {
        log.info("Set Checkbox [{}] to state {[]} .", element, val);
        if (val != isSwitchOn(element))
            element.click();
        else
            log.info("No handling necessary; checkbox is in the correct state [{}]yet.", val);
    }

    /**
     * @param index Zero-basded
     * @param val   True (checked) or not.
     * @return
     * @throws Throwable
     */
    public void switchOn(int index, boolean val) throws Throwable {
        log.info("Set Checkbox index [{}] to state {[]} .", index, val);
        if (val != isSwitchOn4CheckBoxClass(index))
            getCheckBoxElement(index).click();
        else
            log.info("No handling necessary; checkbox is in the correct state [{}]yet.", val);
    }


    /**
     * Looking for CheckBox classes.
     *
     * @param index Zero-based
     * @return
     * @throws Throwable
     */
    public boolean isSwitchOn4CheckBoxClass(int index) throws Throwable {
        return isSwitchOn(getCheckBoxElement(index));
    }

    public boolean isSwitchOn(WebElement element) throws Throwable {
        if (driver instanceof AndroidDriver) {
            String s = element.getAttribute(CHECKED);
            return s.toLowerCase().equals("true");
        } else {
            String s = element.getAttribute(VALUE);
            return s.toLowerCase().equals("1");
        }
    }


    /**
     * Return element based on Index
     *
     * @param index Zero-based
     * @return
     * @throws Throwable
     */
    private WebElement getCheckBoxElement(int index) throws Throwable {
        log.info("Get Checkbox index: {}", index);
        if (driver instanceof AndroidDriver) {
            List<WebElement> al = driver.findElements(By.className(IWidgets4Android.CHECKBOX));


            // List<WebElement> al = E.fastFetchAllElementsByXPath("//*");//(IWidgets4Android.CHECKBOX);
            //for (WebElement el : al)
            //	WidgetInfoHelper.showDebugInfos(el);

            if (al == null || al.size() <= index)
                throw new EInconsistencyException("Index [" + index + "] isn't valid. Found ["
                        + (al == null ? "NULL" : al.size()) + "] checkboxes only.");

            return al.get(index);
        } else {
            throw new ENotImplementedYet();
        }
    }

}

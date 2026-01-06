package com.avenqo.cucumber.beapp.selectors;

import com.avenqo.cucumber.beapp.widget.IWidgets4iOS;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class ByIos {

    /**
     * @param className     Bspw. XCUIElementTypeButton
     * @param nameContained
     * @return
     */
    public static By classWithNameContained(String className, String nameContained) {
        String s = "type == '" + className + "' AND name CONTAINS '" + nameContained + "' AND visible == TRUE";
        return AppiumBy.iOSNsPredicateString(s);
    }


    /**
     * @param className Bspw. XCUIElementTypeButton
     * @param exactName
     * @return
     */
    public static By classWithName(String className, String exactName) {
        String s = "type == '" + className + "' AND name == '" + exactName + "' AND visible == TRUE";
        return AppiumBy.iOSNsPredicateString(s);
    }


    /**
     * Für 'XCUIElementTypeButton'.
     *
     * @param name Exakter String.
     * @return
     */
    public static By buttonWithName(String name) {
        return classWithName(IWidgets4iOS.BUTTON, name);
    }


    /**
     * Für 'XCUIElementTypeButton'.
     *
     * @param name Der Name muss im Attribut 'name' enthalten sein.
     * @return
     */
    public static By buttonWithNameContained(String name) {
        return classWithNameContained(IWidgets4iOS.BUTTON, name);
    }
}

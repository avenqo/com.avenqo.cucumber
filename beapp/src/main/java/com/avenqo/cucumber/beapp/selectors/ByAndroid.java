package com.avenqo.cucumber.beapp.selectors;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class ByAndroid {

    // ---- description ----

    public static By partialDescription(String partialDescription) {
        return AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + partialDescription + "\")");
    }

    public static By classWithDescription(String className, String desc) {
        return AppiumBy.androidUIAutomator("new UiSelector().className(\"" + className + "\").description(\"" + desc + "\")");
    }


    public static By classWithPartialDescription(String className, String partialDescription) {
        return AppiumBy.androidUIAutomator("new UiSelector().className(\"" + className + "\").descriptionContains(\"" + partialDescription + "\")");
    }

    public static By buttonWithDescription(String description) {
        return AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").description(\"" + description + "\")");
    }

    // ---- class with text ----

    /**
     * Sucht nach Element mit dem enthaltenen(!) Text.
     *
     * @param className siehe IWidgets4Android, bspw. IWidgets4Android.BUTTON.
     * @param txt       Der String, der im Attribut 'Text' enthalten sein muss.
     * @return
     */
    public static By classWithTextContained(String className, String txt) {
        String s = "new UiSelector().className(\"" + className + "\").textContains(\"" + txt + "\")";
        return AppiumBy.androidUIAutomator(s);
    }

    /**
     * Sucht nach Element vom gg. Class Type mit dem exakt(!) übereinstimmenden Text.
     *
     * @param className siehe IWidgets4Android, bspw. IWidgets4Android.BUTTON.
     * @param txt       Der String, der identisch ist mit dem Attribut 'Text'.
     * @return
     */
    public static By classWithText(String className, String txt) {
        String s = "new UiSelector().className(\"" + className + "\").text(\"" + txt + "\")";
        return AppiumBy.androidUIAutomator(s);
    }

    /**
     * Sucht nach Element vom gg. Class Type mit dem exakt(!) übereinstimmenden Text.
     *
     * @param txt Der String, der identisch ist mit dem Attribut 'Text'.
     * @return
     */
    public static By buttonWithText(String txt) {
        String s = "new UiSelector().className(\"android.widget.Button\").text(\"" + txt + "\")";
        return AppiumBy.androidUIAutomator(s);
    }

    // ---- resourceId ----

    public static By classWithResourceId(String className, String resourceId) {
        String s = "new UiSelector().className(\"" + className + "\").resourceId(\"" + resourceId + "\")";
        return AppiumBy.androidUIAutomator(s);
    }


    public static By parentId_childWithText(String parentId, String childText) {
        String s = "new UiSelector().resourceId(\"" + parentId + "\").childSelector(UiSelector().text(\"" + childText + "\"))";
        return AppiumBy.androidUIAutomator(s);
    }

    public static By parentId_childClass(String parentId, String childClass) {
        String s = "new UiSelector().resourceId(\"" + parentId + "\").childSelector(UiSelector().className(\"" + childClass + "\"))";
        return AppiumBy.androidUIAutomator(s);
    }

    /**
     * @param parentId   Ressource-ID des Eltern-Elements.
     * @param childClass Class-Attribut des Kind-Elements.
     * @param childText  Text-Attribut des Kind-Elements.
     * @return
     */
    public static By parentId_childClassAndText(String parentId, String childClass, String childText) {
        String s = "new UiSelector().resourceId(\"" + parentId + "\").childSelector(UiSelector().className(\"" + childClass + "\").text(\"" + childText + "\"))";
        return AppiumBy.androidUIAutomator(s);
    }

    /**
     * @param id ressource-id
     * @param i  Zero based.
     * @return
     */
    public static By idWithIndex(String id, int i) {
        String s = "new UiSelector().resourceId(\"" + id + "\").index(" + i + ")";
        return AppiumBy.androidUIAutomator(s);
    }

    // ---- XPath ----

    /**
     * Achtung, verwendet XPath für (nicht-standard-) Attribute.
     *
     * @param className
     * @param attributeName
     * @param attributeValue Exakte Übdereinstimmung notwendig.
     * @return
     */
    public static By xpWidgetByAttributeValue(String className, String attributeName, String attributeValue) {
        //	"//android.widget.Button[@content-desc='Frauen']"
        String s = "//" + className + "[@" + attributeName + "='" + attributeValue + "']";
        return By.xpath(s);
    }


    /**
     * Achtung, verwendet XPath für (nicht-standard-) Attribute.
     *
     * @param className
     * @param attributeName
     * @param attributeValue Der Wert des Attributs muss den Text lediglich enthalten.
     * @return
     */
    public static By xpWidgetByContainedAttributeValue(String className, String attributeName, String attributeValue) {
        String s = "//" + className + "[contains(@" + attributeName + ", '" + attributeValue + "')]";
        return By.xpath(s);
    }


}

package com.avenqo.cucumber.beapp.pages;

import com.avenqo.cucumber.beapp.exceptions.ETimeout;

import java.time.Duration;

public interface IPage {

    /**
     * Ist die Seite gerade sichtbar (z.B. mindestens ein zentrales Element)?
     */
    boolean isVisible();

    /**
     * Ist die Seite „vollständig“ gerendert (alle relevanten Elemente vorhanden)?
     */
    boolean isComplete();

    /**
     * Blockiert, bis die Seite sichtbar ist (oder Timeout).
     */
    void waitUntilVisible();

    /**
     * Blockiert, bis die Seite sichtbar ist (oder Timeout).
     */
    void waitUntilVisible(Duration d) throws ETimeout;

    /**
     * Blockiert, bis die Seite nicht mehr sichtbar ist (oder Timeout).
     */
    void waitUntilInvisible();

    /**
     * Blockiert, bis die Seite nicht mehr sichtbar ist (oder Timeout).
     */
    void waitUntilInvisible(Duration d) throws ETimeout;

    public String getName();
}

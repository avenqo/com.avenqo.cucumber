package com.avenqo.cucumber.beapp.exceptions;

import org.openqa.selenium.By;

@SuppressWarnings("serial")
public class ElementNotUniqueException extends AException {

    public ElementNotUniqueException(String msg) {
        super(msg);
    }

    public ElementNotUniqueException(By by, int size) {
        super("By='" + by + "', size=" + size);
    }

    public ElementNotUniqueException(String msg, int size) {
        super("By='" + msg + "', size=" + size);
    }
}

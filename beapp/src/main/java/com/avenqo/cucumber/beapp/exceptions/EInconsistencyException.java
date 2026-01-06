package com.avenqo.cucumber.beapp.exceptions;

import org.openqa.selenium.By;

@SuppressWarnings("serial")
public class EInconsistencyException extends AException {

    public EInconsistencyException(String msg) {
        super(msg);
    }

    public EInconsistencyException(By by, int size) {
        super("By='" + by + "', size=" + size);
    }

}

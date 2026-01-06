package com.avenqo.cucumber.beapp.exceptions;


public class EConfigException extends Exception {

    public EConfigException(String txt) {
        super(txt);
    }

    public EConfigException(String txt, Object reason) {
        this("Message: [" + txt + "]\nReason: [" + reason.toString() + "]");
    }
}

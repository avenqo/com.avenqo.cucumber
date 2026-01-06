package com.avenqo.cucumber.plugins.stepviewer;

public class UserAbortException extends RuntimeException {
    public UserAbortException() { super("Testlauf manuell abgebrochen."); }
}


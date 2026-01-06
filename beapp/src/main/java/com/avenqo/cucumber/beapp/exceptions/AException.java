package com.avenqo.cucumber.beapp.exceptions;

/**
 * Just to distinguish between exceptions intentionally thrown by the code and
 * 'other' unexpected exceptions
 *
 * @author hko
 *
 */
public class AException extends Exception {

    public AException() {
        super();
    }

    public AException(String msg) {
        super(msg);
    }
}

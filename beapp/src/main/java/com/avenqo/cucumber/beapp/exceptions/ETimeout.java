package com.avenqo.cucumber.beapp.exceptions;

import java.time.Duration;

/**
 * @author hko
 */
public class ETimeout extends AException {
    public ETimeout(String conditionWasNotFulfilled, Duration timeout, Duration pollInterval) {
    }
}

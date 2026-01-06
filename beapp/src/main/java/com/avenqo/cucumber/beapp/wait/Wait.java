package com.avenqo.cucumber.beapp.wait;

import com.avenqo.cucumber.beapp.exceptions.ETimeout;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public class Wait {

    public static void until(BooleanSupplier condition,
                             Duration timeout,
                             Duration pollInterval) throws ETimeout {

        long end = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < end) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted", e);
            }
        }
        throw new ETimeout("Condition was not fulfilled", timeout,pollInterval);
    }
}

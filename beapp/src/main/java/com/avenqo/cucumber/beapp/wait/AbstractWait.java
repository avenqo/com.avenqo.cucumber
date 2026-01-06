package com.avenqo.cucumber.beapp.wait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


abstract class AbstractWait {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractWait.class);

    /**
     * Set a default value for implicit waits -> This will incluence the
     * Wait4Procedures becaus implicit wait is overriding the explicit waits.
     * @throws Throwable
     */
    @Autowired
    public static void implicitWaitSetDefault() throws Throwable {
        LOGGER.trace("");
        //	TEnv.getDriver().manage().timeouts().implicitlyWait(Timeout.STANDARD.getSeconds(), TimeUnit.SECONDS);
// TODO: reactivate
    }

    /**
     * Make implicit waits very short so that the explicit wait may work. Otherwise,
     * explicit waits would wait the implicit wait time -always.
     *
     * @throws Throwable
     */
    public static void implicitWaitClear() throws Throwable {
        LOGGER.trace("");
        // TEnv.getDriver().manage().timeouts().implicitlyWait(Timeout.SHORT.getSeconds(), TimeUnit.SECONDS);
        // TODO: reactivate
    }


}

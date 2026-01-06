package com.avenqo.cucumber.beapp.driver.appium;

import com.avenqo.cucumber.beapp.util.ProcessHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Killt alle instanzen des SafariDrivers.
 *
 * @author hko
 *
 */
@Slf4j
public class SafariDriverKiller {

    public static void perform() {
        try {
            //Alle safaridriver killen; Wenn sie im Terminal laufen, blockieren sie ansonsten den shutdown
            do {
                //darf sich selbst nicht killen und nicht die vom Programm gestartete shells
                Integer[] processNumbers = ProcessHelper.listProcesses("safaridriver", new String[]{"grep safaridriver"});
                log.info("Found {} safaridriver-processes to be killed.", processNumbers == null ? 0 : processNumbers.length);

                if (processNumbers == null || processNumbers.length == 0)
                    break;    //Nichts mehr zu killen

                ProcessHelper.killProcessNumbers(processNumbers);

                log.info("Waiting a few seconds ...");

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } while (true);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}

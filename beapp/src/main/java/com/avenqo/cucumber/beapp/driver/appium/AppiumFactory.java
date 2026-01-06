package com.avenqo.cucumber.beapp.driver.appium;

import com.avenqo.cucumber.beapp.util.FileHelper;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Stellt einen laufenden Appium Server sicher a) Entweder ist Appium bereits
 * gestartet UND der Server ebenso auf default URL:
 * http://127.0.0.1:4723/status, ODER b) es wird versucht, den
 * AppiumServer direkt zu starten (Erfordert installiertes Appium).
 *
 * @author hko
 */
@Slf4j
public class AppiumFactory {

    private final static String APPIUM_DESKTOP_DEFAULT_URL = "http://127.0.0.1:4723/status";

    private final static String serverAdress = "127.0.0.1";

    private final static String APPIUM_LOG_FILE_DIR = "./log";
    private final static String APPIUM_LOG_FILE_NAME_PRE = "appium_";

    private static String currentAppiumUrl = null;
    private String path4log = null;

    // ================== API ====================

    /**
     * @param deviceName Name des Device-under-Test, der zur Unterscheidung der
     *                   Appium-Log-Files dient
     * @throws Throwable
     */
    public AppiumFactory(String deviceName) throws Throwable {
        log.info("deviceName [{}]", deviceName);
        FileHelper.checkDestPathAndCreateIfNecessary(APPIUM_LOG_FILE_DIR);

        path4log = APPIUM_LOG_FILE_DIR + "/" + APPIUM_LOG_FILE_NAME_PRE
                + (deviceName != null && deviceName.length() > 0 ? deviceName
                : RandomStringUtils.random(15, true, true))
                + ".log";
        log.info("Appium is logged in '{}'.", path4log);
    }

    /**
     * Überprüft, ob Appium Desktop auf der Default-URL (http://127.0.0.1:4723)
     * läuft. Wenn nicht, wird versucht, den Service lokal zu starten, auf irgend
     * einem freien Port.
     *
     * @return URL des Servers
     * @throws Throwable Wenn der Appium-Service nicht gestartet werden konnte.
     */
    public String ensureAppiumIsRunning() throws Throwable {
        log.info("");
        if (currentAppiumUrl == null) {
            log.info("Start to determine current appium server");

            String strServerUrl = null;
            if (!isAppiumRunning(APPIUM_DESKTOP_DEFAULT_URL)) {
                AppiumDriverLocalService adls = startNewAppiumService();
                strServerUrl = adls.getUrl().toExternalForm();
            } else
                strServerUrl = APPIUM_DESKTOP_DEFAULT_URL;

            // http://127.0.0.1:4723/status -> das '/status' entfernen
            int pos = strServerUrl.indexOf("/status");
            if (pos > -1) {
                strServerUrl = strServerUrl.substring(0, pos);
            }

            currentAppiumUrl = strServerUrl;
            log.info("Using Appium Server at '{}'.", currentAppiumUrl);
        }
        return currentAppiumUrl;
    }

    // ================== intern ====================

    private boolean isAppiumRunning(String strUrl) throws ClientProtocolException, IOException {

        log.info("Appium request: '{}'.", strUrl);

        boolean bRet = false;
        // Create Object and pass the url
        HttpUriRequest request = new HttpGet(strUrl);

        try {
            // send the response or execute the request
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            Object httpStatus = httpResponse.getStatusLine().getStatusCode();
            if (httpStatus.equals(HttpStatus.SC_OK)) { // Verify the response code is equal to 200

                // {"value":{"build":{"version":"1.15.1"}},"sessionId":null,"status":0}
                String result = EntityUtils.toString(httpResponse.getEntity());
                log.info("AppiumServer HttpResponse: '{}'.", result);

                // Convert the result as a String to a JSON object
                JSONObject jo = new JSONObject(result);

                Object valueObject = jo.get("value");
                if (valueObject instanceof JSONObject) {
                    valueObject = ((JSONObject) valueObject).get("build");
                    if (valueObject instanceof JSONObject) {
                        valueObject = ((JSONObject) valueObject).get("version");
                        log.info("Appium Version: '{}'.", valueObject);
                        bRet = true;
                    }
                }
            }

        } catch (org.apache.http.conn.HttpHostConnectException hhce) {
            log.info("Http connection refused.");
        }
        log.info("Checking AppiumServer returned: '{}'.", bRet);
        return bRet;
    }


    private AppiumDriverLocalService startNewAppiumService() throws Throwable {
        log.info("Starting Appium Service, logging to '{}', ...", path4log);
        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(

                new AppiumServiceBuilder().withIPAddress(serverAdress).usingAnyFreePort()

                        // ------ Logfile creation ------
                        .withLogFile(new File(path4log)).withArgument(GeneralServerFlag.DEBUG_LOG_SPACING)
                        // .withArgument(GeneralServerFlag.LOG_LEVEL, "info:info")
                        .withArgument(GeneralServerFlag.LOG_LEVEL, "warn")

                // ------ END: Logfile creation ------
        );

        log.info("... using URL='{}'", service.getUrl().toString());
        service.start();
        log.info("Appium Service startet");
        return service; // wozu?
    }

}

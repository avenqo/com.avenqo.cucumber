package com.avenqo.cucumber.beapp.aut;

import com.avenqo.cucumber.beapp.driver.AbstractMapFromInputStreamProvider;
import io.appium.java_client.android.options.app.SupportsAppPackageOption;
import io.appium.java_client.ios.options.app.SupportsBundleIdOption;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

/**
 * It partially provides those capabilities being responsible for package and main activity definition.
 */
@Slf4j
public class AutProvider extends AbstractMapFromInputStreamProvider {

    // use this property to define the AUT (application under test)
    private final static String PROP_NAME = "com.avenqo.beapp.aut";
    // path to the root of the AUT definition files
    private final static String PATH_LOC = "aut/";

    private static final String APPIUM = "appium:";
    private final static String CAP_APP_PACKAGE = APPIUM + SupportsAppPackageOption.APP_PACKAGE_OPTION;
    private final static String CAP_BUNDLE_ID = APPIUM + SupportsBundleIdOption.BUNDLE_ID_OPTION;
    private static AutProvider instance;

    // ----------- Singleton -----------
    private Capabilities appCapabilities;

    private AutProvider() {
        super(PATH_LOC);
    }

    public static AutProvider instance() {
        if (instance == null) {
            log.info("");
            instance = new AutProvider();
            instance.init();
        }
        return instance;
    }

    // ============= Methods =============

    public Capabilities getCapabilities() {
        return appCapabilities;
    }

    public String getBundleId() {

        var m = appCapabilities.asMap();
        // Android
        if (m.containsKey(CAP_APP_PACKAGE))
            return (String) m.get(CAP_APP_PACKAGE);
        // iOS
        if (m.containsKey(CAP_BUNDLE_ID))
            return (String) m.get(CAP_BUNDLE_ID);

        throw new RuntimeException("Aborting. Missing BundleID / App Package.");
    }

    // ----------- Protected -----------

    @Override
    protected String getFileNameWithoutExtension() {
        var fileName = System.getProperty(PROP_NAME);

        if (fileName == null || fileName.length() == 0) {
            throw new RuntimeException("Aborting. Missing property '" + PROP_NAME + "'.");
        }
        return fileName;
    }

    // ----------- Private -----------

    private void init() {
        log.info("");
        Map<String, ?> config = super.createMapFromJson();
        appCapabilities = new MutableCapabilities(config);
    }
}

package com.avenqo.cucumber.beapp.device;

import com.avenqo.cucumber.beapp.driver.AbstractMapFromInputStreamProvider;
import com.avenqo.cucumber.beapp.exceptions.ENotImplementedYet;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Map;

/**
 * Provides device bound Capabilities based on a JSON file defining a specific
 * device.
 */
@Slf4j
public class DeviceProvider extends AbstractMapFromInputStreamProvider {

    // ---- const ----

    // use this property to define the device
    private final static String PROP_NAME = "com.avenqo.beapp.device";
    // path to the root of the device definition files
    private final static String PATH_LOC = "devices/";
    private static final String APPIUM_CAP_DEVICENAME = "appium:deviceName";

    private static final String KEY_PHONE = "phoneNumber";
    private static final String KEY_PINCODE = "pinCode";
    private static final Object KEY_APPIUM = "appium";

    private Capabilities capabilities;
    private String phoneNumber;

    // ----------- Singleton -----------
    private String pinCode;

    public DeviceProvider() {
        super(PATH_LOC);
        initCapabilities();
    }

    // ============= Methods =============

    // ----------- Public -----------

    public boolean isAndroid() {
        return capabilities instanceof UiAutomator2Options;
    }

    public boolean isIOS() {
        return capabilities instanceof XCUITestOptions;
    }

    public String getUdid() throws ENotImplementedYet {
        throw new ENotImplementedYet();
    }

    public Capabilities getDeviceCapabilities() {
        return capabilities;
    }

    /**
     * @return The name of the device based on a) PROP_NAME or (if provided) b)
     * appium:deviceName:
     * <p>
     * Option b) overwrites a)
     */
    public String getDeviceName() {
        var deviceName = System.getProperty(PROP_NAME);

        if (deviceName == null || deviceName.length() == 0) {
            throw new RuntimeException("Aborting. Missing property '" + PROP_NAME + "'.");
        }

        var map = capabilities.asMap();
        // SupportsDeviceNameOption.DEVICE_NAME_OPTION

        // Overwrite device name by capability 'appium:deviceName'
        if (map.containsKey(APPIUM_CAP_DEVICENAME)) {
            Object d2 = map.get(APPIUM_CAP_DEVICENAME);

            if (d2 instanceof String) {
                String dStr = (String) d2;
                if (!dStr.isEmpty()) {
                    log.info("Device Name was overwritten by Appium Capability '{}'.", APPIUM_CAP_DEVICENAME);
                    deviceName = dStr;
                }
            }
        }

        log.info("Device Name is '{}'.", APPIUM_CAP_DEVICENAME);
        return deviceName;
    }

    public Object getCapability(String name) {
        return capabilities.getCapability(name);
    }

    @Override
    public String getFileNameWithoutExtension() {
        var deviceName = System.getProperty(PROP_NAME);

        if (deviceName == null || deviceName.length() == 0) {
            throw new RuntimeException("Aborting. Missing property '" + PROP_NAME + "'.");
        }
        return deviceName;
    }

    // ----------- Private -----------

    /**
     * Initializes the capabilities. Checks if platformName property is found and
     * supported.
     */
    private void initCapabilities() {
        try {
            Map<String, ?> m = super.createMapFromJson();

            phoneNumber = getString(m, KEY_PHONE);
            pinCode = getString(m, KEY_PINCODE);

            Map<String, ?> config = (Map<String, ?>) m.get(KEY_APPIUM);


            // determine platform
            if (!config.keySet().contains(CapabilityType.PLATFORM_NAME)) {
                throw new RuntimeException("Missing property '" + CapabilityType.PLATFORM_NAME + "'; Device is '"
                        + System.getProperty(PROP_NAME) + "'.");
            }

            String platformName = ((String) config.get(CapabilityType.PLATFORM_NAME)).toUpperCase();

            // initialize capabilities
            if (Platform.ANDROID.name().equals(platformName)) {
                UiAutomator2Options options = new UiAutomator2Options(config);
                capabilities = options;
            } else if (Platform.IOS.name().equals(platformName)) {
                var options = new XCUITestOptions(config);
                capabilities = options;
                options.setUpdatedWdaBundleId("io.appium.WebDriverAgentRunner");
            } else {
                throw new RuntimeException("Aborting: Platform '" + CapabilityType.PLATFORM_NAME + "' not supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Aborting. See stacktrace before.");
        }
    }


    private String getString(Map<String, ?> map, String key) {
        if (map.containsKey(key)) {
            var o = map.get(key);
            if (o instanceof String) {
                String v = (String) o;
                log.info("Key: {}, Value: {}", key, v);
                return v;
            }
        }
        return null;
    }
}

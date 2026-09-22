package com.carcomfort.core.driver;

import com.carcomfort.core.config.FrameworkConfig;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;

/**
 * Physical iPhone driver lifecycle (Appium XCUITest + WebDriverAgent).
 *
 * <p>Prepared for future real-device iOS automation. NOT active until a physical
 * iPhone, macOS/Xcode signing, and WDA provisioning are available. Never assumes Simulator.
 * Thread-safe via ThreadLocal; shares the device-lock contract with Android.
 */
public final class IosDriverManager implements DriverManager<IOSDriver> {
    private static final Logger log = LoggerFactory.getLogger(IosDriverManager.class);

    private final ThreadLocal<IOSDriver> driverThreadLocal = new ThreadLocal<>();

    @Override
    public IOSDriver getDriver() {
        IOSDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "iOS driver not initialized. Physical iPhone automation is not yet enabled.");
        }
        return driver;
    }

    @Override
    public void quitDriver() {
        IOSDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Error quitting iOS driver", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    @Override
    public boolean isSessionActive() {
        IOSDriver driver = driverThreadLocal.get();
        if (driver == null) {
            return false;
        }
        try {
            driver.getSessionId();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initializes a physical iPhone session. Requires IOS_DEVICE_UDID and signing config.
     */
    public IOSDriver initialize(String udid) {
        if (udid == null || udid.isBlank()) {
            throw new IllegalStateException(
                    "Physical iPhone UDID required (IOS_DEVICE_UDID). Simulators are not supported.");
        }
        try {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setAutomationName("XCUITest");
            options.setUdid(udid);
            options.setDeviceName(FrameworkConfig.getString("carcomfort.ios.capabilities.deviceName", udid));
            options.setNoReset(true);
            options.setNewCommandTimeout(Duration.ofSeconds(300));
            FrameworkConfig.getOptionalString("carcomfort.ios.app.bundleId")
                    .filter(s -> !s.isBlank())
                    .ifPresent(options::setBundleId);
            FrameworkConfig.getOptionalString("carcomfort.ios.device.xcodeOrgId")
                    .filter(s -> !s.isBlank())
                    .ifPresent(v -> options.setCapability("xcodeOrgId", v));

            String host = FrameworkConfig.getString("carcomfort.appium.server.host", "127.0.0.1");
            int port = FrameworkConfig.getInt("carcomfort.appium.server.port", 4723);
            String path = FrameworkConfig.getString("carcomfort.appium.server.path", "/wd/hub");
            URL serverUrl = new URL("http", host, port, path);

            log.info("Creating XCUITest session for physical iPhone: {}", udid);
            IOSDriver driver = new IOSDriver(serverUrl, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            driverThreadLocal.set(driver);
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize iOS driver for UDID: " + udid, e);
        }
    }
}

package com.carcomfort.core.driver;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.core.device.DeviceManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Android driver lifecycle management with device locking.
 * Thread-safe using ThreadLocal for parallel execution isolation.
 */
public final class AndroidDriverManager implements DriverManager<AndroidDriver> {
    private static final Logger log = LoggerFactory.getLogger(AndroidDriverManager.class);

    private final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<DeviceInfo> deviceThreadLocal = new ThreadLocal<>();
    private final DeviceLockManager lockManager;
    private final DeviceManager deviceManager;
    private final AppiumServerManager appiumServerManager;
    private volatile boolean sessionInitialized = false;

    public AndroidDriverManager(DeviceLockManager lockManager, DeviceManager deviceManager, AppiumServerManager appiumServerManager) {
        this.lockManager = lockManager;
        this.deviceManager = deviceManager;
        this.appiumServerManager = appiumServerManager;
    }

    @Override
    public AndroidDriver getDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("Android driver not initialized for current thread. Call initialize() first.");
        }
        return driver;
    }

    @Override
    public void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        DeviceInfo device = deviceThreadLocal.get();

        if (driver != null) {
            try {
                log.info("Quitting Android driver for device: {}", device != null ? device.udid() : "unknown");
                driver.quit();
            } catch (Exception e) {
                log.warn("Error quitting Android driver", e);
            } finally {
                driverThreadLocal.remove();
            }
        }

        if (device != null) {
            lockManager.releaseLock(device);
            deviceThreadLocal.remove();
        }

        sessionInitialized = false;
    }

    @Override
    public boolean isSessionActive() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver == null) return false;

        try {
            driver.getSessionId();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AndroidDriver initialize() {
        return initialize(Optional.empty());
    }

    public AndroidDriver initialize(Optional<String> preferredUdid) {
        if (sessionInitialized && isSessionActive()) {
            log.debug("Reusing existing Android driver session");
            return getDriver();
        }

        log.info("Initializing Android driver...");

        if (!appiumServerManager.isRunning()) {
            appiumServerManager.start();
        }

        DeviceInfo device = deviceManager.selectAndroidDevice(preferredUdid.orElse(null))
                .orElseThrow(() -> new IllegalStateException("No suitable Android device found"));

        if (!deviceManager.acquireDevice(device)) {
            throw new IllegalStateException("Failed to acquire device lock for: " + device.udid());
        }

        UiAutomator2Options options = buildOptions(device);
        URL serverUrl = appiumServerManager.getServerUrl();

        try {
            log.info("Creating Appium session for device: {} ({})", device.deviceName(), device.udid());
            AndroidDriver driver = new AndroidDriver(serverUrl, options);

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

            driverThreadLocal.set(driver);
            deviceThreadLocal.set(device);
            sessionInitialized = true;

            log.info("Android driver initialized successfully. Session ID: {}", driver.getSessionId());
            return driver;

        } catch (Exception e) {
            lockManager.releaseLock(device);
            deviceThreadLocal.remove();
            throw new RuntimeException("Failed to initialize Android driver", e);
        }
    }

    public DeviceInfo getCurrentDevice() {
        return deviceThreadLocal.get();
    }

    private UiAutomator2Options buildOptions(DeviceInfo device) {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setAutomationName(FrameworkConfig.getString("carcomfort.appium.capabilities.automationName"));
        options.setPlatformName(FrameworkConfig.getString("carcomfort.appium.capabilities.platformName"));
        options.setDeviceName(device.deviceName());
        options.setUdid(device.udid());
        options.setPlatformVersion(device.osVersion());

        String appPackage = FrameworkConfig.getOptionalString("carcomfort.appium.capabilities.appPackage")
                .orElse(FrameworkConfig.getString("carcomfort.android.app.package"));
        String appActivity = FrameworkConfig.getOptionalString("carcomfort.appium.capabilities.appActivity")
                .orElse(FrameworkConfig.getString("carcomfort.android.app.activity"));

        if (appPackage != null && !appPackage.isBlank()) {
            options.setAppPackage(appPackage);
        }
        if (appActivity != null && !appActivity.isBlank()) {
            options.setAppActivity(appActivity);
        }

        options.setNoReset(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.noReset"));
        options.setFullReset(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.fullReset"));
        options.setAutoGrantPermissions(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.autoGrantPermissions"));
        options.setNewCommandTimeout(Duration.ofSeconds(FrameworkConfig.getLong("carcomfort.appium.capabilities.newCommandTimeout")));
        // options.setUnicodeKeyboard(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.unicodeKeyboard")); // Not available in newer version
        // options.setResetKeyboard(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.resetKeyboard")); // Not available in newer version
        options.setEnsureWebviewsHavePages(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.ensureWebviewsHavePages"));
        options.setNativeWebScreenshot(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.nativeWebScreenshot"));
        options.setIgnoreHiddenApiPolicyError(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.ignoreHiddenApiPolicyError"));
        options.setDisableWindowAnimation(FrameworkConfig.getBoolean("carcomfort.appium.capabilities.disableWindowAnimation"));

        int mjpegPort = FrameworkConfig.getInt("carcomfort.appium.capabilities.mjpegServerPort");
        if (mjpegPort > 0) {
            options.setMjpegServerPort(mjpegPort);
            // options.setMjpegServerScreenshotQuality(FrameworkConfig.getInt("carcomfort.appium.capabilities.mjpegServerScreenshotQuality")); // Not available in newer version
            // options.setMjpegServerFramerate(FrameworkConfig.getInt("carcomfort.appium.capabilities.mjpegServerFramerate")); // Not available in newer version
        }

        return options;
    }

    public void launchApp() {
        AndroidDriver driver = getDriver();
        String appPackage = FrameworkConfig.getString("carcomfort.android.app.package");
        driver.activateApp(appPackage);
        log.debug("App launched: {}", appPackage);
    }

    public void terminateApp() {
        AndroidDriver driver = getDriver();
        String appPackage = FrameworkConfig.getString("carcomfort.android.app.package");
        driver.terminateApp(appPackage);
        log.debug("App terminated: {}", appPackage);
    }

    public void restartApp() {
        terminateApp();
        launchApp();
    }

    public void backgroundApp(Duration duration) {
        AndroidDriver driver = getDriver();
        driver.runAppInBackground(duration);
        log.debug("App backgrounded for {}", duration);
    }

    public boolean isAppInstalled() {
        AndroidDriver driver = getDriver();
        String appPackage = FrameworkConfig.getString("carcomfort.android.app.package");
        return driver.isAppInstalled(appPackage);
    }

    public void installApp(String appPath) {
        AndroidDriver driver = getDriver();
        driver.installApp(appPath);
        log.info("App installed from: {}", appPath);
    }

    public void removeApp() {
        AndroidDriver driver = getDriver();
        String appPackage = FrameworkConfig.getString("carcomfort.android.app.package");
        driver.removeApp(appPackage);
        log.info("App removed: {}", appPackage);
    }
}
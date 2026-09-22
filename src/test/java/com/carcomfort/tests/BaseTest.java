package com.carcomfort.tests;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.core.device.DeviceManager;
import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.driver.AppiumServerManager;
import com.carcomfort.core.driver.WebDriverManager;
import com.carcomfort.core.assertions.SafeAssertions;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.evidence.EvidenceCollector.EvidenceItem;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.testdata.TestDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Base test class providing common setup/teardown for all tests.
 *
 * <p>Suite-scoped resources are static so they are shared across all test-class
 * instances (TestNG instantiates each test class separately; instance fields set
 * in {@code @BeforeSuite} would otherwise be null in sibling classes).
 */
public abstract class BaseTest {
    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private static final Object SUITE_LOCK = new Object();
    private static volatile boolean suiteInitialized = false;

    protected static DeviceLockManager deviceLockManager;
    protected static DeviceManager deviceManager;
    protected static AppiumServerManager appiumServerManager;
    protected static AndroidDriverManager androidDriverManager;
    protected static WebDriverManager customerWebDriverManager;
    protected static WebDriverManager adminWebDriverManager;
    protected static EvidenceCollector evidenceCollector;
    protected static TestLogger suiteLogger;
    protected static ReportEngine reportEngine;
    protected static PdfReportGenerator pdfReportGenerator;
    protected static TestDataManager testDataManager;

    protected TestLogger testLogger;
    protected SafeAssertions assertions;

    protected DeviceInfo currentDevice;
    protected LocalDateTime testStartTime;
    protected String currentTestName;
    protected String currentTestClass;

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        synchronized (SUITE_LOCK) {
            if (suiteInitialized) {
                return;
            }
            log.info("=== SUITE SETUP START ===");
            FrameworkConfig.getExecutionId(); // Initialize config

            deviceLockManager = new DeviceLockManager();
            deviceLockManager.start();

            deviceManager = new DeviceManager(deviceLockManager);
            deviceManager.startAdbServer(); // idempotent; never killed here (shared host resource)

            appiumServerManager = new AppiumServerManager();
            androidDriverManager = new AndroidDriverManager(deviceLockManager, deviceManager, appiumServerManager);

            customerWebDriverManager = new WebDriverManager("customer");
            adminWebDriverManager = new WebDriverManager("admin");

            evidenceCollector = new EvidenceCollector();
            testDataManager = new TestDataManager();

            String suiteExecutionId = FrameworkConfig.getExecutionId();
            suiteLogger = new TestLogger("Suite", suiteExecutionId);
            reportEngine = new ReportEngine(evidenceCollector, suiteLogger);
            pdfReportGenerator = new PdfReportGenerator(evidenceCollector);
            pdfReportGenerator.startExecution();

            suiteInitialized = true;
            log.info("=== SUITE SETUP COMPLETE ===");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        synchronized (SUITE_LOCK) {
            if (!suiteInitialized) {
                return;
            }
            log.info("=== SUITE TEARDOWN START ===");

            try {
                pdfReportGenerator.endExecution();
                Path pdfPath = pdfReportGenerator.generateReport();
                log.info("PDF report generated: {}", pdfPath);
            } catch (Exception e) {
                log.warn("PDF report generation failed", e);
            }

            try {
                reportEngine.flush();
            } catch (Exception e) {
                log.warn("Extent flush failed", e);
            }

            if (androidDriverManager != null) {
                androidDriverManager.quitDriver();
            }
            if (customerWebDriverManager != null) {
                customerWebDriverManager.quitDriver();
            }
            if (adminWebDriverManager != null) {
                adminWebDriverManager.quitDriver();
            }

            if (appiumServerManager != null) {
                appiumServerManager.stop();
            }

            if (deviceLockManager != null) {
                deviceLockManager.stop();
            }

            suiteInitialized = false;
            log.info("=== SUITE TEARDOWN COMPLETE ===");
        }
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"deviceUdid"})
    public void testSetup(Method method, @Optional String deviceUdid) {
        currentTestName = method.getName();
        currentTestClass = getClass().getSimpleName();
        testStartTime = LocalDateTime.now();

        testLogger = new TestLogger(currentTestClass, currentTestName);
        testLogger.testStart();

        assertions = new SafeAssertions(evidenceCollector, testLogger, androidDriverManager);

        evidenceCollector.clearCurrentTestEvidence();

        log.info("=== TEST START: {}.{} ===", currentTestClass, currentTestName);
    }

    @AfterMethod(alwaysRun = true)
    public void testTeardown(ITestResult result) {
        long durationMs = ChronoUnit.MILLIS.between(testStartTime, LocalDateTime.now());
        String status = getStatusString(result.getStatus());

        testLogger.testEnd(status, durationMs);

        reportEngine.assignCategory(currentTestName, getTestGroups(result).toArray(new String[0]));
        reportEngine.assignAuthor(currentTestName, FrameworkConfig.getString("carcomfort.deviceLock.ownerId"));

        if (currentDevice != null) {
            reportEngine.assignDevice(currentTestName, currentDevice.deviceName());
        }

        if (result.isSuccess()) {
            reportEngine.logPass(currentTestName, "Test passed");
        } else if (result.getStatus() == ITestResult.SKIP) {
            reportEngine.logSkip(currentTestName, "Test skipped: " + getSkipReason(result));
        } else {
            Throwable throwable = result.getThrowable();
            String errorMsg = throwable != null ? throwable.getMessage() : "Unknown error";
            reportEngine.logFail(currentTestName, errorMsg, throwable);

            evidenceCollector.captureScreenshotAndPageSource("failure_" + currentTestName, androidDriverManager);
        }

        List<EvidenceItem> evidence = evidenceCollector.getCurrentTestEvidence();
        for (EvidenceItem item : evidence) {
            if (item.type() == EvidenceItem.Type.SCREENSHOT) {
                reportEngine.addScreenshot(currentTestName, item.path().toString(), item.name());
            }
        }

        pdfReportGenerator.recordTestResult(
                currentTestName,
                currentTestClass,
                status,
                durationMs,
                result.getThrowable() != null ? result.getThrowable().getMessage() : null,
                evidence,
                getTestGroups(result)
        );

        if (androidDriverManager != null && androidDriverManager.isSessionActive()) {
            androidDriverManager.quitDriver();
        }
        if (customerWebDriverManager != null && customerWebDriverManager.isSessionActive()) {
            customerWebDriverManager.quitDriver();
        }
        if (adminWebDriverManager != null && adminWebDriverManager.isSessionActive()) {
            adminWebDriverManager.quitDriver();
        }

        log.info("=== TEST END: {}.{} | Status: {} | Duration: {}ms ===",
                currentTestClass, currentTestName, status, durationMs);
    }

    protected void initializeAndroidDriver() {
        initializeAndroidDriver(null);
    }

    protected void initializeAndroidDriver(String preferredUdid) {
        currentDevice = deviceManager.selectAndroidDevice(preferredUdid)
                .orElseThrow(() -> new IllegalStateException("No Android device available"));

        if (!deviceManager.acquireDevice(currentDevice)) {
            throw new IllegalStateException("Failed to acquire device: " + currentDevice.udid());
        }

        androidDriverManager.initialize(java.util.Optional.ofNullable(preferredUdid));
        reportEngine.setDeviceInfo(currentDevice);

        String appPackage = FrameworkConfig.getString("carcomfort.android.app.package");
        String appVersion = "Unknown";
        try {
            Object out = androidDriverManager.getDriver().executeScript("mobile: shell", java.util.Map.of(
                    "command", "dumpsys",
                    "args", java.util.List.of("package", appPackage)));
            java.util.regex.Matcher m =
                    java.util.regex.Pattern.compile("versionName=(\\S+)").matcher(String.valueOf(out));
            if (m.find()) {
                appVersion = m.group(1);
            }
        } catch (Exception ignored) {}
        reportEngine.setAppInfo(appPackage, appVersion);
        pdfReportGenerator.setDeviceInfo(currentDevice);
        pdfReportGenerator.setAppInfo(appPackage, appVersion);
    }

    protected void initializeCustomerWebDriver() {
        customerWebDriverManager.initialize();
        customerWebDriverManager.navigateToBaseUrl();
    }

    protected void initializeAdminWebDriver() {
        adminWebDriverManager.initialize();
        adminWebDriverManager.navigateToBaseUrl();
    }

    protected void assertBusinessRule(boolean condition, String message) {
        if (assertions != null) {
            assertions.assertTrue(condition, message);
        }
    }

    protected void assertBusinessRuleEquals(Object actual, Object expected, String message) {
        if (assertions != null) {
            assertions.assertEquals(actual, expected, message);
        }
    }

    protected void finalizeAssertions() {
        if (assertions != null) {
            assertions.assertAll();
        }
    }

    private String getStatusString(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "FAILED";
            case ITestResult.STARTED -> "RUNNING";
            default -> "UNKNOWN";
        };
    }

    private String getSkipReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable != null ? throwable.getMessage() : "No reason provided";
    }

    private java.util.List<String> getTestGroups(ITestResult result) {
        return java.util.Arrays.asList(result.getMethod().getGroups());
    }
}

package com.carcomfort.flows;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.assertions.SafeAssertions;
import com.carcomfort.core.waits.WaitStrategies;
import com.carcomfort.core.waits.AndroidWaitStrategies;
import com.carcomfort.core.guards.GatedTestGuard;
import com.carcomfort.core.testdata.TestDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base business flow - composes screens, components, and provides
 * high-level business operations for tests.
 */
public abstract class BaseBusinessFlow {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final AndroidDriverManager driverManager;
    protected final WaitStrategies waits;
    protected final AndroidWaitStrategies androidWaits;
    protected final EvidenceCollector evidenceCollector;
    protected final TestLogger testLogger;
    protected final ReportEngine reportEngine;
    protected final PdfReportGenerator pdfReportGenerator;
    protected final SafeAssertions assertions;
    protected final GatedTestGuard gatedGuard;
    protected final TestDataManager testDataManager;

    protected BaseBusinessFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager
    ) {
        this.driverManager = driverManager;
        this.waits = new WaitStrategies();
        this.androidWaits = new AndroidWaitStrategies(driverManager);
        this.evidenceCollector = evidenceCollector;
        this.testLogger = testLogger;
        this.reportEngine = reportEngine;
        this.pdfReportGenerator = pdfReportGenerator;
        this.assertions = new SafeAssertions(evidenceCollector, testLogger, driverManager);
        this.gatedGuard = new GatedTestGuard();
        this.testDataManager = testDataManager;
    }

    protected void verifyGated(String testName, String... categories) {
        gatedGuard.verifyAuthorization(testName, categories);
    }

    protected boolean isGated(String... categories) {
        return gatedGuard.isGated(categories);
    }

    protected void logBusinessCheckpoint(String checkpoint, String details) {
        testLogger.businessCheckpoint(checkpoint, details);
    }

    protected void logAction(String action, String element, String details) {
        testLogger.action(action, element, details);
    }

    protected void captureCheckpointEvidence(String name) {
        evidenceCollector.captureScreenshotAndPageSource(name, driverManager);
    }

    protected void assertBusinessRule(boolean condition, String message) {
        assertions.assertTrue(condition, message);
    }

    protected void assertBusinessRuleEquals(Object actual, Object expected, String message) {
        assertions.assertEquals(actual, expected, message);
    }

    protected void finalizeAssertions() {
        assertions.assertAll();
    }

    /** Entry points the app can settle on after (re)start with persisted state. */
    public enum EntryState { ROLE, LOGIN, HOME_CUSTOMER, HOME_PROVIDER }

    /**
     * Clears transient UI state without touching app data: BACK dismisses any
     * keyboard/autofill overlay left by earlier runs (open overlays blind
     * UiAutomator2 multi-window lookup — verified), then a process restart
     * returns the app to a deterministic entry point (a persisted login restores
     * straight to HOME).
     */
    protected void settleDeviceState() {
        try {
            driverManager.getDriver().navigate().back();
        } catch (Exception ignored) {
        }
        try {
            driverManager.getDriver().navigate().back();
        } catch (Exception ignored) {
        }
        driverManager.restartApp();
        logBusinessCheckpoint("APP_RESTART", "App restarted to deterministic entry state (no data cleared)");
    }

    /**
     * Polls (main-thread driver reference, since Awaitility polls off-thread and
     * the driver is ThreadLocal) for the role screen, login screen, or
     * authenticated home for up to 30s. Cold starts may show a splash plus
     * first-launch network init first.
     */
    protected EntryState awaitEntryState() {
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        org.openqa.selenium.By roleTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Select your role");
        org.openqa.selenium.By loginTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Welcome to Car Comfort");
        org.openqa.selenium.By homeCustomerTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Enter Service Details");
        org.openqa.selenium.By homeProviderTitle =
                com.carcomfort.mobile.android.LocatorFactory.uiAutomator(
                        "new UiSelector().descriptionContains(\"Driver!\")");
        final EntryState[] found = {null};
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(30))
                    .pollInterval(java.time.Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(() -> {
                        if (!driver.findElements(homeCustomerTitle).isEmpty()) {
                            found[0] = EntryState.HOME_CUSTOMER;
                            return true;
                        }
                        if (!driver.findElements(homeProviderTitle).isEmpty()) {
                            found[0] = EntryState.HOME_PROVIDER;
                            return true;
                        }
                        if (!driver.findElements(loginTitle).isEmpty()) {
                            found[0] = EntryState.LOGIN;
                            return true;
                        }
                        if (!driver.findElements(roleTitle).isEmpty()) {
                            found[0] = EntryState.ROLE;
                            return true;
                        }
                        return false;
                    });
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            throw new IllegalStateException("No known entry state (role/login/home) within 30s");
        }
        return found[0];
    }
}
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

    protected void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.diagnostics.FailureClassifier;
import com.carcomfort.core.guards.GatedTestGuard;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * Safe framework-behavior tests: gated guard, locator policy, failure classification.
 * No device required; all assertions are deterministic.
 */
public class FrameworkSafetyTest extends BaseTest {

    @Test(groups = {"smoke"})
    public void testGatedGuardBlocksWithoutAuthorization() {
        GatedTestGuard guard = new GatedTestGuard();
        boolean blocked = false;
        try {
            guard.verifyAuthorization("SAFE_PROBE", "GATED", "PAYMENT_SETUP");
        } catch (GatedTestGuard.GatedTestException e) {
            blocked = true;
        }
        // If env already authorizes gated runs, the guard must let it through instead.
        String runGated = System.getProperty("RUN_GATED_TESTS", System.getenv("RUN_GATED_TESTS"));
        if ("true".equalsIgnoreCase(runGated)) {
            assertBusinessRule(!blocked, "Guard authorizes when RUN_GATED_TESTS=true with case");
        } else {
            assertBusinessRule(blocked, "Guard blocks gated test without authorization");
        }
        assertBusinessRule(guard.isGated("GATED", "PAYMENT_SETUP"), "Gated categories detected");
        assertBusinessRule(!guard.isGated("smoke", "regression"), "Normal groups not gated");
        finalizeAssertions();
    }

    @Test(groups = {"smoke"})
    public void testLocatorPriorityFactories() {
        assertBusinessRule(LocatorFactory.accessibilityId("loginBtn") != null, "accessibility-id factory works");
        assertBusinessRule(LocatorFactory.resourceId("com.app:id/login") != null, "resource-id factory works");
        assertBusinessRule(LocatorFactory.stableText("Book Now") != null, "stable-text factory works");
        assertBusinessRule(
                LocatorFactory.uiAutomatorText("Book Now") != null, "uiautomator factory works");
        assertBusinessRule(
                LocatorFactory.constrainedXPath("//android.widget.Button[@content-desc='book']") != null,
                "constrained XPath accepted when scoped");
        boolean rejected = false;
        try {
            LocatorFactory.constrainedXPath("//*");
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        assertBusinessRule(rejected, "Unconstrained XPath rejected");
        finalizeAssertions();
    }

    @Test(groups = {"smoke"})
    public void testConfigPlaceholderResolution() {
        System.setProperty("CARCOMFORT_SMOKE_PROBE", "probe-value");
        try {
            assertBusinessRuleEquals(
                    com.carcomfort.core.config.FrameworkConfig.resolvePlaceholders("${CARCOMFORT_SMOKE_PROBE}"),
                    "probe-value", "System property placeholder resolves");
            assertBusinessRuleEquals(
                    com.carcomfort.core.config.FrameworkConfig.resolvePlaceholders("${CARCOMFORT_SMOKE_MISSING:-fallback}"),
                    "fallback", "Default placeholder resolves");
            assertBusinessRule(
                    com.carcomfort.core.config.FrameworkConfig.getString("carcomfort.android.app.package") != null
                            && !com.carcomfort.core.config.FrameworkConfig.getString("carcomfort.android.app.package").contains("${"),
                    "App package config contains no raw placeholders");
        } finally {
            System.clearProperty("CARCOMFORT_SMOKE_PROBE");
        }
        finalizeAssertions();
    }

    @Test(groups = {"smoke"})
    public void testFailureClassificationPolicy() {
        FailureClassifier.Classification assertion =
                FailureClassifier.classify(new AssertionError("Expected 500 got 700"));
        assertBusinessRule(
                assertion.category() == FailureClassifier.Category.APPLICATION_DEFECT,
                "Assertion failures are product defects, never auto-repaired");

        FailureClassifier.Classification locator = FailureClassifier.classify(
                new org.openqa.selenium.NoSuchElementException("Unable to locate element"));
        assertBusinessRule(
                locator.category() == FailureClassifier.Category.LOCATOR_DRIFT && locator.autoRepairable(),
                "Missing elements classify as repairable locator drift");

        FailureClassifier.Classification timeout = FailureClassifier.classify(
                new org.openqa.selenium.TimeoutException("timed out"));
        assertBusinessRule(
                timeout.category() == FailureClassifier.Category.SYNC_ISSUE && timeout.autoRepairable(),
                "Timeouts classify as repairable sync issues");
        finalizeAssertions();
    }
}

package com.carcomfort.core.assertions;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.driver.DriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Assertion framework with soft assertions, evidence capture, and safety guards.
 */
public final class SafeAssertions {
    private static final Logger log = LoggerFactory.getLogger(SafeAssertions.class);

    private final SoftAssertions softAssertions;
    private final EvidenceCollector evidenceCollector;
    private final TestLogger testLogger;
    private final DriverManager<?> driverManager;
    private final boolean screenshotOnFailure;
    private final boolean pageSourceOnFailure;
    private final boolean stopOnFirstFailure;

    public SafeAssertions(EvidenceCollector evidenceCollector, TestLogger testLogger, DriverManager<?> driverManager) {
        this.softAssertions = new SoftAssertions();
        this.evidenceCollector = evidenceCollector;
        this.testLogger = testLogger;
        this.driverManager = driverManager;
        this.screenshotOnFailure = FrameworkConfig.getBoolean("carcomfort.assertions.screenshotOnAssertionFailure");
        this.pageSourceOnFailure = FrameworkConfig.getBoolean("carcomfort.assertions.pageSourceOnAssertionFailure");
        this.stopOnFirstFailure = FrameworkConfig.getBoolean("carcomfort.assertions.stopOnFirstFailure");
    }

    public void assertTrue(boolean condition, String message) {
        logAssertion("ASSERT_TRUE", message, condition);
        softAssertions.assertThat(condition).as(message).isTrue();
        handleFailure(condition, message);
    }

    public void assertFalse(boolean condition, String message) {
        logAssertion("ASSERT_FALSE", message, !condition);
        softAssertions.assertThat(condition).as(message).isFalse();
        handleFailure(!condition, message);
    }

    public void assertEquals(Object actual, Object expected, String message) {
        boolean passed = (actual == null && expected == null) || (actual != null && actual.equals(expected));
        logAssertion("ASSERT_EQUALS", message + " | Expected: " + expected + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).isEqualTo(expected);
        handleFailure(passed, message);
    }

    public void assertNotEquals(Object actual, Object expected, String message) {
        boolean passed = (actual == null && expected != null) || (actual != null && !actual.equals(expected));
        logAssertion("ASSERT_NOT_EQUALS", message + " | Expected not: " + expected + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).isNotEqualTo(expected);
        handleFailure(passed, message);
    }

    public void assertNull(Object actual, String message) {
        boolean passed = actual == null;
        logAssertion("ASSERT_NULL", message + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).isNull();
        handleFailure(passed, message);
    }

    public void assertNotNull(Object actual, String message) {
        boolean passed = actual != null;
        logAssertion("ASSERT_NOT_NULL", message + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).isNotNull();
        handleFailure(passed, message);
    }

    public void assertContains(String actual, String expectedSubstring, String message) {
        boolean passed = actual != null && actual.contains(expectedSubstring);
        logAssertion("ASSERT_CONTAINS", message + " | Expected to contain: " + expectedSubstring + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).contains(expectedSubstring);
        handleFailure(passed, message);
    }

    public void assertMatches(String actual, String regex, String message) {
        boolean passed = actual != null && actual.matches(regex);
        logAssertion("ASSERT_MATCHES", message + " | Expected to match: " + regex + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(message).matches(regex);
        handleFailure(passed, message);
    }

    public void assertGreaterThan(Comparable actual, Comparable expected, String message) {
        boolean passed = actual != null && expected != null && actual.compareTo(expected) > 0;
        logAssertion("ASSERT_GREATER_THAN", message + " | Expected > " + expected + " | Actual: " + actual, passed);
        softAssertions.assertThat(passed).as(message).isTrue();
        handleFailure(passed, message);
    }

    public void assertLessThan(Comparable actual, Comparable expected, String message) {
        boolean passed = actual != null && expected != null && actual.compareTo(expected) < 0;
        logAssertion("ASSERT_LESS_THAN", message + " | Expected < " + expected + " | Actual: " + actual, passed);
        softAssertions.assertThat(passed).as(message).isTrue();
        handleFailure(passed, message);
    }

    public void assertBetween(Comparable actual, Comparable lower, Comparable upper, String message) {
        boolean passed = actual != null && lower != null && upper != null &&
                actual.compareTo(lower) >= 0 && actual.compareTo(upper) <= 0;
        logAssertion("ASSERT_BETWEEN", message + " | Expected between " + lower + " and " + upper + " | Actual: " + actual, passed);
        softAssertions.assertThat(passed).as(message).isTrue();
        handleFailure(passed, message);
    }

    public void assertElementVisible(Supplier<Boolean> visibilityCheck, String elementDescription) {
        boolean passed = visibilityCheck.get();
        logAssertion("ASSERT_ELEMENT_VISIBLE", elementDescription, passed);
        softAssertions.assertThat(passed).as("Element visible: " + elementDescription).isTrue();
        handleFailure(passed, elementDescription);
    }

    public void assertElementNotVisible(Supplier<Boolean> visibilityCheck, String elementDescription) {
        boolean passed = !visibilityCheck.get();
        logAssertion("ASSERT_ELEMENT_NOT_VISIBLE", elementDescription, passed);
        softAssertions.assertThat(passed).as("Element not visible: " + elementDescription).isTrue();
        handleFailure(passed, elementDescription);
    }

    public void assertTextEquals(Supplier<String> actualTextSupplier, String expectedText, String elementDescription) {
        String actual = actualTextSupplier.get();
        boolean passed = expectedText.equals(actual);
        logAssertion("ASSERT_TEXT_EQUALS", elementDescription + " | Expected: " + expectedText + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(elementDescription).isEqualTo(expectedText);
        handleFailure(passed, elementDescription);
    }

    public void assertTextContains(Supplier<String> actualTextSupplier, String expectedSubstring, String elementDescription) {
        String actual = actualTextSupplier.get();
        boolean passed = actual != null && actual.contains(expectedSubstring);
        logAssertion("ASSERT_TEXT_CONTAINS", elementDescription + " | Expected to contain: " + expectedSubstring + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(elementDescription).contains(expectedSubstring);
        handleFailure(passed, elementDescription);
    }

    public void assertAttributeEquals(Supplier<String> attributeSupplier, String attributeName, String expectedValue, String elementDescription) {
        String actual = attributeSupplier.get();
        boolean passed = expectedValue.equals(actual);
        logAssertion("ASSERT_ATTRIBUTE_EQUALS", elementDescription + " | " + attributeName + " | Expected: " + expectedValue + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(elementDescription + " [" + attributeName + "]").isEqualTo(expectedValue);
        handleFailure(passed, elementDescription);
    }

    public void assertListSize(Supplier<Integer> sizeSupplier, int expectedSize, String listDescription) {
        int actual = sizeSupplier.get();
        boolean passed = actual == expectedSize;
        logAssertion("ASSERT_LIST_SIZE", listDescription + " | Expected size: " + expectedSize + " | Actual: " + actual, passed);
        softAssertions.assertThat(actual).as(listDescription).isEqualTo(expectedSize);
        handleFailure(passed, listDescription);
    }

    public void assertNotEmpty(Supplier<String> stringSupplier, String message) {
        String actual = stringSupplier.get();
        boolean passed = actual != null && !actual.isBlank();
        logAssertion("ASSERT_NOT_EMPTY", message + " | Actual: '" + actual + "'", passed);
        softAssertions.assertThat(actual).as(message).isNotBlank();
        handleFailure(passed, message);
    }

    public void assertAll() {
        try {
            softAssertions.assertAll();
        } catch (AssertionError e) {
            testLogger.logFailure("Assertion failed: " + e.getMessage());
            throw e;
        }
    }

    public boolean hasFailures() {
        return softAssertions.errorsCollected().size() > 0;
    }

    public int getFailureCount() {
        return softAssertions.errorsCollected().size();
    }

    private void logAssertion(String type, String message, boolean passed) {
        String status = passed ? "PASSED" : "FAILED";
        testLogger.logAssertion(type, message, status);
        log.debug("[{}] {} - {}", type, status, message);
    }

    private void handleFailure(boolean passed, String description) {
        if (!passed) {
            if (screenshotOnFailure && driverManager != null && driverManager.isSessionActive()) {
                evidenceCollector.captureScreenshot("assertion_failure_" + sanitize(description));
            }
            if (pageSourceOnFailure && driverManager != null && driverManager.isSessionActive()) {
                evidenceCollector.capturePageSource("assertion_failure_" + sanitize(description));
            }
            if (stopOnFirstFailure) {
                assertAll();
            }
        }
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_").substring(0, Math.min(50, input.length()));
    }
}
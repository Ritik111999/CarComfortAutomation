package com.carcomfort.core.logging;

import com.carcomfort.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Structured test logging with categorized events.
 */
public final class TestLogger {
    private static final Logger log = LoggerFactory.getLogger(TestLogger.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String testName;
    private final String testClass;
    private final boolean structuredLogging;
    private final boolean maskSecrets;

    public TestLogger(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
        this.structuredLogging = FrameworkConfig.getBoolean("carcomfort.logging.structured");
        this.maskSecrets = FrameworkConfig.getBoolean("carcomfort.logging.maskSecrets");
    }

    public void testStart() {
        logEvent("TEST_START", "Test started: " + testName);
    }

    public void testEnd(String status, long durationMs) {
        logEvent("TEST_END", "Test finished: " + testName + " | Status: " + status + " | Duration: " + durationMs + "ms");
    }

    public void screenOpen(String screenName) {
        logEvent("SCREEN_OPEN", "Screen opened: " + screenName);
    }

    public void action(String action, String element, String details) {
        logEvent("ACTION", action + " | Element: " + element + " | " + details);
    }

    public void assertion(String type, String message, String status) {
        logEvent("ASSERTION", type + " | " + message + " | " + status);
    }

    public void businessCheckpoint(String checkpoint, String details) {
        logEvent("BUSINESS_CHECKPOINT", checkpoint + " | " + details);
    }

    public void warning(String message) {
        logEvent("WARNING", message);
    }

    public void failure(String message) {
        logEvent("FAILURE", message);
    }

    public void logFailure(String message) {
        logEvent("FAILURE", message);
    }

    public void deviceFailure(String message) {
        logEvent("DEVICE_FAILURE", message);
    }

    public void frameworkFailure(String message) {
        logEvent("FRAMEWORK_FAILURE", message);
    }

    public void logAssertion(String type, String message, String status) {
        logEvent("ASSERTION", type + " | " + message + " | " + status);
    }

    private void logEvent(String category, String message) {
        String maskedMessage = maskSecrets ? mask(message) : message;
        String formatted = structuredLogging
                ? String.format("[%s] [%s] [%s] %s", TIMESTAMP.format(LocalDateTime.now()), category, testName, maskedMessage)
                : String.format("[%s] %s", category, maskedMessage);

        switch (category) {
            case "TEST_START", "SCREEN_OPEN", "BUSINESS_CHECKPOINT" -> log.info(formatted);
            case "TEST_END" -> log.info(formatted);
            case "ACTION", "ASSERTION" -> log.debug(formatted);
            case "WARNING" -> log.warn(formatted);
            case "FAILURE", "DEVICE_FAILURE", "FRAMEWORK_FAILURE" -> log.error(formatted);
            default -> log.info(formatted);
        }
    }

    private String mask(String input) {
        if (input == null) return null;

        String[] secretPatterns = FrameworkConfig.getStringList("carcomfort.logging.secretPatterns").toArray(new String[0]);
        String result = input;

        for (String pattern : secretPatterns) {
            result = result.replaceAll("(?i)" + pattern + "\\s*[:=]\\s*\\S+", pattern + "=***MASKED***");
            result = result.replaceAll("(?i)" + pattern + "\\s*[\"']\\S+[\"']", pattern + "='***MASKED***'");
        }

        return result;
    }

    public static TestLogger forClass(Class<?> testClass) {
        return new TestLogger(testClass.getSimpleName(), testClass.getSimpleName());
    }
}
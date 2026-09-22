package com.carcomfort.core.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classifies test failures so AI repair only touches legitimate automation defects.
 *
 * <p>Categories: APPLICATION_DEFECT, LOCATOR_DRIFT, SYNC_ISSUE, DEVICE_INFRASTRUCTURE,
 * TEST_DATA, AUTOMATION_DEFECT. Only LOCATOR_DRIFT, SYNC_ISSUE, AUTOMATION_DEFECT
 * are auto-repairable. Business assertions must never be weakened to match actuals.
 */
public final class FailureClassifier {
    private static final Logger log = LoggerFactory.getLogger(FailureClassifier.class);

    public enum Category {
        APPLICATION_DEFECT,
        LOCATOR_DRIFT,
        SYNC_ISSUE,
        DEVICE_INFRASTRUCTURE,
        TEST_DATA,
        AUTOMATION_DEFECT
    }

    public record Classification(Category category, String reason, boolean autoRepairable) {}

    private FailureClassifier() {}

    public static Classification classify(Throwable throwable) {
        if (throwable == null) {
            return new Classification(Category.AUTOMATION_DEFECT, "Null throwable", true);
        }
        String type = throwable.getClass().getSimpleName();
        String msg = String.valueOf(throwable.getMessage());

        if (throwable instanceof AssertionError) {
            return new Classification(Category.APPLICATION_DEFECT,
                    "Business assertion failed (" + type + "): " + msg + " — do NOT weaken expected values", false);
        }
        if (type.contains("NoSuchElement") || type.contains("ElementNotFound")
                || type.contains("InvalidSelector") || msg.contains("Unable to locate element")) {
            return new Classification(Category.LOCATOR_DRIFT, type + ": " + msg, true);
        }
        if (type.contains("StaleElement") || type.contains("Timeout") || type.contains("NotInteractable")
                || type.contains("ElementClickIntercepted") || type.contains("Wait")) {
            return new Classification(Category.SYNC_ISSUE, type + ": " + msg, true);
        }
        if (type.contains("SessionNotCreated") || type.contains("NoSuchSession")
                || type.contains("UnreachableBrowser") || type.contains("ConnectException")
                || msg.contains("device") && (msg.contains("offline") || msg.contains("unauthorized")
                || msg.contains("disconnected") || msg.contains("UiAutomator2"))) {
            return new Classification(Category.DEVICE_INFRASTRUCTURE, type + ": " + msg, false);
        }
        if (type.contains("IllegalState") && msg.contains("test data")
                || type.contains("IllegalArgument") && msg.contains("account")) {
            return new Classification(Category.TEST_DATA, type + ": " + msg, false);
        }
        Classification result = new Classification(Category.AUTOMATION_DEFECT, type + ": " + msg, true);
        log.debug("Failure classified as {}: {}", result.category(), result.reason());
        return result;
    }

    public static boolean isAutoRepairable(Classification classification) {
        return classification.autoRepairable();
    }
}

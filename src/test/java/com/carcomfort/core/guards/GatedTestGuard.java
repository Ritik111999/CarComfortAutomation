package com.carcomfort.core.guards;

import com.carcomfort.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gated test execution guard - prevents unauthorized execution of one-time/destructive tests.
 */
public final class GatedTestGuard {
    private static final Logger log = LoggerFactory.getLogger(GatedTestGuard.class);

    private final boolean enabled;
    private final boolean requireExplicitAuthorization;
    private final String[] blockedCategories;

    public GatedTestGuard() {
        this.enabled = FrameworkConfig.getBoolean("carcomfort.gated.enabled");
        this.requireExplicitAuthorization = FrameworkConfig.getBoolean("carcomfort.gated.requireExplicitAuthorization");
        this.blockedCategories = FrameworkConfig.getStringList("carcomfort.gated.blockedCategories").toArray(new String[0]);
    }

    public void verifyAuthorization(String testName, String... categories) {
        if (!enabled) {
            log.debug("Gated guard disabled, allowing test: {}", testName);
            return;
        }

        boolean isGated = false;
        String matchedCategory = null;

        for (String category : categories) {
            for (String blocked : blockedCategories) {
                if (blocked.equalsIgnoreCase(category)) {
                    isGated = true;
                    matchedCategory = blocked;
                    break;
                }
            }
            if (isGated) break;
        }

        if (!isGated) {
            log.debug("Test {} not in gated categories, allowing", testName);
            return;
        }

        String runGated = System.getProperty("RUN_GATED_TESTS", System.getenv("RUN_GATED_TESTS"));
        String gatedCase = System.getProperty("GATED_CASE", System.getenv("GATED_CASE"));

        boolean authorized = "true".equalsIgnoreCase(runGated) && gatedCase != null && !gatedCase.isBlank();

        if (!authorized) {
            String msg = String.format(
                    "GATED TEST BLOCKED: %s | Category: %s | " +
                    "Required: RUN_GATED_TESTS=true and GATED_CASE=<case> | " +
                    "Provided: RUN_GATED_TESTS=%s, GATED_CASE=%s",
                    testName, matchedCategory, runGated, gatedCase
            );
            log.error(msg);
            if (FrameworkConfig.getBoolean("carcomfort.gated.abortOnUnauthorized")) {
                throw new GatedTestException(msg);
            }
        }

        boolean caseMatches = gatedCase != null && matchesCategory(gatedCase, categories);
        if (!caseMatches) {
            String msg = String.format(
                    "GATED CASE MISMATCH: %s | Requested case: %s | Test categories: %s",
                    testName, gatedCase, String.join(", ", categories)
            );
            log.error(msg);
            if (FrameworkConfig.getBoolean("carcomfort.gated.abortOnUnauthorized")) {
                throw new GatedTestException(msg);
            }
        }

        log.warn("GATED TEST AUTHORIZED: {} | Category: {} | Case: {}", testName, matchedCategory, gatedCase);
    }

    public void verifyAuthorization(String testName, String category) {
        verifyAuthorization(testName, new String[]{category});
    }

    public boolean isGated(String... categories) {
        for (String category : categories) {
            for (String blocked : blockedCategories) {
                if (blocked.equalsIgnoreCase(category)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesCategory(String gatedCase, String[] categories) {
        for (String category : categories) {
            if (category.equalsIgnoreCase(gatedCase) ||
                    category.toUpperCase().contains(gatedCase.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static class GatedTestException extends RuntimeException {
        public GatedTestException(String message) {
            super(message);
        }
    }
}
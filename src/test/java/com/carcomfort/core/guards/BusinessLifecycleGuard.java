package com.carcomfort.core.guards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business-lifecycle execution guard — separate from the one-time
 * {@link GatedTestGuard}. Controlled state-changing functional tests
 * (booking create/accept/progress/complete) run ONLY when BOTH:
 *
 * <pre>
 * RUN_BUSINESS_LIFECYCLE_TESTS=true
 * BUSINESS_CASE=&lt;case&gt;   (e.g. BOOKING_LIFECYCLE_HAPPY_PATH)
 * </pre>
 *
 * Values are read from system properties first, environment second, so
 * {@code mvn test -DRUN_BUSINESS_LIFECYCLE_TESTS=true -DBUSINESS_CASE=...}
 * and exported env vars both work. Never placed inside ordinary smoke.
 */
public final class BusinessLifecycleGuard {
    private static final Logger log = LoggerFactory.getLogger(BusinessLifecycleGuard.class);

    public void verifyCase(String testName, String requiredCase) {
        String flag = System.getProperty("RUN_BUSINESS_LIFECYCLE_TESTS",
                System.getenv("RUN_BUSINESS_LIFECYCLE_TESTS"));
        String businessCase = System.getProperty("BUSINESS_CASE", System.getenv("BUSINESS_CASE"));

        boolean authorized = "true".equalsIgnoreCase(flag)
                && requiredCase.equalsIgnoreCase(businessCase);

        if (!authorized) {
            String msg = String.format(
                    "BUSINESS LIFECYCLE BLOCKED: %s | Required: RUN_BUSINESS_LIFECYCLE_TESTS=true and BUSINESS_CASE=%s | "
                            + "Provided: RUN_BUSINESS_LIFECYCLE_TESTS=%s, BUSINESS_CASE=%s. "
                            + "Use scripts/run-lifecycle.sh %s (owner-authorized only).",
                    testName, requiredCase, flag, businessCase, requiredCase);
            log.error(msg);
            throw new BusinessLifecycleException(msg);
        }
        log.warn("BUSINESS LIFECYCLE AUTHORIZED: {} | Case: {}", testName, businessCase);
    }

    public static String currentCase() {
        String businessCase = System.getProperty("BUSINESS_CASE", System.getenv("BUSINESS_CASE"));
        return businessCase != null ? businessCase : "";
    }

    public static class BusinessLifecycleException extends RuntimeException {
        public BusinessLifecycleException(String message) {
            super(message);
        }
    }
}

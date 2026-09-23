package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.screens.CustomerHomeScreen;
import com.carcomfort.mobile.android.screens.CustomerProfileScreen;
import com.carcomfort.mobile.android.screens.LoginScreen;
import com.carcomfort.mobile.android.screens.RoleSelectionScreen;

/**
 * Customer authentication flow: role select → login with authorized test account.
 * Credentials come from environment only (CUSTOMER_TEST_EMAIL / CUSTOMER_TEST_PASSWORD).
 * Login with existing accounts is explicitly authorized; account creation is NOT.
 */
public final class CustomerAuthFlow extends BaseBusinessFlow {

    private final RoleSelectionScreen roleSelection;
    private final LoginScreen login;
    private final CustomerHomeScreen home;
    private final CustomerProfileScreen profile;

    public CustomerAuthFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.roleSelection = new RoleSelectionScreen(driverManager);
        this.login = new LoginScreen(driverManager);
        this.home = new CustomerHomeScreen(driverManager);
        this.profile = new CustomerProfileScreen(driverManager);
    }

    public void loginAsCustomer() {
        String email = System.getenv("CUSTOMER_TEST_EMAIL");
        String password = System.getenv("CUSTOMER_TEST_PASSWORD");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "CUSTOMER_TEST_EMAIL / CUSTOMER_TEST_PASSWORD must be set in the environment");
        }
        logBusinessCheckpoint("ROLE_SELECT", "Selecting Customer role");
        settleDeviceState();
        EntryState entry = awaitEntryState();
        if (entry == EntryState.HOME_CUSTOMER) {
            logBusinessCheckpoint("SESSION_RESTORED", "Already authenticated; persisted session restored");
            captureCheckpointEvidence("customer_session_restored");
            return;
        }
        if (entry == EntryState.HOME_PROVIDER) {
            throw new IllegalStateException(
                    "Provider session is active — refusing customer flow (cross-role contamination guard). "
                            + "Log out the provider first.");
        }
        if (entry != EntryState.LOGIN) {
            roleSelection.waitForScreenLoaded();
            roleSelection.selectCustomer();
        } else {
            logBusinessCheckpoint("ROLE_SELECT", "Already on login screen; role pre-selected");
        }

        logBusinessCheckpoint("LOGIN_SUBMIT", "Submitting customer login");
        login.loginAs(email, password);
        login.waitForLoginGone();
        captureCheckpointEvidence("customer_logged_in");
        logBusinessCheckpoint("LOGIN_DONE", "Login screen dismissed");
    }

    public boolean isAuthenticated() {
        try {
            return !login.isScreenDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCustomerHomeActive() {
        return home.isScreenDisplayed();
    }

    /** Verifies the authenticated home state with real business evidence. */
    public void verifyCustomerHome(String displayName) {
        home.waitForScreenLoadedLong();
        captureCheckpointEvidence("customer_home");
        assertBusinessRule(home.isGreetingShown(displayName),
                "Personalized greeting shown for " + displayName);
        assertBusinessRule(!driverManager.getDriver()
                .findElements(com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Car Wash")).isEmpty(),
                "Car Wash service card present");
        assertBusinessRule(!driverManager.getDriver()
                .findElements(com.carcomfort.mobile.android.LocatorFactory.accessibilityId("EV Charging")).isEmpty(),
                "EV Charging service card present");
        logBusinessCheckpoint("HOME_VERIFIED", "Customer home verified for " + displayName);
        finalizeAssertions();
    }

    /** Opens the profile via avatar; caller discovers/captures the profile state. */
    public void openProfile() {
        home.tapAvatar();
        profile.waitForScreenLoadedLong();
        captureCheckpointEvidence("customer_profile");
        assertBusinessRule(profile.isPaymentMethodsRowPresent(),
                "Payment Methods row present (entry point documented as gated)");
        logBusinessCheckpoint("PROFILE_OPEN", "Profile verified via avatar");
        finalizeAssertions();
    }

    /**
     * Logs out from the PROFILE screen. Verified behavior (2026-09-22):
     * the destination after Log Out + Okay is the logged-out auth area but
     * varies by back-stack — either ROLE SELECTION ("Select your role",
     * failure-evidence 20260922_142209_041) or the LOGIN form directly
     * (failure-evidence 20260922_142614_576). Both prove teardown; the
     * assertion accepts either marker instead of overfitting one run.
     * Provider behavior stays in its own separately verified flow.
     * Authorized session teardown.
     */
    public void logout() {
        profile.tapLogOut();
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        org.openqa.selenium.By roleTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Select your role");
        org.openqa.selenium.By loginTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Welcome to Car Comfort");
        final boolean[] seen = {false};
        final String[] where = {""};
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(30))
                    .pollInterval(java.time.Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(() -> {
                        if (!driver.findElements(loginTitle).isEmpty()) {
                            seen[0] = true;
                            where[0] = "login form";
                            return true;
                        }
                        if (!driver.findElements(roleTitle).isEmpty()) {
                            seen[0] = true;
                            where[0] = "role selection";
                            return true;
                        }
                        return false;
                    });
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            // seen[0] stays false -> assertion below reports it
        }
        captureCheckpointEvidence("customer_logged_out");
        assertBusinessRule(seen[0], "Customer reaches a logged-out state after logout");
        logBusinessCheckpoint("LOGOUT_DONE", "Customer logged out cleanly (" + where[0] + ")");
        finalizeAssertions();
    }
}

package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.screens.LoginScreen;
import com.carcomfort.mobile.android.screens.ProviderHomeScreen;
import com.carcomfort.mobile.android.screens.ProviderProfileScreen;
import com.carcomfort.mobile.android.screens.ProviderSettingsScreen;
import com.carcomfort.mobile.android.screens.RoleSelectionScreen;

/**
 * Provider authentication flow: role select → login with authorized provider account.
 * Credentials from environment only (PROVIDER_TEST_EMAIL / PROVIDER_TEST_PASSWORD).
 * Provider verification/KYC and job accept/reject/complete are NOT included (gated).
 */
public final class ProviderAuthFlow extends BaseBusinessFlow {

    private final RoleSelectionScreen roleSelection;
    private final LoginScreen login;
    private final ProviderHomeScreen home;
    private final ProviderProfileScreen profile;
    private final ProviderSettingsScreen settings;

    public ProviderAuthFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.roleSelection = new RoleSelectionScreen(driverManager);
        this.login = new LoginScreen(driverManager);
        this.home = new ProviderHomeScreen(driverManager);
        this.profile = new ProviderProfileScreen(driverManager);
        this.settings = new ProviderSettingsScreen(driverManager);
    }

    public void loginAsProvider() {
        String email = System.getenv("PROVIDER_TEST_EMAIL");
        String password = System.getenv("PROVIDER_TEST_PASSWORD");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "PROVIDER_TEST_EMAIL / PROVIDER_TEST_PASSWORD must be set in the environment");
        }
        logBusinessCheckpoint("ROLE_SELECT", "Selecting Service Provider role");
        settleDeviceState();
        EntryState entry = awaitEntryState();
        if (entry == EntryState.HOME_PROVIDER) {
            logBusinessCheckpoint("SESSION_RESTORED", "Already authenticated; persisted session restored");
            captureCheckpointEvidence("provider_session_restored");
            return;
        }
        if (entry == EntryState.HOME_CUSTOMER) {
            throw new IllegalStateException(
                    "Customer session is active — refusing provider flow (cross-role contamination guard). "
                            + "Log out the customer first.");
        }
        if (entry != EntryState.LOGIN) {
            roleSelection.waitForScreenLoaded();
            roleSelection.selectProvider();
        } else {
            logBusinessCheckpoint("ROLE_SELECT", "Already on login screen; role pre-selected");
        }

        logBusinessCheckpoint("LOGIN_SUBMIT", "Submitting provider login");
        login.loginAs(email, password);
        login.waitForLoginGone();
        home.waitForScreenLoadedLong();
        captureCheckpointEvidence("provider_logged_in");
        logBusinessCheckpoint("LOGIN_DONE", "Provider login screen dismissed");
    }

    public boolean isProviderHomeActive() {
        return home.isScreenDisplayed();
    }

    /**
     * True once the login screen is gone. Polls for ABSENCE (up to the short
     * wait) instead of a single presence probe: right after submit the login
     * title node can linger invisibly in the hierarchy while home builds
     * (evidence 20260922_143723_361: home rendered, title still present),
     * which a one-shot check misreads as "still logged out".
     */
    public boolean isAuthenticated() {
        try {
            io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
            org.openqa.selenium.By loginMarker = login.getUniqueLocator();
            waits.waitShort(ignored -> {
                java.util.List<org.openqa.selenium.WebElement> found = driver.findElements(loginMarker);
                if (found.isEmpty()) {
                    return Boolean.TRUE;
                }
                try {
                    return found.get(0).isDisplayed() ? null : Boolean.TRUE;
                } catch (Exception gone) {
                    return Boolean.TRUE;
                }
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Verifies the provider home with real business evidence (no job actions). */
    public void verifyProviderHome(String displayName) {
        captureCheckpointEvidence("provider_home");
        assertBusinessRule(awaitGreeting(displayName),
                "Personalized provider greeting shown for " + displayName);
        logBusinessCheckpoint("HOME_VERIFIED", "Provider home verified for " + displayName);
        finalizeAssertions();
    }

    /**
     * Greeting poll: after login submit the home builds async and single-shot
     * presence probes race the transition (suite-sequential evidence
     * 20260922_144345_226: login form captured 1s before home rendered).
     * Polls up to 15s; still fails loudly if the greeting never appears.
     */
    private boolean awaitGreeting(String displayName) {
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        org.openqa.selenium.By greeting = home.greetingFor(displayName);
        final boolean[] seen = {false};
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(15))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> {
                        seen[0] = !driver.findElements(greeting).isEmpty();
                        return seen[0];
                    });
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            // seen[0] stays false -> assertion reports it
        }
        return seen[0];
    }

    /** Opens the provider profile via avatar (logout path discovery). */
    public void openProfile() {
        home.tapAvatar();
        profile.waitForScreenLoadedLong();
        profile.waitForContentLoaded();
        captureCheckpointEvidence("provider_profile");
        assertBusinessRule(profile.isVerifiedBadgeShown(), "VERIFIED DRIVER badge shown");
        assertBusinessRule(profile.isOnboardingCompleteShown(), "Onboarding complete shown");
        logBusinessCheckpoint("PROFILE_OPEN", "Provider profile verified via avatar");
        finalizeAssertions();
    }

    /** Opens the bottom-nav settings tab (provider logout lives here). */
    public void openSettings() {
        home.tapNavSettings();
        settings.waitForScreenLoadedLong();
        captureCheckpointEvidence("provider_settings_entry");
        assertBusinessRule(settings.isPaymentMethodsRowPresent(),
                "Payment Methods row present (entry point documented as gated)");
        logBusinessCheckpoint("SETTINGS_OPEN", "Provider settings verified via bottom nav");
        finalizeAssertions();
    }

    /**
     * Returns from the pushed profile screen to home via BACK (profile was pushed
     * over home, so BACK pops the stack — verified safe; BACK from a tab root
     * exits the app and is never used there).
     */
    public void backToHomeFromProfile() {
        driverManager.getDriver().navigate().back();
        home.waitForScreenLoadedLong();
        logBusinessCheckpoint("HOME_RETURN", "Returned to provider home from profile");
    }

    /**
     * Logs out from the settings tab. Verified behavior (2026-09-22): the
     * destination after Logout + Okay is the logged-out auth area but varies
     * by back-stack — LOGIN form (mapping evidence AND-SHARED-LOGIN-002/004)
     * or ROLE SELECTION (suite evidence 20260922_144319_706). The assertion
     * accepts either marker instead of overfitting one run (same lesson as
     * the customer logout contract). Authorized teardown.
     */
    public void logout() {
        settings.tapLogOut();
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
        captureCheckpointEvidence("provider_logged_out");
        assertBusinessRule(seen[0], "Provider reaches a logged-out auth state after logout");
        logBusinessCheckpoint("LOGOUT_DONE", "Provider logged out cleanly (" + where[0] + ")");
        finalizeAssertions();
    }
}

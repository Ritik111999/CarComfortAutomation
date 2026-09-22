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
        captureCheckpointEvidence("provider_logged_in");
        logBusinessCheckpoint("LOGIN_DONE", "Provider login screen dismissed");
    }

    public boolean isAuthenticated() {
        try {
            return !login.isScreenDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Verifies the provider home with real business evidence (no job actions). */
    public void verifyProviderHome(String displayName) {
        captureCheckpointEvidence("provider_home");
        assertBusinessRule(home.isGreetingShown(displayName),
                "Personalized provider greeting shown for " + displayName);
        logBusinessCheckpoint("HOME_VERIFIED", "Provider home verified for " + displayName);
        finalizeAssertions();
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

    /** Logs out from the settings tab and verifies return to role selection. */
    public void logout() {
        settings.tapLogOut();
        roleSelection.waitForScreenLoadedLong();
        captureCheckpointEvidence("provider_logged_out");
        assertBusinessRule(!driverManager.getDriver()
                .findElements(com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Select your role")).isEmpty(),
                "Returned to role selection after provider logout");
        logBusinessCheckpoint("LOGOUT_DONE", "Provider logged out cleanly");
        finalizeAssertions();
    }
}

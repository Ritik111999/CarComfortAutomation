package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.ProviderBookingDetailScreen;
import com.carcomfort.mobile.android.screens.ProviderBookingsScreen;
import com.carcomfort.mobile.android.screens.ProviderHelpScreen;
import com.carcomfort.mobile.android.screens.ProviderHomeScreen;
import com.carcomfort.mobile.android.screens.ProviderProfileScreen;
import com.carcomfort.mobile.android.screens.ProviderSettingsScreen;
import com.carcomfort.mobile.android.screens.ProviderWalletScreen;

/**
 * Provider safe-navigation flow: Home → Profile → Bookings → safe booking
 * detail (if a card exists) → Wallet (view-only) → Settings → Help.
 *
 * <p>Boundaries (never crossed here): Accept/Reject/Start/Complete/Cancel,
 * Charge/Refund, Stripe verification, Withdraw, Payment Methods, Switch to
 * Customer, Delete Account, KYC. Detail is opened only for an existing
 * read-only card; an empty bookings list is a valid state, not a failure —
 * but the screen must still prove itself (title + filter or recognized
 * empty marker). No raw driver use from tests.
 */
public final class ProviderNavigationFlow extends BaseBusinessFlow {

    private final ProviderHomeScreen home;
    private final ProviderProfileScreen profile;
    private final ProviderBookingsScreen bookings;
    private final ProviderBookingDetailScreen detail;
    private final ProviderWalletScreen wallet;
    private final ProviderSettingsScreen settings;
    private final ProviderHelpScreen help;
    private final BottomNavComponent bottomNav;
    private boolean detailOpened = false;

    public ProviderNavigationFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.home = new ProviderHomeScreen(driverManager);
        this.profile = new ProviderProfileScreen(driverManager);
        this.bookings = new ProviderBookingsScreen(driverManager);
        this.detail = new ProviderBookingDetailScreen(driverManager);
        this.wallet = new ProviderWalletScreen(driverManager);
        this.settings = new ProviderSettingsScreen(driverManager);
        this.help = new ProviderHelpScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    /** Verifies the authenticated Provider Home (role greeting marker, transition-tolerant). */
    public void verifyHome(String displayName) {
        home.waitForScreenLoadedLong();
        captureCheckpointEvidence("prov_nav_home");
        assertBusinessRule(awaitGreeting(displayName),
                "Personalized provider greeting shown for " + displayName);
        logBusinessCheckpoint("HOME_VERIFIED", "Provider home verified for " + displayName);
        finalizeAssertions();
    }

    /** Greeting poll: home builds async post-login; single-shot probes race it. */
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

    /** Opens the provider profile via avatar (VERIFIED badge, no PII asserts). */
    public void openProfile() {
        home.tapAvatar();
        profile.waitForScreenLoadedLong();
        profile.waitForContentLoaded();
        captureCheckpointEvidence("prov_nav_profile");
        assertBusinessRule(profile.isVerifiedBadgeShown(), "VERIFIED DRIVER badge shown");
        assertBusinessRule(profile.isOnboardingCompleteShown(), "Onboarding complete shown");
        logBusinessCheckpoint("PROFILE_OPEN", "Provider profile verified via avatar");
        finalizeAssertions();
    }

    /** Returns from the pushed profile screen to home via BACK (stack pop, verified safe). */
    public void backToHomeFromProfile() {
        driverManager.getDriver().navigate().back();
        home.waitForScreenLoadedLong();
        logBusinessCheckpoint("HOME_RETURN", "Returned to provider home from profile");
    }

    /** Returns from a tab root via the home tab (BACK would exit the app). */
    public void homeViaTab() {
        bottomNav.tapHomeTab();
        home.waitForScreenLoadedLong();
        logBusinessCheckpoint("HOME_RETURN", "Returned to provider home via home tab");
    }

    /**
     * Opens My Bookings (tab 1). STATE A (cards) and STATE B (recognized
     * empty state) both pass; an unrecognized empty list fails loudly so a
     * broken list cannot hide behind the fallback.
     */
    public void openBookings() {
        bottomNav.tapTab(1);
        bookings.waitForScreenLoadedLong();
        captureCheckpointEvidence("prov_nav_bookings");
        assertBusinessRule(bookings.isFilterAllShown(),
                "Bookings filter control (All) present");
        int cards = bookings.viewDetailsCount();
        logBusinessCheckpoint("BOOKINGS_COUNT", "Booking cards with details entry: " + cards);
        assertBusinessRule(cards > 0 || isRecognizedEmptyState(),
                "Bookings shows cards or a recognized empty state");
        logBusinessCheckpoint("BOOKINGS_OPEN", "Provider bookings verified");
        finalizeAssertions();
    }

    /** Opens the first booking detail when a card exists; otherwise logs the skip. */
    public void openSafeBookingDetailIfAvailable() {
        int cards = bookings.viewDetailsCount();
        if (cards == 0) {
            detailOpened = false;
            logBusinessCheckpoint("DETAIL_SKIP", "No booking cards — detail step skipped (valid empty state)");
            return;
        }
        bookings.openFirstDetail();
        detail.waitForScreenLoadedLong();
        captureCheckpointEvidence("prov_nav_detail");
        assertBusinessRule(detail.isBookingMetaShown(),
                "Booking detail shows booking ID and status (read-only)");
        detailOpened = true;
        logBusinessCheckpoint("DETAIL_OPEN", "Provider booking detail verified (no job action taken)");
        finalizeAssertions();
    }

    /** Returns from the pushed booking detail to the bookings list (no-op if skipped). */
    public void backToBookingsFromDetail() {
        if (!detailOpened) {
            logBusinessCheckpoint("DETAIL_RETURN_SKIP", "Detail was not opened — staying on bookings list");
            return;
        }
        detailOpened = false;
        driverManager.getDriver().navigate().back();
        bookings.waitForScreenLoadedLong();
        logBusinessCheckpoint("BOOKINGS_RETURN", "Returned to bookings from detail");
    }

    /** Opens Wallet (tab 2); Stripe CTAs are presence-verified only, never tapped. */
    public void openWallet() {
        bottomNav.tapTab(2);
        wallet.waitForScreenLoadedLong();
        awaitWalletBody();
        captureCheckpointEvidence("prov_nav_wallet");
        boolean gate = wallet.isStripeGateShown();
        int bodyNodes = countBodyDescs();
        logBusinessCheckpoint("WALLET_STATE", "Stripe gate shown: " + gate + "; body nodes: " + bodyNodes);
        assertBusinessRule(gate || bodyNodes > 0,
                "Wallet shows Stripe gate or account content (view-only, no Stripe action)");
        logBusinessCheckpoint("WALLET_OPEN", "Provider wallet verified (no financial mutation)");
        finalizeAssertions();
    }

    /**
     * Wallet body renders async after the title (mapping-era dumps showed 5
     * nodes; a bare title was observed when asserted too early). Waits up to
     * 15s for the gate or any body content; never taps anything.
     */
    private void awaitWalletBody() {
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(15))
                    .pollInterval(java.time.Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(() -> wallet.isStripeGateShown() || countBodyDescs() > 0);
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            logBusinessCheckpoint("WALLET_BODY_WAIT",
                    "Wallet body did not populate within 15s — asserting on observed state");
        }
    }

    private int countBodyDescs() {
        try {
            return driverManager.getDriver()
                    .findElements(org.openqa.selenium.By.xpath("//*[@content-desc]")).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Opens Settings (tab 3); gated rows are presence-verified only, never entered. */
    public void openSettings() {
        bottomNav.tapTab(3);
        settings.waitForScreenLoadedLong();
        captureCheckpointEvidence("prov_nav_settings");
        assertBusinessRule(settings.isPaymentMethodsRowPresent(),
                "Settings payment entry present (documented as gated, not entered)");
        logBusinessCheckpoint("SETTINGS_OPEN", "Provider settings verified (no gated entry)");
        finalizeAssertions();
    }

    /** Opens Help (tab 4, read-only). */
    public void openHelp() {
        bottomNav.tapTab(4);
        help.waitForScreenLoadedLong();
        captureCheckpointEvidence("prov_nav_help");
        assertBusinessRule(help.isHelpContentShown(),
                "Help content shown");
        logBusinessCheckpoint("HELP_OPEN", "Provider help verified (read-only)");
        finalizeAssertions();
    }

    private boolean isRecognizedEmptyState() {
        var driver = driverManager.getDriver();
        String[] candidates = {
                "No bookings", "No Bookings", "No upcoming bookings",
                "You have no bookings", "No jobs", "Nothing here"
        };
        for (String text : candidates) {
            String literal = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            if (!driver.findElements(LocatorFactory.uiAutomator(
                    "new UiSelector().descriptionContains(" + literal + ")")).isEmpty()
                    || !driver.findElements(LocatorFactory.uiAutomator(
                    "new UiSelector().textContains(" + literal + ")")).isEmpty()) {
                logBusinessCheckpoint("EMPTY_STATE", "Recognized bookings empty marker: " + text);
                return true;
            }
        }
        return false;
    }
}

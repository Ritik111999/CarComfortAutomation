package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.CustomerActiveBookingScreen;
import com.carcomfort.mobile.android.screens.CustomerBookingsScreen;
import com.carcomfort.mobile.android.screens.CustomerHomeScreen;
import com.carcomfort.mobile.android.screens.CustomerProfileScreen;
import com.carcomfort.mobile.android.screens.CustomerServiceWizardScreen;
import com.carcomfort.mobile.android.screens.CustomerSettingsScreen;
import com.carcomfort.mobile.android.screens.CustomerSupportScreen;
import com.carcomfort.mobile.android.screens.LoginScreen;
import com.carcomfort.mobile.android.screens.RoleSelectionScreen;

/**
 * Customer safe-navigation flow: Home → service wizard (step 1 only) →
 * Bookings → Active → Settings → Support → Profile, always returning via
 * mapped safe paths (home tab for tab roots, BACK for pushed screens).
 *
 * <p>Boundaries (never crossed here): wizard Next/Confirm/Book/Pay/Submit
 * (GATE-CUST-BOOKING-SUBMIT-001), Payment Methods, Switch to Service
 * Provider, Delete Account. No raw driver use from tests — all navigation
 * lives in this flow and the Screen/Component layer.
 */
public final class CustomerNavigationFlow extends BaseBusinessFlow {

    private final CustomerHomeScreen home;
    private final CustomerServiceWizardScreen wizard;
    private final CustomerBookingsScreen bookings;
    private final CustomerActiveBookingScreen active;
    private final CustomerSettingsScreen settings;
    private final CustomerSupportScreen support;
    private final CustomerProfileScreen profile;
    private final BottomNavComponent bottomNav;
    private final RoleSelectionScreen roleSelection;
    private final LoginScreen login;

    public CustomerNavigationFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.home = new CustomerHomeScreen(driverManager);
        this.wizard = new CustomerServiceWizardScreen(driverManager);
        this.bookings = new CustomerBookingsScreen(driverManager);
        this.active = new CustomerActiveBookingScreen(driverManager);
        this.settings = new CustomerSettingsScreen(driverManager);
        this.support = new CustomerSupportScreen(driverManager);
        this.profile = new CustomerProfileScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
        this.roleSelection = new RoleSelectionScreen(driverManager);
        this.login = new LoginScreen(driverManager);
    }

    /** Verifies the authenticated Customer Home (greeting + service entries). */
    public void verifyHome(String displayName) {
        home.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_home");
        assertBusinessRule(home.isGreetingShown(displayName),
                "Personalized greeting shown for " + displayName);
        assertBusinessRule(!driverManager.getDriver()
                .findElements(LocatorFactory.accessibilityId("Car Wash")).isEmpty(),
                "Car Wash service card present");
        assertBusinessRule(!driverManager.getDriver()
                .findElements(LocatorFactory.accessibilityId("EV Charging")).isEmpty(),
                "EV Charging service card present");
        logBusinessCheckpoint("HOME_VERIFIED", "Customer home verified for " + displayName);
        finalizeAssertions();
    }

    /** Opens the Car Wash card and verifies wizard step 1 (Location). */
    public void openCarWashStep1() {
        home.tapCarWash();
        wizard.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_wizard_carwash");
        assertBusinessRule(wizard.isFourStepWizardShown(),
                "Car Wash wizard shows Location/Service/Vehicle/Review steps");
        logBusinessCheckpoint("WIZARD_STEP1", "Car Wash wizard step 1 verified (Next not pressed)");
        finalizeAssertions();
    }

    /** Returns from a pushed screen (wizard/detail/profile) via BACK to Home. */
    public void backToHomeFromPushed() {
        driverManager.getDriver().navigate().back();
        home.waitForScreenLoadedLong();
        logBusinessCheckpoint("HOME_RETURN", "Returned to Customer Home from pushed screen");
    }

    /** Returns from a tab root via the home tab (BACK would exit the app). */
    public void homeViaTab() {
        bottomNav.tapHomeTab();
        home.waitForScreenLoadedLong();
        logBusinessCheckpoint("HOME_RETURN", "Returned to Customer Home via home tab");
    }

    /** Opens My Bookings (tab 1) and verifies list identity without production-data coupling. */
    public void openBookings() {
        bottomNav.tapTab(1);
        bookings.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_bookings");
        assertBusinessRule(bookings.isFilterAllShown(),
                "Bookings filter control (All) present");
        int cards = bookings.viewDetailsCount();
        logBusinessCheckpoint("BOOKINGS_COUNT", "Booking cards with details entry: " + cards);
        assertBusinessRule(cards > 0,
                "Booking cards present (valid booking states observed, no exact-data coupling)");
        logBusinessCheckpoint("BOOKINGS_OPEN", "Customer bookings verified");
        finalizeAssertions();
    }

    /** Opens My Active Booking (tab 2); accepts empty state or live cards. */
    public void openActiveBookings() {
        bottomNav.tapTab(2);
        active.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_active");
        boolean empty = active.isEmptyStateShown();
        logBusinessCheckpoint("ACTIVE_STATE", "Active bookings empty state: " + empty);
        assertBusinessRule(empty || bookings.viewDetailsCount() > 0,
                "Active bookings shows empty state or live booking cards");
        logBusinessCheckpoint("ACTIVE_OPEN", "Customer active bookings verified");
        finalizeAssertions();
    }

    /** Opens Settings (tab 3); gated rows are presence-verified only, never entered. */
    public void openSettings() {
        bottomNav.tapTab(3);
        settings.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_settings");
        assertBusinessRule(settings.isCoreRowsPresent(),
                "Settings core rows present (switch/provider, history, payment entries)");
        logBusinessCheckpoint("SETTINGS_OPEN", "Customer settings verified (no gated entry)");
        finalizeAssertions();
    }

    /** Opens Support (tab 4, read-only). */
    public void openSupport() {
        bottomNav.tapTab(4);
        support.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_support");
        assertBusinessRule(support.isHelpContentShown(),
                "Support help content shown");
        logBusinessCheckpoint("SUPPORT_OPEN", "Customer support verified (read-only)");
        finalizeAssertions();
    }

    /** Opens the profile via avatar; asserts safe rows only (no PII values). */
    public void openProfile() {
        home.tapAvatar();
        profile.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_profile");
        assertBusinessRule(profile.isPaymentMethodsRowPresent(),
                "Payment Methods row present (entry point documented as gated)");
        logBusinessCheckpoint("PROFILE_OPEN", "Customer profile verified via avatar");
        finalizeAssertions();
    }

    /**
     * Ensures the login form from the post-logout auth area: if role
     * selection is showing, select Customer (safe navigation); if the login
     * form is already showing, only verify its markers. No credentials
     * entered, no side effects.
     */
    public void toLoginFromRoleSelection() {
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        org.openqa.selenium.By roleTitle =
                com.carcomfort.mobile.android.LocatorFactory.accessibilityId("Select your role");
        if (!driver.findElements(roleTitle).isEmpty()) {
            roleSelection.selectCustomer();
        } else {
            logBusinessCheckpoint("ROLE_SKIP", "Already past role selection; verifying login form directly");
        }
        login.waitForScreenLoadedLong();
        captureCheckpointEvidence("cust_nav_login_form");
        assertBusinessRule(login.confirmPresent(),
                "Login form shown (Welcome to Car Comfort)");
        logBusinessCheckpoint("LOGIN_FORM", "Post-logout login screen verified");
        finalizeAssertions();
    }
}

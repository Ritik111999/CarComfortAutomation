package com.carcomfort.tests.android.provider;

import com.carcomfort.flows.ProviderAuthFlow;
import com.carcomfort.flows.ProviderNavigationFlow;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * Provider smoke on the physical device: login → home → profile → bookings
 * → safe booking detail (only if a card exists) → wallet (view-only) →
 * settings → help → logout → session-end verification.
 *
 * <p>Safe and deterministic. Accept/Reject/Start/Complete/Cancel, Charge/
 * Refund, Stripe verification, Withdraw, Payment Methods, Switch to
 * Customer, Delete Account, and KYC are NOT performed here (gated or
 * separately scoped).
 */
public class ProviderNavigationSmokeTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "android", "provider"})
    public void testProviderSafeNavigationSmoke() {
        initializeAndroidDriver();

        ProviderAuthFlow auth = new ProviderAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        auth.loginAsProvider();

        assertBusinessRule(auth.isAuthenticated(), "Provider leaves login screen after authorized login");
        testLogger.businessCheckpoint("AUTH_STATE", "Provider authenticated landing state reached");
        finalizeAssertions();

        String displayName = System.getenv("PROVIDER_DISPLAY_NAME");
        String name = displayName != null && !displayName.isBlank() ? displayName : "karl Driver";

        ProviderNavigationFlow nav = new ProviderNavigationFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);

        nav.verifyHome(name);

        nav.openProfile();
        nav.backToHomeFromProfile();

        nav.openBookings();
        nav.openSafeBookingDetailIfAvailable();
        nav.backToBookingsFromDetail();
        nav.homeViaTab();

        nav.openWallet();
        nav.openSettings();
        nav.openHelp();
        nav.homeViaTab();

        auth.openSettings();
        auth.logout();

        assertBusinessRule(!auth.isAuthenticated(), "Provider session ends after logout");
        testLogger.businessCheckpoint("SMOKE_DONE", "Provider safe-navigation smoke completed");
        finalizeAssertions();
    }
}

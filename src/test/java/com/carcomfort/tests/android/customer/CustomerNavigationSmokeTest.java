package com.carcomfort.tests.android.customer;

import com.carcomfort.flows.CustomerAuthFlow;
import com.carcomfort.flows.CustomerNavigationFlow;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * Customer smoke on the physical device: login → home → service wizard
 * step 1 (Next never pressed) → bookings → active → settings → support →
 * profile → logout → login-form verification.
 *
 * <p>Safe and deterministic. Booking submission, payment, Stripe, vehicle
 * onboarding, verification, role switch, and account deletion are NOT
 * performed here (gated or separately scoped).
 */
public class CustomerNavigationSmokeTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "android", "customer"})
    public void testCustomerSafeNavigationSmoke() {
        initializeAndroidDriver();

        CustomerAuthFlow auth = new CustomerAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        auth.loginAsCustomer();

        assertBusinessRule(auth.isAuthenticated(), "Customer leaves login screen after authorized login");
        testLogger.businessCheckpoint("AUTH_STATE", "Customer authenticated landing state reached");
        finalizeAssertions();

        String displayName = System.getenv("CUSTOMER_DISPLAY_NAME");
        String name = displayName != null && !displayName.isBlank() ? displayName : "Carl Customer";

        CustomerNavigationFlow nav = new CustomerNavigationFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);

        nav.verifyHome(name);

        nav.openCarWashStep1();
        nav.backToHomeFromPushed();

        nav.openBookings();
        nav.openActiveBookings();
        nav.openSettings();
        nav.openSupport();
        nav.homeViaTab();

        nav.openProfile();

        auth.logout();
        nav.toLoginFromRoleSelection();

        assertBusinessRule(!auth.isAuthenticated(), "Customer session ends at the login screen after logout");
        testLogger.businessCheckpoint("SMOKE_DONE", "Customer safe-navigation smoke completed");
        finalizeAssertions();
    }
}

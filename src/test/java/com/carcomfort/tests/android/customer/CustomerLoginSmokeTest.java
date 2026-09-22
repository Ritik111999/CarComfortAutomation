package com.carcomfort.tests.android.customer;

import com.carcomfort.flows.CustomerAuthFlow;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * First real-device Customer smoke: launch → role select → login → authenticated home
 * (personalized greeting + service cards) → profile entry.
 * Safe and repeatable. Booking/payment/final-commit actions are NOT performed here.
 */
public class CustomerLoginSmokeTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "android", "customer"})
    public void testCustomerLoginReachesAuthenticatedState() {
        initializeAndroidDriver();

        CustomerAuthFlow auth = new CustomerAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        auth.loginAsCustomer();

        assertBusinessRule(auth.isAuthenticated(), "Customer leaves login screen after authorized login");
        testLogger.businessCheckpoint("AUTH_STATE", "Customer authenticated landing state reached");
        finalizeAssertions();

        String displayName = System.getenv("CUSTOMER_DISPLAY_NAME");
        auth.verifyCustomerHome(displayName != null && !displayName.isBlank() ? displayName : "Carl Customer");

        auth.openProfile();

        auth.logout();
    }
}

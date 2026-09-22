package com.carcomfort.tests.android.provider;

import com.carcomfort.flows.ProviderAuthFlow;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * First real-device Provider smoke: role select → provider login → home
 * (personalized greeting) → profile (verified badge, onboarding) → settings
 * tab → logout → role selection.
 * Job accept/reject, payments, verification, and detail expanders are NOT
 * performed (gated or future work).
 */
public class ProviderLoginSmokeTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "android", "provider"})
    public void testProviderLoginReachesAuthenticatedState() {
        initializeAndroidDriver();

        ProviderAuthFlow auth = new ProviderAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        auth.loginAsProvider();

        assertBusinessRule(auth.isAuthenticated(), "Provider leaves login screen after authorized login");
        testLogger.businessCheckpoint("AUTH_STATE", "Provider authenticated landing state reached");
        finalizeAssertions();

        String displayName = System.getenv("PROVIDER_DISPLAY_NAME");
        auth.verifyProviderHome(displayName != null && !displayName.isBlank() ? displayName : "karl Driver");

        auth.openProfile();
        auth.backToHomeFromProfile();
        auth.openSettings();
        auth.logout();
    }
}

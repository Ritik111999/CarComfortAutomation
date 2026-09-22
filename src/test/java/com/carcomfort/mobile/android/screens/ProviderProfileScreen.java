package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.GestureHelper;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-PROFILE-001 — Service Provider Profile (verified driver, onboarding,
 * expandable detail rows). Discovered 2026-09-22, app v1.1.1.
 *
 * <p>Discovery boundary: Account/Vehicle/Documents expanders were NOT opened
 * (license, background-check, and contact PII inside — unnecessary for smoke).
 * Logout lives below the fold; reached via explicit scroll.
 */
public final class ProviderProfileScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Service Provider Profile");
    private static final By VERIFIED_BADGE = LocatorFactory.accessibilityId("VERIFIED DRIVER");
    private static final By ONBOARDING_DONE = LocatorFactory.accessibilityId("All 8 steps complete");
    private static final By LOG_OUT = LocatorFactory.accessibilityId("Log Out");
    private static final By LOGOUT_CONFIRM_TEXT = LocatorFactory.uiAutomator(
            "new UiSelector().descriptionContains(\"Are You sure you want to logout\")");
    private static final By LOGOUT_CONFIRM_OKAY = LocatorFactory.accessibilityId("Okay");

    public ProviderProfileScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderProfile";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isVerifiedBadgeShown() {
        return !driverManager.getDriver().findElements(VERIFIED_BADGE).isEmpty();
    }

    public boolean isOnboardingCompleteShown() {
        return !driverManager.getDriver().findElements(ONBOARDING_DONE).isEmpty();
    }

    public void scrollToLogOut() {
        waitForContentLoaded();
        new GestureHelper(driverManager).scrollUntilVisible(LOG_OUT, 6);
    }

    /** Profile header renders instantly; content (badge, rows) loads async — wait for it. */
    public void waitForContentLoaded() {
        waits.waitLong(d -> org.openqa.selenium.support.ui.ExpectedConditions
                .visibilityOfElementLocated(VERIFIED_BADGE).apply(driverManager.getDriver()));
    }

    public void tapLogOut() {
        scrollToLogOut();
        click(LOG_OUT, "Provider Log Out button");
        if (!driverManager.getDriver().findElements(LOGOUT_CONFIRM_TEXT).isEmpty()) {
            click(LOGOUT_CONFIRM_OKAY, "Logout confirm Okay");
            log.debug("Provider logout confirmed via Okay");
        }
    }
}

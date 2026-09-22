package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-PROFILE-001 — Customer profile (avatar, referral, account list).
 * Discovered 2026-09-22, app v1.1.1. All rows expose stable content-desc.
 * "Payment Methods" row navigates toward GATED payment setup — automation stops
 * at row presence; entering card/bank flows requires explicit authorization.
 */
public final class CustomerProfileScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Profile");
    private static final By PERSONAL_INFO = LocatorFactory.accessibilityId("Personal Information");
    private static final By MY_VEHICLES = LocatorFactory.accessibilityId("My Vehicles\n3");
    private static final By PAYMENT_METHODS = LocatorFactory.accessibilityId("Payment Methods");
    private static final By LOG_OUT = LocatorFactory.accessibilityId("Log Out");
    private static final By LOGOUT_CONFIRM_TEXT = LocatorFactory.uiAutomator(
            "new UiSelector().descriptionContains(\"Are You sure you want to logout\")");
    private static final By LOGOUT_CONFIRM_OKAY = LocatorFactory.accessibilityId("Okay");

    public CustomerProfileScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerProfile";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isPaymentMethodsRowPresent() {
        return !driverManager.getDriver().findElements(PAYMENT_METHODS).isEmpty();
    }

    public void tapLogOut() {
        click(LOG_OUT, "Log Out button");
        // Confirmation dialog "Are You sure you want to logout" → Okay (authorized).
        if (!driverManager.getDriver().findElements(LOGOUT_CONFIRM_TEXT).isEmpty()) {
            click(LOGOUT_CONFIRM_OKAY, "Logout confirm Okay");
            log.debug("Logout confirmed via Okay");
        }
    }
}

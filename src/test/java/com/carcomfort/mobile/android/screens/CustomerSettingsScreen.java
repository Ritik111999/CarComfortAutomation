package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-SETTINGS-001/002 — Customer Settings tab (presence-only rows).
 * Mapped 2026-09-22. Payment Methods / Switch to Service Provider /
 * Delete Account are GATED or out-of-scope — never tapped. Logout is the
 * authorized teardown path.
 */
public final class CustomerSettingsScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Settings");
    private static final By SWITCH_PROVIDER = LocatorFactory.accessibilityId("Switch to Service Provider");
    private static final By PAYMENT_METHODS = LocatorFactory.accessibilityId("Payment Methods");
    private static final By BOOKING_HISTORY = LocatorFactory.accessibilityId("Booking History");
    private static final By BILLING_HISTORY = LocatorFactory.accessibilityId("Billing History");
    private static final By NOTIFICATIONS_ROW = LocatorFactory.accessibilityId("Notifications");
    private static final By LOG_OUT = LocatorFactory.accessibilityId("Log Out");
    private static final By LOGOUT_CONFIRM_OKAY = LocatorFactory.accessibilityId("Okay");

    public CustomerSettingsScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerSettings";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isPaymentMethodsRowPresent() {
        return !driverManager.getDriver().findElements(PAYMENT_METHODS).isEmpty();
    }

    /** Structural identity: title load is proven by wait; rows prove the right settings list. */
    public boolean isCoreRowsPresent() {
        var driver = driverManager.getDriver();
        return !driver.findElements(SWITCH_PROVIDER).isEmpty()
                && !driver.findElements(BOOKING_HISTORY).isEmpty()
                && !driver.findElements(PAYMENT_METHODS).isEmpty();
    }

    public void tapLogOut() {
        click(LOG_OUT, "Log Out button");
        if (!driverManager.getDriver().findElements(LOGOUT_CONFIRM_OKAY).isEmpty()) {
            click(LOGOUT_CONFIRM_OKAY, "Logout confirm Okay");
            log.debug("Logout confirmed via Okay");
        }
    }
}

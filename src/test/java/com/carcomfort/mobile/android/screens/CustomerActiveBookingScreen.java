package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-ACTIVE-001 — Customer My Active Booking (empty state).
 * Mapped 2026-09-22. Read-only; "Start a New Booking" is presence-only
 * (entry to booking wizard, submission boundary GATED downstream).
 */
public final class CustomerActiveBookingScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("My Active Booking");
    private static final By EMPTY_STATE = LocatorFactory.accessibilityId("No Active Booking");
    private static final By START_NEW = LocatorFactory.accessibilityId("Start a New Booking");

    public CustomerActiveBookingScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerActiveBooking";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isEmptyStateShown() {
        return !driverManager.getDriver().findElements(EMPTY_STATE).isEmpty();
    }
}

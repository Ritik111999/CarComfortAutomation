package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-BOOKINGS-001 — Customer My Bookings (read-only list).
 * Mapped 2026-09-22. Cards show Confirmed/Completed/Cancelled states;
 * booking mutations are GATED by policy and never tapped from here.
 */
public final class CustomerBookingsScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("My Bookings");
    private static final By FILTER_ALL = LocatorFactory.accessibilityId("All");
    private static final By VIEW_DETAILS = LocatorFactory.accessibilityId("View Service Details");

    public CustomerBookingsScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerBookings";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public int viewDetailsCount() {
        return driverManager.getDriver().findElements(VIEW_DETAILS).size();
    }

    public boolean isFilterAllShown() {
        return !driverManager.getDriver().findElements(FILTER_ALL).isEmpty();
    }

    public void openFirstDetail() {
        click(VIEW_DETAILS, "First View Service Details");
    }
}

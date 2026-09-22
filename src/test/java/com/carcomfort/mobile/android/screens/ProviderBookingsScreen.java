package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-BOOKINGS-001 — Provider My Bookings (read-only list).
 * Mapped 2026-09-22, app v1.1.1. Detail entry only; job mutations are GATED by policy.
 */
public final class ProviderBookingsScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("My Bookings");
    private static final By FILTER_ALL = LocatorFactory.accessibilityId("All");
    private static final By VIEW_DETAILS = LocatorFactory.accessibilityId("View Service Details");

    public ProviderBookingsScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderBookings";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public int viewDetailsCount() {
        return driverManager.getDriver().findElements(VIEW_DETAILS).size();
    }

    public void openFirstDetail() {
        click(VIEW_DETAILS, "First View Service Details");
    }
}

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

    public boolean isFilterAllShown() {
        return !driverManager.getDriver().findElements(FILTER_ALL).isEmpty();
    }

    public void openFirstDetail() {
        click(VIEW_DETAILS, "First View Service Details");
    }

    /** Indexed detail entry for correlation scans (never blind position in tests). */
    public void openDetailAt(int index) {
        var cards = driverManager.getDriver().findElements(VIEW_DETAILS);
        if (index < 0 || index >= cards.size()) {
            throw new IllegalStateException("Booking card " + index + " unavailable (found " + cards.size() + ")");
        }
        cards.get(index).click();
        log.debug("Opened booking detail at index {}", index);
    }
}

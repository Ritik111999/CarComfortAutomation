package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-REQDETAIL-001/002 — Provider booking detail (read-only).
 * Mapped 2026-09-22. Completed booking shows no action buttons; job mutations
 * remain policy-GATED and are never tapped from here.
 */
public final class ProviderBookingDetailScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Service Details");
    private static final By BOOKING_ID_LABEL = LocatorFactory.accessibilityId("BOOKING ID");
    private static final By BOOKING_STATUS_LABEL = LocatorFactory.accessibilityId("BOOKING STATUS");

    public ProviderBookingDetailScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderBookingDetail";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isBookingMetaShown() {
        var d = driverManager.getDriver();
        return !d.findElements(BOOKING_ID_LABEL).isEmpty()
                && !d.findElements(BOOKING_STATUS_LABEL).isEmpty();
    }
}

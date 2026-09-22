package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-HELP-001 — Provider help/support (read-only informational).
 * Mapped 2026-09-22 via bottom tab4.
 */
public final class ProviderHelpScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Need Help?");
    private static final By SUBTITLE = LocatorFactory.accessibilityId("How we can help you?");

    public ProviderHelpScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderHelp";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isHelpContentShown() {
        return !driverManager.getDriver().findElements(SUBTITLE).isEmpty();
    }
}

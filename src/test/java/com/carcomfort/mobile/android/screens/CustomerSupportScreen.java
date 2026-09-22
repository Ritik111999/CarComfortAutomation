package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-SUPPORT-001 — Customer support (read-only informational).
 * Mapped 2026-09-22 via bottom tab4. Email/Chat/Call rows are
 * presence-only (no external intents fired).
 */
public final class CustomerSupportScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Support");
    private static final By SUBTITLE = LocatorFactory.accessibilityId("How we can help you?");

    public CustomerSupportScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerSupport";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isHelpContentShown() {
        return !driverManager.getDriver().findElements(SUBTITLE).isEmpty();
    }
}

package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.GestureHelper;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-SETTINGS-001/002 — Provider Settings tab (role switch, profile, payments,
 * wallet, history, support rows; logout below the fold). Mapped 2026-09-22, extended
 * with legal/support rows (presence-only).
 *
 * <p>Discovery boundary: Payment Methods / Wallet / File a Claim / Report
 * Accident rows are presence-noted only — entering them is GATED or future work.
 * Legal pages (Waiver/NDA/Contractor/Privacy/Vehicle/Terms), Earnings Statement,
 * Notifications, Tutorials, Learning Center, Profile Settings, Booking History
 * are presence-only candidates. "Switch to customer" is NOT tapped (role-switch
 * side effects need owner scoping). "Delete Account" is DESTRUCTIVE (never tapped).
 */
public final class ProviderSettingsScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Settings");
    private static final By SWITCH_TO_CUSTOMER = LocatorFactory.accessibilityId("Switch to customer");
    private static final By PROFILE_SETTINGS = LocatorFactory.accessibilityId("Profile Settings");
    private static final By PAYMENT_METHODS = LocatorFactory.accessibilityId("Payment Methods");
    private static final By WALLET = LocatorFactory.accessibilityId("Wallet");
    private static final By BOOKING_HISTORY = LocatorFactory.accessibilityId("Booking History");
    private static final By TUTORIALS = LocatorFactory.accessibilityId("Tutorials");
    private static final By LEARNING_CENTER = LocatorFactory.accessibilityId("Learning Center");
    private static final By FILE_CLAIM = LocatorFactory.accessibilityId("File a Claim");
    private static final By REPORT_ACCIDENT = LocatorFactory.accessibilityId("Report Accident/Damage");
    private static final By REPORT_MISSING = LocatorFactory.accessibilityId("Report Missing Item");
    private static final By EARNINGS_STATEMENT = LocatorFactory.accessibilityId("Earnings Statement (PDF)");
    private static final By NOTIFICATIONS_ROW = LocatorFactory.accessibilityId("Notifications");
    private static final By SUPPORT_ROW = LocatorFactory.accessibilityId("Support");
    private static final By DELETE_ACCOUNT = LocatorFactory.accessibilityId("Delete Account");
    private static final By LOG_OUT = LocatorFactory.accessibilityId("Logout");
    private static final By LOGOUT_CONFIRM_TEXT = LocatorFactory.uiAutomator(
            "new UiSelector().descriptionContains(\"Are You sure you want to logout\")");
    private static final By LOGOUT_CONFIRM_OKAY = LocatorFactory.accessibilityId("Okay");

    public ProviderSettingsScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderSettings";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isPaymentMethodsRowPresent() {
        return !driverManager.getDriver().findElements(PAYMENT_METHODS).isEmpty();
    }

    public void scrollToLogOut() {
        new GestureHelper(driverManager).scrollUntilVisible(LOG_OUT, 8);
    }

    public void tapLogOut() {
        scrollToLogOut();
        click(LOG_OUT, "Settings Log Out button");
        if (!driverManager.getDriver().findElements(LOGOUT_CONFIRM_TEXT).isEmpty()) {
            click(LOGOUT_CONFIRM_OKAY, "Logout confirm Okay");
            log.debug("Provider logout confirmed via Okay");
        }
    }
}

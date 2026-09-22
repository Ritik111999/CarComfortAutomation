package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-HOME-001 — Customer home (map + "Enter Service Details" sheet).
 * Discovered 2026-09-22, app v1.1.1 (Xiaomi 22021211RI, Android 14).
 *
 * <p>Weak semantics notes (filed for app improvement): bottom-nav icons and the
 * avatar ImageView expose no content-desc/resource-id — located by UiAutomator
 * class instance. Greeting text is time-dependent ("Good Morning,") — assert only
 * the stable personalized name part.
 */
public final class CustomerHomeScreen extends BaseAndroidScreen {

    private static final By SHEET_TITLE = LocatorFactory.accessibilityId("Enter Service Details");
    private static final By CAR_WASH_CARD = LocatorFactory.accessibilityId("Car Wash");
    private static final By EV_CHARGING_CARD = LocatorFactory.accessibilityId("EV Charging");
    private static final By COMBO_CARD = LocatorFactory.accessibilityId("EV charging & Car wash");
    /** Avatar: 4th ImageView in hierarchy order (no desc). Positional — see class note. */
    private static final By AVATAR =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(3)");
    /** Bottom nav icons (no desc): instances 5,6,7,8 in hierarchy order. Positional. */
    private static final By NAV_BOOKINGS =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(6)");
    private static final By NAV_SETTINGS =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(7)");

    public CustomerHomeScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerHome";
    }

    @Override
    public By getUniqueLocator() {
        return SHEET_TITLE;
    }

    public By greetingFor(String displayName) {
        return LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(" + quoted(displayName) + ")");
    }

    public boolean isGreetingShown(String displayName) {
        return !driverManager.getDriver().findElements(greetingFor(displayName)).isEmpty();
    }

    public void tapCarWash() {
        click(CAR_WASH_CARD, "Car Wash service card");
    }

    public void tapEvCharging() {
        click(EV_CHARGING_CARD, "EV Charging service card");
    }

    public void tapAvatar() {
        click(AVATAR, "Profile avatar");
    }

    public void tapNavBookings() {
        click(NAV_BOOKINGS, "Bottom nav bookings (positional instance 6)");
    }

    public void tapNavSettings() {
        click(NAV_SETTINGS, "Bottom nav settings (positional instance 7)");
    }

    private static String quoted(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

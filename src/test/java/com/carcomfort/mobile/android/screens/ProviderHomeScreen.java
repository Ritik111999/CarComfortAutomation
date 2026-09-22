package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-HOME-001 — Provider home (map + greeting, no service sheet on top level).
 * Discovered 2026-09-22, app v1.1.1. Same weak-semantics pattern as customer home:
 * avatar and bottom-nav icons expose no content-desc (positional UiAutomator instances).
 */
public final class ProviderHomeScreen extends BaseAndroidScreen {

    /** Avatar: 4th ImageView in hierarchy order (no desc). Positional. */
    private static final By AVATAR =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(3)");
    /** Bottom-nav settings gear: instance 7 in hierarchy order (no desc). Positional. */
    private static final By NAV_SETTINGS =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(7)");
    /** Bottom-nav home: instance 5 in hierarchy order (no desc). Positional. */
    private static final By NAV_HOME =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(5)");

    public ProviderHomeScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderHome";
    }

    @Override
    public By getUniqueLocator() {
        // Time-independent role marker: greeting prefix changes (Morning/Afternoon),
        // but the provider greeting always ends with "Driver!".
        return LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"Driver!\")");
    }

    public By greetingFor(String displayName) {
        return LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(" + quoted(displayName) + ")");
    }

    public boolean isGreetingShown(String displayName) {
        return !driverManager.getDriver().findElements(greetingFor(displayName)).isEmpty();
    }

    public void tapAvatar() {
        click(AVATAR, "Provider profile avatar");
    }

    public void tapNavSettings() {
        click(NAV_SETTINGS, "Bottom nav settings (positional instance 7)");
    }

    public void tapNavHome() {
        click(NAV_HOME, "Bottom nav home (positional instance 5)");
    }

    private static String quoted(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

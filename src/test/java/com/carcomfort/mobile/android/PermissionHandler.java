package com.carcomfort.mobile.android;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles Android runtime permission dialogs and system popups safely.
 * Dismisses only known system permission UI; never bypasses product auth/KYC.
 */
public final class PermissionHandler {
    private static final Logger log = LoggerFactory.getLogger(PermissionHandler.class);

    private static final List<By> ALLOW_BUTTONS = List.of(
            LocatorFactory.resourceId("com.android.permissioncontroller:id/permission_allow_button"),
            LocatorFactory.resourceId("com.android.packageinstaller:id/permission_allow_button"),
            LocatorFactory.stableText("Allow"),
            LocatorFactory.stableText("ALLOW"));

    private static final List<By> DENY_BUTTONS = List.of(
            LocatorFactory.resourceId("com.android.permissioncontroller:id/permission_deny_button"),
            LocatorFactory.stableText("Deny"));

    private final AndroidDriverManager drivers;

    public PermissionHandler(AndroidDriverManager drivers) {
        this.drivers = drivers;
    }

    /** Returns true if a permission dialog was present and handled. */
    public boolean handlePermissionIfPresent(boolean grant) {
        List<By> candidates = grant ? ALLOW_BUTTONS : DENY_BUTTONS;
        for (By locator : candidates) {
            List<WebElement> matches = drivers.getDriver().findElements(locator);
            if (!matches.isEmpty() && matches.get(0).isDisplayed()) {
                matches.get(0).click();
                log.info("Handled system permission dialog (grant={}) via {}", grant, locator);
                return true;
            }
        }
        return false;
    }

    public void dismissSystemPopupIfPresent() {
        for (By locator : List.of(
                LocatorFactory.stableText("Close"),
                LocatorFactory.stableText("Dismiss"),
                LocatorFactory.stableText("OK"))) {
            List<WebElement> matches = drivers.getDriver().findElements(locator);
            if (!matches.isEmpty() && matches.get(0).isDisplayed()) {
                log.debug("System popup present: {}", locator);
                return;
            }
        }
    }

    public boolean isKeyboardShown() {
        try {
            return drivers.getDriver().isKeyboardShown();
        } catch (Exception e) {
            return false;
        }
    }

    public void hideKeyboardIfShown() {
        try {
            if (isKeyboardShown()) {
                drivers.getDriver().hideKeyboard();
                log.debug("Keyboard hidden");
            }
        } catch (Exception e) {
            log.debug("hideKeyboard not applicable", e);
        }
    }
}

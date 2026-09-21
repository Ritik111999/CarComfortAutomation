package com.carcomfort.core.waits;

import com.carcomfort.core.driver.AndroidDriverManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Android-specific wait strategies.
 */
public final class AndroidWaitStrategies {
    private static final Logger log = LoggerFactory.getLogger(AndroidWaitStrategies.class);

    private final AndroidDriverManager driverManager;
    private final WaitStrategies baseWaits;

    public AndroidWaitStrategies(AndroidDriverManager driverManager) {
        this.driverManager = driverManager;
        this.baseWaits = new WaitStrategies();
    }

    public void waitForActivity(String expectedActivity) {
        baseWaits.waitFor(ctx -> {
            try {
                String currentActivity = (String) driverManager.getDriver().executeScript("mobile: shell", java.util.Map.of(
                        "command", "dumpsys activity activities | grep mResumedActivity"
                ));
                return expectedActivity.equals(currentActivity) ? Boolean.TRUE : null;
            } catch (Exception e) {
                return null;
            }
        }, "activity: " + expectedActivity);
    }

    public void waitForAnyActivity(List<String> expectedActivities) {
        baseWaits.waitFor(ctx -> {
            try {
                String currentActivity = (String) driverManager.getDriver().executeScript("mobile: shell", java.util.Map.of(
                        "command", "dumpsys activity activities | grep mResumedActivity"
                ));
                return expectedActivities.contains(currentActivity) ? Boolean.TRUE : null;
            } catch (Exception e) {
                return null;
            }
        }, "any activity: " + expectedActivities);
    }

    public void waitForContext(String expectedContext) {
        baseWaits.waitFor(ctx -> {
            String currentContext = driverManager.getDriver().getContext();
            return expectedContext.equals(currentContext) ? Boolean.TRUE : null;
        }, "context: " + expectedContext);
    }

    public void waitForWebViewContext() {
        baseWaits.waitFor(ctx -> {
            for (String context : driverManager.getDriver().getContextHandles()) {
                if (context.toLowerCase().contains("webview")) {
                    return Boolean.TRUE;
                }
            }
            return null;
        }, "WebView context");
    }

    public void waitForNativeContext() {
        baseWaits.waitFor(ctx -> {
            String currentContext = driverManager.getDriver().getContext();
            return "NATIVE_APP".equals(currentContext) ? Boolean.TRUE : null;
        }, "NATIVE_APP context");
    }

    public WebElement waitForVisibleByAccessibilityId(String accessibilityId) {
        return baseWaits.waitForVisible(driverManager.getDriver(), org.openqa.selenium.By.xpath("//*[@content-desc='" + accessibilityId + "']"));
    }

    public WebElement waitForClickableByAccessibilityId(String accessibilityId) {
        return baseWaits.waitForClickable(driverManager.getDriver(), org.openqa.selenium.By.xpath("//*[@content-desc='" + accessibilityId + "']"));
    }

    public WebElement waitForVisibleByResourceId(String resourceId) {
        return baseWaits.waitForVisible(driverManager.getDriver(), org.openqa.selenium.By.id(resourceId));
    }

    public WebElement waitForClickableByResourceId(String resourceId) {
        return baseWaits.waitForClickable(driverManager.getDriver(), org.openqa.selenium.By.id(resourceId));
    }

    public WebElement waitForVisibleByText(String text) {
        return baseWaits.waitForVisible(driverManager.getDriver(), org.openqa.selenium.By.xpath("//*[@text='" + text + "']"));
    }

    public WebElement waitForClickableByText(String text) {
        return baseWaits.waitForClickable(driverManager.getDriver(), org.openqa.selenium.By.xpath("//*[@text='" + text + "']"));
    }

    public WebElement waitForVisibleByUiAutomator(String uiautomatorSelector) {
        return baseWaits.waitForVisible(driverManager.getDriver(), new io.appium.java_client.AppiumBy.ByAndroidUIAutomator(uiautomatorSelector));
    }

    public WebElement waitForClickableByUiAutomator(String uiautomatorSelector) {
        return baseWaits.waitForClickable(driverManager.getDriver(), new io.appium.java_client.AppiumBy.ByAndroidUIAutomator(uiautomatorSelector));
    }

    public void waitForToast(String message) {
        baseWaits.waitForToast(driverManager.getDriver(), message);
    }

    public void waitForSnackbar(String message) {
        baseWaits.waitForSnackbar(driverManager.getDriver(), message);
    }

    public void waitForKeyboardToHide() {
        baseWaits.waitFor(ctx -> {
            return !driverManager.getDriver().isKeyboardShown() ? Boolean.TRUE : null;
        }, "keyboard to hide");
    }

    public void waitForKeyboardToShow() {
        baseWaits.waitFor(ctx -> {
            return driverManager.getDriver().isKeyboardShown() ? Boolean.TRUE : null;
        }, "keyboard to show");
    }

    public void waitForNetworkIdle(int idleMs) {
        try {
            Thread.sleep(idleMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
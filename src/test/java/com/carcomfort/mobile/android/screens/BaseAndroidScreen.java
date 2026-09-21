package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.waits.AndroidWaitStrategies;
import com.carcomfort.core.waits.WaitStrategies;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Android screen/page objects.
 */
public abstract class BaseAndroidScreen {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final AndroidDriverManager driverManager;
    protected final WaitStrategies waits;
    protected final AndroidWaitStrategies androidWaits;

    protected BaseAndroidScreen(AndroidDriverManager driverManager) {
        this.driverManager = driverManager;
        this.waits = new WaitStrategies();
        this.androidWaits = new AndroidWaitStrategies(driverManager);
    }

    protected AndroidDriverManager getDriverManager() {
        return driverManager;
    }

    protected WaitStrategies getWaits() {
        return waits;
    }

    protected AndroidWaitStrategies getAndroidWaits() {
        return androidWaits;
    }

    public abstract String getScreenName();

    public abstract By getUniqueLocator();

    public void waitForScreenLoaded() {
        waits.waitForVisible(driverManager.getDriver(), getUniqueLocator(), getScreenName() + " loaded");
        log.debug("Screen loaded: {}", getScreenName());
    }

    public boolean isScreenDisplayed() {
        try {
            waits.waitShort(ctx -> driverManager.getDriver().findElements(getUniqueLocator()).stream().findFirst().orElse(null));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected WebElement find(By locator) {
        return driverManager.getDriver().findElement(locator);
    }

    protected WebElement findVisible(By locator) {
        return waits.waitForVisible(driverManager.getDriver(), locator);
    }

    protected WebElement findClickable(By locator) {
        return waits.waitForClickable(driverManager.getDriver(), locator);
    }

    protected void click(By locator) {
        findClickable(locator).click();
    }

    protected void click(By locator, String description) {
        WebElement element = waits.waitForClickable(driverManager.getDriver(), locator, description);
        element.click();
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = findVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return findVisible(locator).getText();
    }

    protected String getAttribute(By locator, String attribute) {
        return findVisible(locator).getAttribute(attribute);
    }

    protected boolean isDisplayed(By locator) {
        try {
            return findVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForDisappeared(By locator) {
        waits.waitForDisappeared(driverManager.getDriver(), locator);
    }

    protected void scrollTo(By locator) {
        String uiautomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
                "new UiSelector()." + toUiSelector(locator) + ")";
        driverManager.getDriver().findElement(new io.appium.java_client.AppiumBy.ByAndroidUIAutomator(uiautomator));
    }

    private String toUiSelector(By locator) {
        if (locator.toString().contains("id")) {
            String id = locator.toString().split("id: ")[1].replace("]", "");
            return "resourceId(\"" + id + "\")";
        } else if (locator.toString().contains("xpath")) {
            String xpath = locator.toString().split("xpath: ")[1].replace("]", "");
            return "xpath(\"" + xpath + "\")";
        }
        return "className(\"android.widget.TextView\")";
    }

    public void navigateBack() {
        driverManager.getDriver().navigate().back();
    }

    public void hideKeyboard() {
        if (driverManager.getDriver().isKeyboardShown()) {
            driverManager.getDriver().hideKeyboard();
        }
    }
}
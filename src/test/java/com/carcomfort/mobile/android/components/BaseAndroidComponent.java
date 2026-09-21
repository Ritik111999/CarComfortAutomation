package com.carcomfort.mobile.android.components;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.waits.WaitStrategies;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for reusable Android component objects.
 */
public abstract class BaseAndroidComponent {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final AndroidDriverManager driverManager;
    protected final WaitStrategies waits;
    protected final WebElement rootElement;

    protected BaseAndroidComponent(AndroidDriverManager driverManager, WebElement rootElement) {
        this.driverManager = driverManager;
        this.waits = new WaitStrategies();
        this.rootElement = rootElement;
    }

    protected BaseAndroidComponent(AndroidDriverManager driverManager, By rootLocator) {
        this(driverManager, driverManager.getDriver().findElement(rootLocator));
    }

    protected WebElement getRootElement() {
        return rootElement;
    }

    protected WebElement find(By locator) {
        return rootElement.findElement(locator);
    }

    protected WebElement findVisible(By locator) {
        return waits.waitForVisible(rootElement, locator);
    }

    protected WebElement findClickable(By locator) {
        return waits.waitForClickable(rootElement, locator);
    }

    protected void click(By locator) {
        findClickable(locator).click();
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

    public abstract String getComponentName();
}
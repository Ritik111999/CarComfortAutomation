package com.carcomfort.web.customer;

import com.carcomfort.core.driver.WebDriverManager;
import com.carcomfort.core.waits.WaitStrategies;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Customer PWA page objects.
 */
public abstract class BaseCustomerPage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final WebDriverManager driverManager;
    protected final WaitStrategies waits;

    protected BaseCustomerPage(WebDriverManager driverManager) {
        this.driverManager = driverManager;
        this.waits = new WaitStrategies();
    }

    protected WebDriver getDriver() {
        return driverManager.getDriver();
    }

    protected WaitStrategies getWaits() {
        return waits;
    }

    public abstract String getPageName();

    public abstract By getUniqueLocator();

    public void waitForPageLoaded() {
        waits.waitForVisible(getDriver(), getUniqueLocator(), getPageName() + " loaded");
        waits.waitForPageLoad(getDriver());
        log.debug("Page loaded: {}", getPageName());
    }

    public boolean isPageDisplayed() {
        try {
            waits.waitShort(ctx -> getDriver().findElements(getUniqueLocator()).stream().findFirst().orElse(null));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected WebElement find(By locator) {
        return getDriver().findElement(locator);
    }

    protected WebElement findVisible(By locator) {
        return waits.waitForVisible(getDriver(), locator);
    }

    protected WebElement findClickable(By locator) {
        return waits.waitForClickable(getDriver(), locator);
    }

    protected void click(By locator) {
        findClickable(locator).click();
    }

    protected void click(By locator, String description) {
        WebElement element = waits.waitForClickable(getDriver(), locator, description);
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
        waits.waitForDisappeared(getDriver(), locator);
    }

    protected void navigateTo(String url) {
        getDriver().get(url);
        log.debug("Navigated to: {}", url);
    }

    public void navigateToBaseUrl() {
        driverManager.navigateToBaseUrl();
    }
}
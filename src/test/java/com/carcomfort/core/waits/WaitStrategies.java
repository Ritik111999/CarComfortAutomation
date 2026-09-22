package com.carcomfort.core.waits;

import com.carcomfort.core.config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable explicit wait strategies. Strictly prohibits Thread.sleep().
 */
public final class WaitStrategies {
    private static final Logger log = LoggerFactory.getLogger(WaitStrategies.class);

    private final Wait<WebDriver> defaultWait;
    private final Wait<WebDriver> longWait;
    private final Wait<WebDriver> shortWait;

    public WaitStrategies() {
        this.defaultWait = createWait(FrameworkConfig.getInt("carcomfort.waits.defaultTimeoutSeconds"));
        this.longWait = createWait(FrameworkConfig.getInt("carcomfort.waits.longTimeoutSeconds"));
        this.shortWait = createWait(FrameworkConfig.getInt("carcomfort.waits.shortTimeoutSeconds"));
    }

    public WaitStrategies(int defaultSeconds, int longSeconds, int shortSeconds) {
        this.defaultWait = createWait(defaultSeconds);
        this.longWait = createWait(longSeconds);
        this.shortWait = createWait(shortSeconds);
    }

    @SuppressWarnings("unchecked")
    private Wait<WebDriver> createWait(int timeoutSeconds) {
        Class<? extends Throwable>[] exceptionClasses = FrameworkConfig.getStringList("carcomfort.waits.ignoreExceptions").stream()
                .map(this::resolveExceptionClass)
                .toArray(Class[]::new);
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(null)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(FrameworkConfig.getLong("carcomfort.waits.pollingIntervalMs")));
        if (exceptionClasses.length > 0) {
            wait.ignoring(exceptionClasses[0]);
            for (int i = 1; i < exceptionClasses.length; i++) {
                wait.ignoring(exceptionClasses[i]);
            }
        }
        return wait;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> resolveExceptionClass(String className) {
        try {
            return (Class<? extends Throwable>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.warn("Exception class not found: {}", className);
            return RuntimeException.class;
        }
    }

    /**
     * NOTE: FluentWait instances are constructed without an input driver, so generic
     * {@code waitFor(Function)} lambdas MUST capture their driver explicitly and ignore
     * the lambda argument. All driver-taking helpers below bind the passed driver.
     */
    public <T> T waitFor(Function<WebDriver, T> condition) {
        return defaultWait.until(condition);
    }

    public <T> T waitFor(Function<WebDriver, T> condition, String description) {
        log.debug("Waiting for: {}", description);
        return defaultWait.until(condition);
    }

    public <T> T waitLong(Function<WebDriver, T> condition) {
        return longWait.until(condition);
    }

    public <T> T waitShort(Function<WebDriver, T> condition) {
        return shortWait.until(condition);
    }

    public WebElement waitForVisible(WebDriver driver, By locator) {
        return defaultWait.until(d -> ExpectedConditions.visibilityOfElementLocated(locator).apply(driver));
    }

    public WebElement waitForVisible(WebDriver driver, By locator, String description) {
        log.debug("Waiting for visible: {}", description);
        return defaultWait.until(d -> ExpectedConditions.visibilityOfElementLocated(locator).apply(driver));
    }

    public WebElement waitForVisible(org.openqa.selenium.SearchContext context, By locator) {
        return defaultWait.until(d -> {
            List<WebElement> els = context.findElements(locator);
            for (WebElement el : els) {
                try {
                    if (el.isDisplayed()) {
                        return el;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    return null;
                }
            }
            return null;
        });
    }

    public WebElement waitForVisible(org.openqa.selenium.SearchContext context, By locator, String description) {
        log.debug("Waiting for visible: {}", description);
        return waitForVisible(context, locator);
    }

    public WebElement waitForClickable(WebDriver driver, By locator) {
        return defaultWait.until(d -> ExpectedConditions.elementToBeClickable(locator).apply(driver));
    }

    public WebElement waitForClickable(WebDriver driver, By locator, String description) {
        log.debug("Waiting for clickable: {}", description);
        return defaultWait.until(d -> ExpectedConditions.elementToBeClickable(locator).apply(driver));
    }

    public WebElement waitForClickable(org.openqa.selenium.SearchContext context, By locator) {
        return defaultWait.until(d -> {
            List<WebElement> els = context.findElements(locator);
            for (WebElement el : els) {
                try {
                    if (el.isDisplayed() && el.isEnabled()) {
                        return el;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    return null;
                }
            }
            return null;
        });
    }

    public WebElement waitForClickable(org.openqa.selenium.SearchContext context, By locator, String description) {
        log.debug("Waiting for clickable: {}", description);
        return waitForClickable(context, locator);
    }

    public WebElement waitForPresent(WebDriver driver, By locator) {
        return defaultWait.until(d -> ExpectedConditions.presenceOfElementLocated(locator).apply(driver));
    }

    public List<WebElement> waitForAllPresent(WebDriver driver, By locator) {
        return defaultWait.until(d -> ExpectedConditions.presenceOfAllElementsLocatedBy(locator).apply(driver));
    }

    public void waitForDisappeared(WebDriver driver, By locator) {
        defaultWait.until(d -> ExpectedConditions.invisibilityOfElementLocated(locator).apply(driver));
    }

    public void waitForDisappeared(WebDriver driver, By locator, String description) {
        log.debug("Waiting for disappeared: {}", description);
        defaultWait.until(d -> ExpectedConditions.invisibilityOfElementLocated(locator).apply(driver));
    }

    public void waitForText(WebDriver driver, By locator, String expectedText) {
        defaultWait.until(d -> {
            WebElement element = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
            return element != null && element.getText().contains(expectedText) ? element : null;
        });
    }

    public void waitForTextToChange(WebDriver driver, By locator, String previousText) {
        defaultWait.until(d -> {
            WebElement element = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
            return element != null && !element.getText().equals(previousText) ? element : null;
        });
    }

    public void waitForAttribute(WebDriver driver, By locator, String attribute, String expectedValue) {
        defaultWait.until(d -> {
            WebElement element = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
            return element != null && expectedValue.equals(element.getAttribute(attribute)) ? element : null;
        });
    }

    public void waitForCondition(Predicate<WebDriver> condition, String description) {
        log.debug("Waiting for condition: {}", description);
        throw new UnsupportedOperationException(
                "waitForCondition(Predicate) cannot bind a driver implicitly. "
                        + "Use waitFor(driver -> condition.test(driver) ? Boolean.TRUE : null, description) with an explicit driver instead.");
    }

    public void waitForActivity(String expectedActivity) {
        // For Android - implemented in Android-specific wait utilities
        throw new UnsupportedOperationException("Use AndroidWaitStrategies for activity waits");
    }

    public void waitForLoaderToDisappear(WebDriver driver, By loaderLocator) {
        waitForDisappeared(driver, loaderLocator, "loader to disappear");
    }

    public void waitForToast(WebDriver driver, String message) {
        By toastLocator = By.xpath("//*[@class='android.widget.Toast' and contains(@text, '" + message + "')]");
        waitForVisible(driver, toastLocator, "toast: " + message);
    }

    public void waitForSnackbar(WebDriver driver, String message) {
        By snackbarLocator = By.xpath("//*[contains(@class, 'Snackbar') and contains(@text, '" + message + "')]");
        waitForVisible(driver, snackbarLocator, "snackbar: " + message);
    }

    public void waitForUrlContains(WebDriver driver, String urlFragment) {
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(FrameworkConfig.getInt("carcomfort.waits.defaultTimeoutSeconds")))
                .pollingEvery(Duration.ofMillis(FrameworkConfig.getLong("carcomfort.waits.pollingIntervalMs")))
                .until(d -> d.getCurrentUrl().contains(urlFragment));
    }

    public void waitForUrlMatches(WebDriver driver, String regex) {
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(FrameworkConfig.getInt("carcomfort.waits.defaultTimeoutSeconds")))
                .pollingEvery(Duration.ofMillis(FrameworkConfig.getLong("carcomfort.waits.pollingIntervalMs")))
                .until(d -> d.getCurrentUrl().matches(regex));
    }

    public void waitForPageLoad(WebDriver driver) {
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(FrameworkConfig.getInt("carcomfort.waits.longTimeoutSeconds")))
                .pollingEvery(Duration.ofMillis(FrameworkConfig.getLong("carcomfort.waits.pollingIntervalMs")))
                .until(d -> "complete".equals(((org.openqa.selenium.JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    public static void sleep(Duration duration) {
        throw new UnsupportedOperationException("Thread.sleep() is prohibited. Use explicit waits instead.");
    }

    public static void sleep(long millis) {
        throw new UnsupportedOperationException("Thread.sleep() is prohibited. Use explicit waits instead.");
    }
}
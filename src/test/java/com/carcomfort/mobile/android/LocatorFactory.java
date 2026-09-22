package com.carcomfort.mobile.android;

import com.carcomfort.core.driver.AndroidDriverManager;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Enforces the locator priority for the Flutter black-box strategy:
 * accessibility-id &gt; resource-id &gt; stable text &gt; UiAutomator &gt; constrained XPath.
 * Coordinate interaction is never offered as a primary strategy.
 */
public final class LocatorFactory {
    private static final Logger log = LoggerFactory.getLogger(LocatorFactory.class);

    private LocatorFactory() {}

    public static By accessibilityId(String contentDesc) {
        log.debug("Locator [accessibility-id]: {}", contentDesc);
        return AppiumBy.accessibilityId(contentDesc);
    }

    public static By resourceId(String resourceId) {
        log.debug("Locator [resource-id]: {}", resourceId);
        return By.id(resourceId);
    }

    public static By stableText(String text) {
        log.debug("Locator [stable-text]: {}", text);
        return By.xpath("//*[@text=" + xpathLiteral(text) + "]");
    }

    public static By uiAutomator(String selector) {
        log.debug("Locator [uiautomator]: {}", selector);
        return new AppiumBy.ByAndroidUIAutomator(selector);
    }

    public static By uiAutomatorText(String text) {
        return uiAutomator("new UiSelector().text(" + javaLiteral(text) + ")");
    }

    public static By uiAutomatorDescription(String contentDesc) {
        return uiAutomator("new UiSelector().description(" + javaLiteral(contentDesc) + ")");
    }

    /**
     * Constrained XPath only: caller must scope to a class + stable attribute.
     * Throws if the expression looks unconstrained (bare //* or text-only contains()).
     */
    public static By constrainedXPath(String xpath) {
        if (xpath == null || xpath.isBlank() || xpath.trim().equals("//*")
                || xpath.matches("(?s).*//\\*\\[contains\\(@text.*")) {
            throw new IllegalArgumentException(
                    "Unconstrained XPath rejected. Scope by class + stable attribute: " + xpath);
        }
        log.debug("Locator [constrained-xpath]: {}", xpath);
        return By.xpath(xpath);
    }

    private static String xpathLiteral(String s) {
        if (!s.contains("'")) {
            return "'" + s + "'";
        }
        return "concat('" + s.replace("'", "',\"'\",'") + "')";
    }

    private static String javaLiteral(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** Convenience: tap an element located via the priority chain. */
    public static void tap(AndroidDriverManager drivers, By locator, String description) {
        WebElement el = drivers.getDriver().findElement(locator);
        el.click();
        log.debug("Tapped: {}", description);
    }

    public static List<WebElement> findAll(AndroidDriverManager drivers, By locator) {
        return drivers.getDriver().findElements(locator);
    }

    public static Dimension screenSize(AndroidDriverManager drivers) {
        return drivers.getDriver().manage().window().getSize();
    }

    public static void swipeW3C(AndroidDriverManager drivers, int startX, int startY,
                                int endX, int endY, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        drivers.getDriver().perform(List.of(swipe));
    }
}

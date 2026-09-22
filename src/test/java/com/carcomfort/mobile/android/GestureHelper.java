package com.carcomfort.mobile.android;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.waits.AndroidWaitStrategies;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Gesture automation via W3C actions. Element-anchored gestures are preferred;
 * screen-percentage swipes are provided for scrollable containers only.
 * No coordinate-based taps as a primary strategy.
 */
public final class GestureHelper {
    private static final Logger log = LoggerFactory.getLogger(GestureHelper.class);

    private final AndroidDriverManager drivers;

    public GestureHelper(AndroidDriverManager drivers) {
        this.drivers = drivers;
    }

    public void swipeUp() {
        swipeVertical(0.8, 0.2);
    }

    public void swipeDown() {
        swipeVertical(0.2, 0.8);
    }

    public void swipeLeft() {
        swipeHorizontal(0.8, 0.2);
    }

    public void swipeRight() {
        swipeHorizontal(0.2, 0.8);
    }

    private void swipeVertical(double fromPct, double toPct) {
        Dimension size = LocatorFactory.screenSize(drivers);
        int x = size.width / 2;
        int startY = (int) (size.height * fromPct);
        int endY = (int) (size.height * toPct);
        LocatorFactory.swipeW3C(drivers, x, startY, x, endY, Duration.ofMillis(800));
        log.debug("Vertical swipe {} -> {}", fromPct, toPct);
    }

    private void swipeHorizontal(double fromPct, double toPct) {
        Dimension size = LocatorFactory.screenSize(drivers);
        int y = size.height / 2;
        int startX = (int) (size.width * fromPct);
        int endX = (int) (size.width * toPct);
        LocatorFactory.swipeW3C(drivers, startX, y, endX, y, Duration.ofMillis(800));
        log.debug("Horizontal swipe {} -> {}", fromPct, toPct);
    }

    /**
     * Scrolls until the element is visible or attempts are exhausted.
     * Uses explicit presence checks between swipes — never fixed sleeps.
     */
    public boolean scrollUntilVisible(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            if (!drivers.getDriver().findElements(locator).isEmpty()) {
                return true;
            }
            swipeUp();
        }
        return !drivers.getDriver().findElements(locator).isEmpty();
    }
}

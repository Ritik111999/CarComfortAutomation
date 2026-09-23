package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * AND-CUST-BOOKINGS-001 — Customer My Bookings (read-only list).
 * Mapped 2026-09-22, enhanced 2026-09-23. Cards show Confirmed/Completed/Cancelled states;
 * booking mutations are GATED by policy and never tapped from here.
 */
public final class CustomerBookingsScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("My Bookings");
    private static final By FILTER_ALL = LocatorFactory.accessibilityId("All");
    private static final By VIEW_DETAILS = LocatorFactory.accessibilityId("View Service Details");

    public CustomerBookingsScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerBookings";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public int viewDetailsCount() {
        return driverManager.getDriver().findElements(VIEW_DETAILS).size();
    }

    public boolean isFilterAllShown() {
        return !driverManager.getDriver().findElements(FILTER_ALL).isEmpty();
    }

    public void openFirstDetail() {
        click(VIEW_DETAILS, "First View Service Details");
    }

    /** Opens the View Service Details at the given index. */
    public void openDetailAt(int index) {
        List<WebElement> buttons = driverManager.getDriver().findElements(VIEW_DETAILS);
        if (index < 0 || index >= buttons.size()) {
            throw new IndexOutOfBoundsException("Card index " + index + " out of bounds (" + buttons.size() + " cards)");
        }
        buttons.get(index).click();
        log.debug("Tapped View Service Details at index {}", index);
    }

    /** Returns all content-desc snippets of visible cards/elements. */
    public List<String> allDescs() {
        List<String> descs = new ArrayList<>();
        try {
            for (WebElement el : driverManager.getDriver().findElements(By.xpath("//*[@content-desc]"))) {
                try {
                    String d = el.getAttribute("content-desc");
                    if (d != null && !d.isBlank()) {
                        descs.add(d.trim());
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return descs;
    }

    /** Finds the card index that matches the given text markers. */
    public int findCardIndex(String... markers) {
        List<WebElement> buttons = driverManager.getDriver().findElements(VIEW_DETAILS);
        List<WebElement> descElements = driverManager.getDriver().findElements(
                LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"Car Wash\")"));
        
        for (int i = 0; i < buttons.size(); i++) {
            // Check if any matching description is within proximity of this button
            for (WebElement el : descElements) {
                String desc = el.getAttribute("content-desc");
                if (desc == null) continue;
                boolean allMatch = true;
                for (String m : markers) {
                    if (m != null && !m.isBlank() && !desc.contains(m)) {
                        allMatch = false;
                        break;
                    }
                }
                if (allMatch) {
                    return i;
                }
            }
        }
        return -1;
    }
}

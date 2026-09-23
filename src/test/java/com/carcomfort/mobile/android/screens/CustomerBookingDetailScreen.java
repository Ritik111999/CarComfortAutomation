package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * AND-CUST-SERVICEDETAIL-001 — Customer-side Service Details screen.
 * Discovered from real Car Wash booking lifecycle (#CC-CW-...).
 * Displays booking metadata, status, provider assignment, route plan,
 * vehicle notes, and price summary.
 *
 * <p>Cancellation button is GATED and never tapped in happy-path tests.
 */
public final class CustomerBookingDetailScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Service Details");
    private static final By BOOKING_ID_LABEL = LocatorFactory.accessibilityId("BOOKING ID");
    private static final By BOOKING_STATUS_LABEL = LocatorFactory.accessibilityId("BOOKING STATUS");
    private static final By TOTAL_PRICE_LABEL = LocatorFactory.accessibilityId("TOTAL PRICE");
    private static final By SERVICE_TYPE_LABEL = LocatorFactory.accessibilityId("SERVICE TYPE");
    private static final By ROUTE_PLAN_LABEL = LocatorFactory.accessibilityId("Route Plan");

    public CustomerBookingDetailScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerBookingDetail";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isDetailShown() {
        return !driverManager.getDriver().findElements(TITLE).isEmpty();
    }

    /** Extracts the Booking ID (e.g. #CC-CW-20260923-000006). */
    public String readBookingId() {
        List<String> descs = allDescs();
        for (String d : descs) {
            if (d.startsWith("#CC-")) {
                return d.trim();
            }
        }
        for (int i = 0; i + 1 < descs.size(); i++) {
            if ("BOOKING ID".equalsIgnoreCase(descs.get(i))) {
                return descs.get(i + 1).trim();
            }
        }
        return "";
    }

    /** Reads booking status (e.g. Confirmed, Completed). */
    public String readBookingStatus() {
        List<String> descs = allDescs();
        for (int i = 0; i + 1 < descs.size(); i++) {
            if ("BOOKING STATUS".equalsIgnoreCase(descs.get(i))) {
                return descs.get(i + 1).trim();
            }
        }
        return "";
    }

    /** Reads total price (e.g. $35.35). */
    public String readTotalPrice() {
        List<String> descs = allDescs();
        for (int i = 0; i + 1 < descs.size(); i++) {
            if ("TOTAL PRICE".equalsIgnoreCase(descs.get(i))) {
                return descs.get(i + 1).trim();
            }
        }
        return "";
    }

    /** Reads service type (e.g. Car Wash). */
    public String readServiceType() {
        List<String> descs = allDescs();
        for (int i = 0; i + 1 < descs.size(); i++) {
            if ("SERVICE TYPE".equalsIgnoreCase(descs.get(i))) {
                return descs.get(i + 1).trim();
            }
        }
        return "";
    }

    /** Checks if a provider assignment label is visible. */
    public String readProviderAssignment() {
        List<String> descs = allDescs();
        for (String d : descs) {
            if (d.contains("Service Provider Not Assigned") || d.contains("Assigned") || d.contains("Driver")) {
                return d.trim();
            }
        }
        return "";
    }

    /** Verifies route plan elements are present. */
    public boolean hasRoutePlan() {
        return !driverManager.getDriver().findElements(ROUTE_PLAN_LABEL).isEmpty();
    }

    public List<String> allDescs() {
        List<String> out = new ArrayList<>();
        try {
            for (WebElement el : driverManager.getDriver().findElements(
                    By.xpath("//*[@content-desc]"))) {
                try {
                    String d = el.getAttribute("content-desc");
                    if (d != null && !d.isBlank()) {
                        out.add(d.replaceAll("\\s+", " ").trim());
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public void scrollDown() {
        try {
            var size = driverManager.getDriver().manage().window().getSize();
            int startX = size.width / 2;
            int startY = (int) (size.height * 0.75);
            int endY = (int) (size.height * 0.25);
            driverManager.getDriver().executeScript("mobile: swipeGesture", java.util.Map.of(
                    "left", 100, "top", 500, "width", size.width - 200, "height", size.height - 1000,
                    "direction", "up",
                    "percent", 0.75
            ));
        } catch (Exception e) {
            log.debug("Swipe gesture fallback", e);
        }
    }

    public void backToBookings() {
        try {
            driverManager.getDriver().navigate().back();
        } catch (Exception e) {
            log.warn("Error navigating back from Service Details", e);
        }
    }
}

package com.carcomfort.mobile.android.components;

import com.carcomfort.core.driver.AndroidDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bottom navigation bar (5 tabs) shared by the Customer and Provider apps.
 *
 * <p>Tab icons expose no content-desc/resource-id (weak Flutter semantics,
 * B-003), and UiAutomator {@code instance(N)} numbering shifts per screen —
 * so tabs are resolved fresh on every call as the clickable nodes in the
 * bottom strip (y≈2241–2362), sorted left→right. Verified across 10+ tab
 * switches on 2026-09-22 (Xiaomi 22021211RI, Android 14, app v1.1.1).
 *
 * <p>Rule: tab switches never leave the app. BACK from a tab root exits to
 * the launcher — always return via {@link #tapHomeTab()}, never BACK.
 */
public final class BottomNavComponent extends BaseAndroidComponent {

    public BottomNavComponent(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getComponentName() {
        return "BottomNav";
    }

    /** All bottom tabs left→right, re-queried (never cached across screens). */
    public List<WebElement> tabs() {
        List<WebElement> out = new ArrayList<>();
        for (WebElement el : driverManager.getDriver().findElements(By.xpath("//*[@clickable='true']"))) {
            try {
                String bounds = el.getAttribute("bounds");
                if (bounds != null && (bounds.contains("2241") || bounds.contains("2362"))) {
                    out.add(el);
                }
            } catch (Exception ignored) {
            }
        }
        out.sort(Comparator.comparingInt(el -> {
            try {
                String bounds = el.getAttribute("bounds");
                return Integer.parseInt(bounds.substring(1, bounds.indexOf(',')));
            } catch (Exception e) {
                return 9999;
            }
        }));
        return out;
    }

    public int tabCount() {
        return tabs().size();
    }

    public void tapTab(int index) {
        List<WebElement> current = tabs();
        if (index < 0 || index >= current.size()) {
            throw new IllegalStateException(
                    "Bottom tab " + index + " unavailable (found " + current.size() + " tabs)");
        }
        current.get(index).click();
        log.debug("Tapped bottom tab {}", index);
    }

    public void tapHomeTab() {
        tapTab(0);
    }
}

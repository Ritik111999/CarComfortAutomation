package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-ROLE-001 / AND-PROV-ROLE-001 — Role selection ("Select your role").
 * Discovered 2026-09-22 on Xiaomi 22021211RI, Android 14, app v1.1.1.
 */
public final class RoleSelectionScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Select your role");
    private static final By CUSTOMER_BUTTON = LocatorFactory.accessibilityId("Customer");
    private static final By PROVIDER_BUTTON = LocatorFactory.accessibilityId("Service Provider");

    public RoleSelectionScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "RoleSelection";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public void selectCustomer() {
        click(CUSTOMER_BUTTON, "Customer role button");
        // Prove the transition left this screen; cold-start Flutter navigation can lag.
        waits.waitLong(d -> org.openqa.selenium.support.ui.ExpectedConditions
                .invisibilityOfElementLocated(TITLE).apply(driverManager.getDriver()));
        log.debug("Selected Customer role");
    }

    public void selectProvider() {
        click(PROVIDER_BUTTON, "Service Provider role button");
        // Prove the transition left this screen; cold-start Flutter navigation can lag.
        waits.waitLong(d -> org.openqa.selenium.support.ui.ExpectedConditions
                .invisibilityOfElementLocated(TITLE).apply(driverManager.getDriver()));
        log.debug("Selected Service Provider role");
    }
}

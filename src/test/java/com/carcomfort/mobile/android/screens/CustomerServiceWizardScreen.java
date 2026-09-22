package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-CUST-SVC-{CARWASH,EVCHARGING,COMBO}-001 — Customer booking wizard,
 * step 1 Location (safe inspection boundary).
 * Mapped 2026-09-22. Four-step wizard (Location/Service/Vehicle/Review);
 * automation stops before "Next" advances toward the GATED submission
 * boundary (Confirm/Book/Pay — never tapped).
 */
public final class CustomerServiceWizardScreen extends BaseAndroidScreen {

    private static final By STEP_LOCATION = LocatorFactory.accessibilityId("Location");
    private static final By STEP_SERVICE = LocatorFactory.accessibilityId("Service");
    private static final By STEP_VEHICLE = LocatorFactory.accessibilityId("Vehicle");
    private static final By STEP_REVIEW = LocatorFactory.accessibilityId("Review");

    public CustomerServiceWizardScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerServiceWizard";
    }

    @Override
    public By getUniqueLocator() {
        return STEP_LOCATION;
    }

    public boolean isFourStepWizardShown() {
        var d = driverManager.getDriver();
        return !d.findElements(STEP_LOCATION).isEmpty()
                && !d.findElements(STEP_SERVICE).isEmpty()
                && !d.findElements(STEP_VEHICLE).isEmpty()
                && !d.findElements(STEP_REVIEW).isEmpty();
    }
}

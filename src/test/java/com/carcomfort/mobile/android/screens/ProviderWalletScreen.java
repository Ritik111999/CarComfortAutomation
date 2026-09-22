package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;

/**
 * AND-PROV-WALLET-001 — Provider wallet (Stripe-gated, presence-only).
 * Mapped 2026-09-22. All CTAs lead to Stripe onboarding/verification (GATED);
 * automation asserts presence only and never taps them.
 */
public final class ProviderWalletScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("My Wallet");
    private static final By STRIPE_GATE = LocatorFactory.accessibilityId("Complete Stripe verification");

    public ProviderWalletScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "ProviderWallet";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public boolean isStripeGateShown() {
        return !driverManager.getDriver().findElements(STRIPE_GATE).isEmpty();
    }
}

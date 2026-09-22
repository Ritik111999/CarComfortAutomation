package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * AND-CUST-AUTH-001 — Customer/Provider login ("Welcome to Car Comfort").
 * Discovered 2026-09-22, app v1.1.1.
 *
 * <p>Weak Flutter semantics note: the Email/Password EditTexts expose no hint text,
 * content-desc, or resource-id — only class + order. Located by UiAutomator class
 * instance (email=0, password=1); password field confirmed by the trailing eye icon.
 * Recommended app improvement: add content-desc/hint ("Email Address", "Password").
 * "Sign up now" is GATED (account creation) — never tapped by automation.
 */
public final class LoginScreen extends BaseAndroidScreen {

    private static final By TITLE = LocatorFactory.accessibilityId("Welcome to Car Comfort");
    private static final By EMAIL_FIELD =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)");
    private static final By PASSWORD_FIELD =
            LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\").instance(1)");
    private static final By LOGIN_BUTTON = LocatorFactory.accessibilityId("Login");
    private static final By SIGN_UP = LocatorFactory.accessibilityId("Sign up now");

    public LoginScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "Login";
    }

    @Override
    public By getUniqueLocator() {
        return TITLE;
    }

    public void enterEmail(String email) {
        hideKeyboard();
        // Same IME-commit rule as password: unfocused setText updates semantics
        // without committing to the Flutter controller (verified: hint still shown
        // at submit despite matching getText). Focus-tap, dismiss any autofill
        // overlay, type on the same reference.
        WebElement el = findClickable(EMAIL_FIELD);
        el.click();
        dismissAutofillPopup();
        el.sendKeys(email);
        if (!email.equals(textOf(EMAIL_FIELD))) {
            throw new IllegalStateException("Failed to enter email into login field");
        }
        log.debug("email entered and verified (focus-tap path)");
    }

    public void enterPassword(String password) {
        hideKeyboard();
        // Obscured Flutter fields only commit with a real IME connection: focus-tap,
        // then type on the SAME reference (post-tap re-resolution goes blind while
        // semantics rebuild — verified via server 404s). A BACK press first dismisses
        // any password-manager autofill overlay (personal data — never tapped).
        WebElement el = findClickable(PASSWORD_FIELD);
        el.click();
        dismissAutofillPopup();
        el.sendKeys(password);
        hideKeyboard();
        log.debug("password entry attempted (focus-tap path)");
    }

    /**
     * Dismisses the OS password-manager autofill overlay with BACK (safe, reversible).
     * The overlay shows device-owner credentials — automation must never tap it.
     */
    private void dismissAutofillPopup() {
        // Autofill text may live in @text or content-desc depending on renderer — check both.
        By[] variants = {
            LocatorFactory.stableText("Manage passwords"),
            LocatorFactory.accessibilityId("Manage passwords")
        };
        for (int i = 0; i < 2; i++) {
            if (!isPopupPresent(variants)) {
                return;
            }
            log.debug("Autofill overlay present; dismissing with BACK (attempt {})", i + 1);
            driverManager.getDriver().navigate().back();
        }
        if (isPopupPresent(variants)) {
            throw new IllegalStateException("Autofill overlay would obscure Login; aborting tap");
        }
    }

    private boolean isPopupPresent(By[] variants) {
        for (By locator : variants) {
            if (!driverManager.getDriver().findElements(locator).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String textOf(By field) {
        try {
            String text = findVisible(field).getText();
            return text != null ? text : "";
        } catch (Exception e) {
            return "";
        }
    }

    public void tapLogin() {
        hideKeyboard();
        dismissAutofillPopup();
        click(LOGIN_BUTTON, "Login button");
    }

    public void loginAs(String email, String password) {
        waitForScreenLoadedLong();
        enterEmail(email);
        enterPassword(password);
        tapLogin();
    }

    public void waitForLoginGone() {
        waitForDisappeared(LOGIN_BUTTON);
    }

    /**
     * Stale-tolerant presence check for post-transition verification (Flutter
     * rebuilds can throw StaleElement mid-poll — retried, never weakened).
     */
    public boolean confirmPresent() {
        io.appium.java_client.android.AndroidDriver driver = driverManager.getDriver();
        final boolean[] seen = {false};
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> {
                        seen[0] = !driver.findElements(TITLE).isEmpty();
                        return seen[0];
                    });
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return seen[0];
        }
        return seen[0];
    }

    public boolean isSignUpPresent() {
        List<WebElement> found = driverManager.getDriver().findElements(SIGN_UP);
        return !found.isEmpty();
    }
}

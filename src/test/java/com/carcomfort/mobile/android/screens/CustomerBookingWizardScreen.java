package com.carcomfort.mobile.android.screens;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.mobile.android.LocatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Customer booking wizard, all 4 steps (Car Wash path; EV/Combo reuse the
 * same step pattern with service-specific titles).
 *
 * <p>Discovered 2026-09-22, app v1.1.1 (one-time traversal, submit never
 * pressed): 1 Location (manual address + suggestion pick; Next blocked until
 * set) -> 2 Service (membership radio, custom Package Name (*) / Price (*) or
 * CarComfort dropdown, ASAP default, Schedule->time picker; date control TBD)
 * -> 3 Vehicle (default saved vehicle; 4 required parking/key dropdowns) ->
 * 4 Review & Confirm (sections + Total + Confirm Booking; NO payment UI).
 *
 * <p>{@link #submitBooking()} taps Confirm Booking — the GATED business
 * boundary. Callers must hold BUSINESS_CASE authorization, ledger state,
 * and the mutation lock first. Never called from smoke.
 */
public final class CustomerBookingWizardScreen extends BaseAndroidScreen {

    // Step 1 — Location
    private static final By MANUAL_ENTRY = LocatorFactory.accessibilityId("Or Enter Address Manually");
    private static final By CANT_FIND = LocatorFactory.accessibilityId("Can't Find Service Location? Enter It Here");
    private static final By NEXT_LOCATION = LocatorFactory.accessibilityId("Next: Car Wash Details");
    // Step 2 — Service
    private static final By NO_MEMBERSHIP = LocatorFactory.accessibilityId("I don't have a car wash membership");
    private static final By HAVE_MEMBERSHIP = LocatorFactory.accessibilityId("I have a car wash membership at my local car wash");
    private static final By ASAP_RADIO = LocatorFactory.accessibilityId("As Soon As Possible");
    private static final By SCHEDULE_RADIO = LocatorFactory.accessibilityId("Schedule");
    private static final By NEXT_SERVICE = LocatorFactory.accessibilityId("Next: Vehicle Details");
    // Step 3 — Vehicle
    private static final By UNANSWERED_DROPDOWN = LocatorFactory.accessibilityId("Select an option");
    private static final By NEXT_VEHICLE = LocatorFactory.accessibilityId("Next: Review & Payment");
    // Step 4 — Review
    private static final By REVIEW_TITLE = LocatorFactory.accessibilityId("Review & Confirm");
    private static final By TOTAL_LABEL = LocatorFactory.accessibilityId("Total:");
    private static final By CONFIRM_BOOKING = LocatorFactory.accessibilityId("Confirm Booking");

    public CustomerBookingWizardScreen(AndroidDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public String getScreenName() {
        return "CustomerBookingWizard";
    }

    @Override
    public By getUniqueLocator() {
        return LocatorFactory.accessibilityId("Enter Car Wash Service Details");
    }

    // ---- Step 1: Location ----

    public void enterManualAddress(String address) {
        click(MANUAL_ENTRY, "Or Enter Address Manually");
        if (!awaitEditTexts(1)) {
            throw new IllegalStateException("Address input field not rendered after manual entry tap");
        }
        typeIntoEditText(0, address);
        log.debug("Manual address entered (suggestion pick follows)");
    }

    public void pickFirstAddressSuggestion() {
        // Suggestions render async after typing (network lookup) — await them.
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> driverManager.getDriver().findElements(
                            LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"Nagpur\")")).size() > 0);
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            // fall through to the loud failure below
        }
        List<WebElement> suggestions = driverManager.getDriver().findElements(
                LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"Nagpur\")"));
        if (suggestions.isEmpty()) {
            throw new IllegalStateException("No address suggestions rendered for the test address");
        }
        String picked = suggestions.get(0).getAttribute("content-desc");
        suggestions.get(0).click();
        log.debug("Address suggestion picked: {}", picked);
    }

    public boolean isLocationSet() {
        var driver = driverManager.getDriver();
        return !driver.findElements(LocatorFactory.accessibilityId("Address selected")).isEmpty()
                || !driver.findElements(LocatorFactory.accessibilityId("Car wash location selected")).isEmpty();
    }

    public void nextFromLocation() {
        click(NEXT_LOCATION, "Next: Car Wash Details");
        awaitMarker("I don't have a car wash membership", "service step");
    }

    // ---- Step 2: Service ----

    public void selectNoMembership() {
        if (!driverManager.getDriver().findElements(NO_MEMBERSHIP).isEmpty()) {
            click(NO_MEMBERSHIP, "No membership option");
        }
    }

    /** Custom package entry (free name/price). Minimal footprint values per lifecycle plan. */
    public void enterCustomPackage(String name, String price) {
        if (!awaitEditTexts(2)) {
            throw new IllegalStateException("Package name/price fields not rendered");
        }
        typeIntoEditText(0, name);
        typeIntoEditText(1, price);
        hideKeyboard();
        log.debug("Custom package entered (draft)");
    }

    /** ASAP is the default; tapping it explicitly keeps the schedule section untouched (no date control needed). */
    public void chooseAsSoonAsPossible() {
        if (!driverManager.getDriver().findElements(ASAP_RADIO).isEmpty()) {
            click(ASAP_RADIO, "As Soon As Possible");
        }
    }

    public void nextFromService() {
        click(NEXT_SERVICE, "Next: Vehicle Details");
        awaitMarker("Where is the vehicle parked?", "vehicle step");
    }

    // ---- Step 3: Vehicle ----

    /** Answers every parking/key dropdown with the given option labels (draft only). */
    public void answerVehicleDropdowns(List<String> optionsInOrder) {
        for (String option : optionsInOrder) {
            answerOneDropdown(option);
        }
        // Quiescence: a single empty observation can be a transition blink or a
        // sheet-covered form (both falsely read as "all done"). Require a run
        // of consecutive settled observations instead.
        requireSettledForm();
    }

    /**
     * Waits for a run of consecutive settled observations (non-empty tree, no
     * open sheet, zero unanswered dropdowns). A single observation is not
     * trusted: transition blinks and sheet-covered forms both read as
     * "all done" exactly once.
     */
    private void requireSettledForm() {
        java.util.concurrent.atomic.AtomicInteger calm = new java.util.concurrent.atomic.AtomicInteger(0);
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(15))
                    .pollInterval(java.time.Duration.ofMillis(400))
                    .ignoreExceptions()
                    .until(() -> {
                        boolean settled;
                        try {
                            settled = !driverManager.getDriver()
                                    .findElements(org.openqa.selenium.By.xpath("//*[@content-desc]")).isEmpty()
                                    && !isSheetOpen() && !hasUnansweredDropdowns();
                        } catch (Exception e) {
                            settled = false;
                        }
                        if (settled) {
                            return calm.incrementAndGet() >= 4;
                        }
                        calm.set(0);
                        return false;
                    });
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            // fall through — the caller assert reports the residual state
        }
    }

    /**
     * One dropdown Q&amp;A with sheet-state tracking. The option sheet carries
     * a "Dismiss" node the form never has; an option tap that lands during the
     * sheet animation misses, leaves the sheet open, and every later check
     * then vacuously passes on the sheet-covered form — so each step is
     * verified (sheet opened → sheet closed → answer visible) with one retry.
     */
    private void answerOneDropdown(String option) {
        for (int attempt = 0; attempt < 2; attempt++) {
            if (isSheetOpen()) {
                // Stale open sheet from a missed tap — dismiss and re-query.
                driverManager.getDriver().navigate().back();
                awaitSheet(false);
            }
            List<WebElement> drops = driverManager.getDriver().findElements(UNANSWERED_DROPDOWN);
            if (drops.isEmpty()) {
                log.debug("No unanswered dropdowns left");
                return;
            }
            drops.get(0).click();
            if (!awaitSheet(true)) {
                continue; // tap missed while opening; retry
            }
            click(LocatorFactory.accessibilityId(option), "Dropdown option: " + option);
            if (!awaitSheet(false)) {
                continue; // tap missed into the backdrop; sheet still open — retry
            }
            if (awaitAnswerVisible(option)) {
                return;
            }
        }
        log.debug("Dropdown answer unverified for option: {}", option);
    }

    private boolean isSheetOpen() {
        return !driverManager.getDriver()
                .findElements(LocatorFactory.accessibilityId("Dismiss")).isEmpty();
    }

    private boolean awaitSheet(boolean open) {
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> isSheetOpen() == open);
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return isSheetOpen() == open;
        }
    }

    private boolean awaitAnswerVisible(String option) {
        // Post-pick the whole semantics tree can blink EMPTY mid-rebuild:
        // wait for repopulation first, then for this answer.
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driverManager.getDriver()
                            .findElements(org.openqa.selenium.By.xpath("//*[@content-desc]")).isEmpty());
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            log.debug("Hierarchy did not repopulate after pick");
        }
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driverManager.getDriver()
                            .findElements(LocatorFactory.accessibilityId(option)).isEmpty());
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            log.debug("Answer not yet visible for option: {}", option);
            return !driverManager.getDriver()
                    .findElements(LocatorFactory.accessibilityId(option)).isEmpty();
        }
    }

    public boolean hasUnansweredDropdowns() {
        return !driverManager.getDriver().findElements(UNANSWERED_DROPDOWN).isEmpty();
    }

    public boolean isAnswerVisible(String option) {
        return !driverManager.getDriver().findElements(LocatorFactory.accessibilityId(option)).isEmpty();
    }

    public void nextFromVehicle() {
        scrollIntoView("Next: Review & Payment");
        click(NEXT_VEHICLE, "Next: Review & Payment");
        awaitMarker("Review & Confirm", "review step");
    }

    /** Step transitions render async — never act on the target step before its marker lands. */
    private void awaitMarker(String contentDesc, String stepName) {
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(20))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driverManager.getDriver()
                            .findElements(LocatorFactory.accessibilityId(contentDesc)).isEmpty());
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            throw new IllegalStateException("Wizard did not reach " + stepName + " after Next");
        }
    }

    // ---- Step 4: Review ----

    public boolean isReviewShown() {
        return !driverManager.getDriver().findElements(REVIEW_TITLE).isEmpty();
    }

    /** Review total line (e.g. "$36.07") — caller parses/records, never hardcodes. */
    public String readReviewTotal() {
        var driver = driverManager.getDriver();
        if (driver.findElements(TOTAL_LABEL).isEmpty()) {
            return "";
        }
        List<WebElement> money = driver.findElements(
                LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"$\")"));
        for (WebElement el : money) {
            try {
                String text = el.getAttribute("content-desc");
                if (text != null && text.trim().startsWith("$")) {
                    return text.trim();
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    /**
     * GATED BUSINESS BOUNDARY — taps Confirm Booking exactly once. Caller must
     * hold BUSINESS_CASE authorization + ledger idempotency + mutation lock.
     */
    public void submitBooking() {
        click(CONFIRM_BOOKING, "Confirm Booking (GATED business submit)");
    }

    public boolean isConfirmVisible() {
        return !driverManager.getDriver().findElements(CONFIRM_BOOKING).isEmpty();
    }

    // ---- helpers ----

    private void scrollIntoView(String contentDesc) {
        String literal = "\"" + contentDesc.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        try {
            driverManager.getDriver().findElement(new io.appium.java_client.AppiumBy.ByAndroidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                            + "new UiSelector().description(" + literal + "))"));
        } catch (Exception e) {
            log.debug("scrollIntoView skipped (already visible or unscrolled): {}", contentDesc);
        }
    }

    private By editTexts() {
        return LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")");
    }

    private boolean awaitEditTexts(int minimum) {
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> driverManager.getDriver().findElements(editTexts()).size() >= minimum);
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return !driverManager.getDriver().findElements(editTexts()).isEmpty()
                    && driverManager.getDriver().findElements(editTexts()).size() >= minimum;
        }
    }

    /**
     * Focus-tap typing tolerant to Flutter semantics rebuilds: the hierarchy
     * rebuilds on focus/keyboard changes, so references stale and the field
     * list can blink empty mid-rebuild (same class of issue as the login
     * fields). Each attempt re-waits for the fields; BACK is only sent when
     * the keyboard is actually shown (a stray BACK would pop the wizard).
     * Locator-level retries only — never a business-action retry.
     */
    private void typeIntoEditText(int index, String text) {
        Exception lastFailure = new IllegalStateException("no attempts ran");
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                if (!awaitEditTexts(index + 1)) {
                    throw new IllegalStateException("Field " + index + " not rendered");
                }
                WebElement field = driverManager.getDriver().findElements(editTexts()).get(index);
                field.click();
                try {
                    if (driverManager.getDriver().isKeyboardShown()) {
                        dismissKeyboardSafely();
                    }
                } catch (Exception ignored) {
                }
                field.sendKeys(text);
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException | IndexOutOfBoundsException e) {
                lastFailure = e;
                log.debug("EditText unstable on focus (attempt {}), re-waiting", attempt + 1);
            }
        }
        throw new IllegalStateException("Failed to type into field " + index, lastFailure);
    }

    /** BACK dismisses autofill/keyboard overlays; never taps them (privacy rule). */
    private void dismissKeyboardSafely() {
        try {
            driverManager.getDriver().navigate().back();
        } catch (Exception ignored) {
        }
    }
}

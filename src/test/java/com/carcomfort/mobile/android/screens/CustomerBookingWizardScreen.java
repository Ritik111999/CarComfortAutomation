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
 * 4 Review & Confirm (sections + Total + Confirm Booking; masked payment method).
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
    private static final By CHOOSE_PACKAGE_BTN = LocatorFactory.accessibilityId("Choose a package");
    // Step 3 — Vehicle
    private static final By UNANSWERED_DROPDOWN = LocatorFactory.accessibilityId("Select an option");
    private static final By NEXT_VEHICLE = LocatorFactory.accessibilityId("Next: Review & Payment");
    // Step 4 — Review
    private static final By REVIEW_TITLE = LocatorFactory.accessibilityId("Review & Confirm");
    private static final By TOTAL_LABEL = LocatorFactory.accessibilityId("Total:");
    private static final By CONFIRM_BOOKING = LocatorFactory.accessibilityId("Confirm Booking");
    // Step 5 — Confirmation
    private static final By CONFIRMATION_TITLE = LocatorFactory.accessibilityId("Booking Confirmed!");
    private static final By HOME_BTN = LocatorFactory.accessibilityId("Home");

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
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> driver.findElements(
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

    public void selectCarWashFacility(String facilityName) {
        var driver = driverManager.getDriver();
        By facilityLocator = LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"" + facilityName + "\")");
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver.findElements(facilityLocator).isEmpty());
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            scrollIntoView(facilityName);
        }
        List<WebElement> facilities = driver.findElements(facilityLocator);
        if (!facilities.isEmpty()) {
            facilities.get(0).click();
            log.debug("Selected car wash facility: {}", facilityName);
        } else {
            log.debug("Facility {} not found in list or already selected", facilityName);
        }
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

    public void selectCarComfortPackage(String packageName) {
        var driver = driverManager.getDriver();
        if (!driver.findElements(CHOOSE_PACKAGE_BTN).isEmpty()) {
            click(CHOOSE_PACKAGE_BTN, "Choose a package dropdown");
            awaitSheet(true);
            By pkg = LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"" + packageName + "\")");
            var found = driver.findElements(pkg);
            if (!found.isEmpty()) {
                found.get(0).click();
                awaitSheet(false);
                log.debug("CarComfort package selected: {}", packageName);
                return;
            }
        }
        log.debug("CarComfort dropdown package selection bypassed or already selected");
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
        requireSettledForm();
    }

    public void enterAdditionalNotes(String notes) {
        if (notes == null || notes.isBlank()) return;
        var driver = driverManager.getDriver();
        var editTexts = driver.findElements(editTexts());
        if (!editTexts.isEmpty()) {
            WebElement noteField = editTexts.get(editTexts.size() - 1);
            noteField.click();
            try {
                if (driver.isKeyboardShown()) {
                    dismissKeyboardSafely();
                }
            } catch (Exception ignored) {
            }
            noteField.sendKeys(notes);
            dismissKeyboardSafely();
            log.debug("Entered additional service notes: {}", notes);
        }
    }

    private void requireSettledForm() {
        java.util.concurrent.atomic.AtomicInteger calm = new java.util.concurrent.atomic.AtomicInteger(0);
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(15))
                    .pollInterval(java.time.Duration.ofMillis(400))
                    .ignoreExceptions()
                    .until(() -> {
                        boolean settled;
                        try {
                            settled = !driver
                                    .findElements(org.openqa.selenium.By.xpath("//*[@content-desc]")).isEmpty()
                                    && driver.findElements(LocatorFactory.accessibilityId("Dismiss")).isEmpty()
                                    && driver.findElements(UNANSWERED_DROPDOWN).isEmpty();
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
            // fall through
        }
    }

    private void answerOneDropdown(String option) {
        for (int attempt = 0; attempt < 2; attempt++) {
            if (isSheetOpen()) {
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
                continue;
            }
            click(LocatorFactory.accessibilityId(option), "Dropdown option: " + option);
            if (!awaitSheet(false)) {
                continue;
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
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver
                            .findElements(LocatorFactory.accessibilityId("Dismiss")).isEmpty() == open);
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return !driverManager.getDriver()
                    .findElements(LocatorFactory.accessibilityId("Dismiss")).isEmpty() == open;
        }
    }

    private boolean awaitAnswerVisible(String option) {
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver
                            .findElements(org.openqa.selenium.By.xpath("//*[@content-desc]")).isEmpty());
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            log.debug("Hierarchy did not repopulate after pick");
        }
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver
                            .findElements(LocatorFactory.accessibilityId(option)).isEmpty());
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
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

    private void awaitMarker(String contentDesc, String stepName) {
        var driver = driverManager.getDriver();
        long started = System.currentTimeMillis();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(30))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver
                            .findElements(LocatorFactory.accessibilityId(contentDesc)).isEmpty());
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            throw new IllegalStateException("Wizard did not reach " + stepName + " after Next");
        }
        long elapsed = (System.currentTimeMillis() - started) / 1000;
        if (elapsed > 15) {
            log.warn("Slow wizard transition to {} ({}s) — backend latency signal", stepName, elapsed);
        }
    }

    // ---- Step 4: Review ----

    public boolean isReviewShown() {
        return !driverManager.getDriver().findElements(REVIEW_TITLE).isEmpty();
    }

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

    public boolean verifyMaskedPaymentMethod() {
        var driver = driverManager.getDriver();
        return !driver.findElements(LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(\"VISA\")")).isEmpty()
                || !driver.findElements(LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(\"••••\")")).isEmpty()
                || !driver.findElements(LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(\"4242\")")).isEmpty()
                || !driver.findElements(LocatorFactory.accessibilityId("Payment Method")).isEmpty();
    }

    public void submitBooking() {
        click(CONFIRM_BOOKING, "Confirm Booking (GATED business submit)");
    }

    public boolean isConfirmVisible() {
        return !driverManager.getDriver().findElements(CONFIRM_BOOKING).isEmpty();
    }

    // ---- Step 5: Confirmation ----

    public boolean awaitBookingConfirmed() {
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(30))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> !driver.findElements(CONFIRMATION_TITLE).isEmpty()
                            || !driver.findElements(LocatorFactory.uiAutomator(
                            "new UiSelector().descriptionContains(\"Booking Confirmed\")")).isEmpty());
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return false;
        }
    }

    public String extractBookingIdFromConfirmation() {
        var driver = driverManager.getDriver();
        List<WebElement> descs = driver.findElements(
                LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"#CC-\")"));
        for (WebElement el : descs) {
            String desc = el.getAttribute("content-desc");
            if (desc != null && desc.contains("#CC-")) {
                int start = desc.indexOf("#CC-");
                return desc.substring(start).replaceAll("[^A-Za-z0-9\\-#]", " ").trim().split("\\s+")[0];
            }
        }
        return "";
    }

    public String extractTotalFromConfirmation() {
        var driver = driverManager.getDriver();
        List<WebElement> descs = driver.findElements(
                LocatorFactory.uiAutomator("new UiSelector().descriptionContains(\"$\")"));
        for (WebElement el : descs) {
            String desc = el.getAttribute("content-desc");
            if (desc != null && desc.contains("$")) {
                int start = desc.indexOf("$");
                return desc.substring(start).trim().split("\\s+")[0];
            }
        }
        return "";
    }

    public void tapHomeFromConfirmation() {
        var driver = driverManager.getDriver();
        var home = driver.findElements(HOME_BTN);
        if (!home.isEmpty()) {
            home.get(0).click();
            log.debug("Tapped Home from confirmation screen");
        } else {
            driver.navigate().back();
        }
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
        var driver = driverManager.getDriver();
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                    .pollInterval(java.time.Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> driver.findElements(editTexts()).size() >= minimum);
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return !driverManager.getDriver().findElements(editTexts()).isEmpty()
                    && driverManager.getDriver().findElements(editTexts()).size() >= minimum;
        }
    }

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

    private void dismissKeyboardSafely() {
        try {
            driverManager.getDriver().navigate().back();
        } catch (Exception ignored) {
        }
    }
}

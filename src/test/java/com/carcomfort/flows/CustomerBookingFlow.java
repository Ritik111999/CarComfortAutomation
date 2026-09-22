package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.CustomerBookingWizardScreen;
import com.carcomfort.mobile.android.screens.CustomerBookingsScreen;
import com.carcomfort.mobile.android.screens.CustomerHomeScreen;

import java.util.List;

/**
 * Customer booking business flow (L3): wizard fill → review → gated submit →
 * bookings-list verification. Multi-screen business process — screen state
 * lives in {@link CustomerBookingWizardScreen}; tests own lifecycle
 * orchestration (guards, ledger, mutation lock).
 */
public final class CustomerBookingFlow extends BaseBusinessFlow {

    private final CustomerHomeScreen home;
    private final CustomerBookingWizardScreen wizard;
    private final CustomerBookingsScreen bookings;
    private final BottomNavComponent bottomNav;

    public CustomerBookingFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.home = new CustomerHomeScreen(driverManager);
        this.wizard = new CustomerBookingWizardScreen(driverManager);
        this.bookings = new CustomerBookingsScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    /** Step 1 Location: manual address + suggestion pick, verified set. */
    public void fillLocation(String address) {
        home.tapCarWash();
        wizard.enterManualAddress(address);
        wizard.pickFirstAddressSuggestion();
        captureCheckpointEvidence("lifecycle_location");
        assertBusinessRule(wizard.isLocationSet(), "Location set (address + facility selected)");
        logBusinessCheckpoint("WIZ_LOCATION", "Wizard location step complete");
        finalizeAssertions();
    }

    /** Step 2 Service: no-membership + custom package + ASAP. */
    public void fillService(String packageName, String packagePrice) {
        wizard.nextFromLocation();
        wizard.selectNoMembership();
        wizard.enterCustomPackage(packageName, packagePrice);
        wizard.chooseAsSoonAsPossible();
        captureCheckpointEvidence("lifecycle_service");
        logBusinessCheckpoint("WIZ_SERVICE", "Wizard service step complete (package " + packageName + ")");
        finalizeAssertions();
    }

    /** Step 3 Vehicle: default saved vehicle + parking/key answers + notes. */
    public void fillVehicle(List<String> dropdownOptions) {
        wizard.nextFromService();
        wizard.answerVehicleDropdowns(dropdownOptions);
        captureCheckpointEvidence("lifecycle_vehicle");
        assertBusinessRule(!wizard.hasUnansweredDropdowns(), "All vehicle dropdowns answered");
        for (String option : dropdownOptions) {
            assertBusinessRule(wizard.isAnswerVisible(option),
                    "Vehicle answer visible: " + option);
        }
        wizard.nextFromVehicle();
        logBusinessCheckpoint("WIZ_VEHICLE", "Wizard vehicle step complete");
        finalizeAssertions();
    }

    /** Step 4 Review: verifies summary structure + records total (no hardcode). */
    public String verifyReview() {
        assertBusinessRule(wizard.isReviewShown(), "Review & Confirm screen shown");
        String total = wizard.readReviewTotal();
        captureCheckpointEvidence("lifecycle_review");
        assertBusinessRule(!total.isEmpty(), "Review total rendered (actual: " + total + ")");
        logBusinessCheckpoint("WIZ_REVIEW", "Review verified, total " + total + " (no payment UI present)");
        finalizeAssertions();
        return total;
    }

    /**
     * GATED submit — caller holds BUSINESS_CASE + ledger + mutation lock.
     * Exactly one tap; on timeout the caller inspects state (never blind-retry).
     */
    public void submitBooking() {
        wizard.submitBooking();
        logBusinessCheckpoint("BOOKING_SUBMIT_TAP", "Confirm Booking tapped once");
    }

    /** Finds our booking card by package-name marker (never list position). */
    public boolean isBookingCardPresent(String packageMarker) {
        bottomNav.tapTab(1);
        bookings.waitForScreenLoadedLong();
        var driver = driverManager.getDriver();
        return !driver.findElements(com.carcomfort.mobile.android.LocatorFactory.uiAutomator(
                "new UiSelector().descriptionContains(\"" + packageMarker + "\")")).isEmpty();
    }

    public int bookingCardCount() {
        return bookings.viewDetailsCount();
    }
}

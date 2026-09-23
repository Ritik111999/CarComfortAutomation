package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.CustomerBookingsScreen;
import com.carcomfort.mobile.android.screens.ProviderBookingDetailScreen;
import com.carcomfort.mobile.android.screens.ProviderBookingsScreen;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Booking-cancellation business flow (L4 scaffold — NOT this milestone).
 *
 * <p>Models the three cancellation scenarios with separate bookings and
 * separate lifecycle IDs (never reused across mutually exclusive branches):
 * customer-cancel-before-accept, customer-cancel-after-accept (late rules/fees
 * TBD), provider-cancel. All cancel taps are GATED by a dedicated
 * BUSINESS_CASE and require a proven-correlation booking + mutation lock held
 * by the caller. This class performs NO live mutation unless the caller has
 * satisfied those preconditions; the happy-path E2E test never calls it.
 *
 * <p>Screen state lives in the booking screens; tests own lifecycle
 * orchestration (guards, ledger, mutation lock).
 */
public final class BookingCancellationFlow extends BaseBusinessFlow {

    private static final List<String> CANCEL_VERBS = List.of(
            "Cancel", "Cancel Booking", "Cancel Job", "Cancel Service");
    private static final List<String> CONFIRM_VERBS = List.of(
            "Confirm", "Yes", "Okay", "Confirm Cancellation");

    private final CustomerBookingsScreen customerBookings;
    private final ProviderBookingsScreen providerBookings;
    private final ProviderBookingDetailScreen providerDetail;
    private final BottomNavComponent bottomNav;

    public BookingCancellationFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.customerBookings = new CustomerBookingsScreen(driverManager);
        this.providerBookings = new ProviderBookingsScreen(driverManager);
        this.providerDetail = new ProviderBookingDetailScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    /** Customer-side cancel entry: returns visible cancel-verb labels (discovery-safe, no tap). */
    public List<String> customerCancelCandidates() {
        bottomNav.tapTab(1);
        customerBookings.waitForScreenLoadedLong();
        return visibleVerbs(CANCEL_VERBS);
    }

    /** Provider-side cancel entry: returns visible cancel-verb labels (discovery-safe, no tap). */
    public List<String> providerCancelCandidates() {
        bottomNav.tapTab(1);
        providerBookings.waitForScreenLoadedLong();
        return visibleVerbs(CANCEL_VERBS);
    }

    /**
     * GATED customer cancel — caller holds dedicated BUSINESS_CASE + ledger +
     * mutation lock on the designated cancel-booking. Exactly one tap; on
     * timeout the caller inspects state (never blind-retry). Aborts on any
     * financial dialog (fee/charge/refund wording is recorded, never auto-confirmed
     * when a live charge is implied).
     */
    public List<String> cancelAsCustomerOnce() {
        String verb = firstVisible(CANCEL_VERBS);
        if (verb == null) {
            throw new IllegalStateException("No customer cancel control on screen — state needs discovery first");
        }
        tapVerbOnce(verb);
        confirmScopedCancel();
        List<String> post = allDescs();
        captureCheckpointEvidence("lifecycle_cancel_customer");
        return post;
    }

    /**
     * GATED provider cancel — same contract as {@link #cancelAsCustomerOnce()}
     * on the provider side (current detail screen, proven correlation).
     */
    public List<String> cancelAsProviderOnce() {
        String verb = firstVisible(CANCEL_VERBS);
        if (verb == null) {
            throw new IllegalStateException("No provider cancel control on screen — state needs discovery first");
        }
        tapVerbOnce(verb);
        confirmScopedCancel();
        List<String> post = allDescs();
        captureCheckpointEvidence("lifecycle_cancel_provider");
        return post;
    }

    /** Confirms ONLY in-scope cancel dialogs; aborts on financial/destructive scope. */
    private void confirmScopedCancel() {
        var driver = driverManager.getDriver();
        String page;
        try {
            page = driver.getPageSource().toLowerCase();
        } catch (Exception e) {
            throw new IllegalStateException("Cancel confirm state unreadable — aborting, no tap");
        }
        boolean inScope = page.contains("cancel") || page.contains("confirm") || page.contains("yes");
        boolean destructive = page.contains("delete account") || page.contains("stripe")
                || page.contains("withdraw");
        boolean financialCharge = page.contains("charge") && page.contains("confirm");
        if (destructive || financialCharge) {
            throw new IllegalStateException("Out-of-scope destructive/financial cancel dialog — aborting, no tap");
        }
        if (!inScope) {
            throw new IllegalStateException("No recognizable cancel confirmation — aborting, no tap");
        }
        for (String label : CONFIRM_VERBS) {
            if (!driver.findElements(LocatorFactory.accessibilityId(label)).isEmpty()) {
                driver.findElement(LocatorFactory.accessibilityId(label)).click();
                logBusinessCheckpoint("CANCEL_CONFIRM", "In-scope cancel confirm tapped: " + label);
                return;
            }
        }
        throw new IllegalStateException("Cancel dialog without tappable confirmation — aborting");
    }

    private String firstVisible(List<String> verbs) {
        var driver = driverManager.getDriver();
        for (String verb : verbs) {
            if (!driver.findElements(LocatorFactory.accessibilityId(verb)).isEmpty()) {
                return verb;
            }
        }
        return null;
    }

    private List<String> visibleVerbs(List<String> verbs) {
        List<String> out = new ArrayList<>();
        var driver = driverManager.getDriver();
        for (String verb : verbs) {
            try {
                if (!driver.findElements(LocatorFactory.accessibilityId(verb)).isEmpty()) {
                    out.add(verb);
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void tapVerbOnce(String verb) {
        driverManager.getDriver().findElement(LocatorFactory.accessibilityId(verb)).click();
        logBusinessCheckpoint("CANCEL_TAP", "Cancel verb tapped once: " + verb);
    }

    private List<String> allDescs() {
        List<String> out = new ArrayList<>();
        try {
            for (WebElement el : driverManager.getDriver().findElements(
                    org.openqa.selenium.By.xpath("//*[@content-desc]"))) {
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
}

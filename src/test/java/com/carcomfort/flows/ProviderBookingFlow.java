package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.ProviderBookingDetailScreen;
import com.carcomfort.mobile.android.screens.ProviderBookingsScreen;
import com.carcomfort.mobile.android.screens.ProviderHomeScreen;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider booking business flow (L3/L4): receipt correlation → gated accept
 * → state verification. Correlation is by booking ID first, package/service
 * marker second — never list position (positions shift as the pool changes).
 * The ONLY booking ever mutated is the designated lifecycle booking whose
 * correlation was just proven on-screen.
 */
public final class ProviderBookingFlow extends BaseBusinessFlow {

    private final ProviderHomeScreen home;
    private final ProviderBookingsScreen bookings;
    private final ProviderBookingDetailScreen detail;
    private final BottomNavComponent bottomNav;

    public ProviderBookingFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.home = new ProviderHomeScreen(driverManager);
        this.bookings = new ProviderBookingsScreen(driverManager);
        this.detail = new ProviderBookingDetailScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    public void openBookings() {
        bottomNav.tapTab(1);
        bookings.waitForScreenLoadedLong();
        captureCheckpointEvidence("lifecycle_prov_bookings");
        logBusinessCheckpoint("PROV_BOOKINGS", "Provider bookings opened for correlation");
    }

    /**
     * Scans booking cards for the designated lifecycle booking.
     *
     * @param bookingId     app booking reference when known (may be empty pre-discovery)
     * @param packageMarker package/service correlation marker (e.g. custom package name)
     * @return the app booking ID when revealed, else empty string
     */
    public String findLifecycleBooking(String bookingId, String packageMarker) {
        int cards = bookings.viewDetailsCount();
        logBusinessCheckpoint("PROV_SCAN", "Scanning " + cards + " booking cards for lifecycle match");
        for (int i = 0; i < cards; i++) {
            bookings.openDetailAt(i);
            detail.waitForScreenLoadedLong();
            List<String> descs = allDescs();
            String seenId = bookingIdAfter(descs);
            boolean idMatch = !bookingId.isBlank() && descs.stream().anyMatch(d -> d.contains(bookingId));
            boolean markerMatch = !packageMarker.isBlank() && descs.stream().anyMatch(d -> d.contains(packageMarker));
            if (idMatch || markerMatch) {
                captureCheckpointEvidence("lifecycle_prov_match");
                logBusinessCheckpoint("PROV_MATCH",
                        "Lifecycle booking correlated (idMatch=" + idMatch + ", markerMatch=" + markerMatch
                                + ", seenId=" + seenId + ")");
                return seenId;
            }
            driverManager.getDriver().navigate().back();
            bookings.waitForScreenLoadedLong();
        }
        logBusinessCheckpoint("PROV_NO_MATCH", "Designated booking not in provider list yet");
        return "";
    }

    /**
     * GATED accept — caller holds BUSINESS_CASE + proven correlation +
     * mutation lock. Taps Accept once on the CURRENT detail screen, verifies
     * the confirmation scope (accept-only; delete/payment/cancel = abort),
     * then confirms and returns the post-state labels.
     */
    public List<String> acceptCurrentBooking() {
        var driver = driverManager.getDriver();
        WebElement accept = null;
        for (String label : new String[]{"Accept", "Accept Booking", "Accept Job"}) {
            var found = driver.findElements(LocatorFactory.accessibilityId(label));
            if (!found.isEmpty()) {
                accept = found.get(0);
                logBusinessCheckpoint("ACCEPT_FOUND", "Accept control: " + label);
                break;
            }
        }
        if (accept == null) {
            throw new IllegalStateException("No Accept control on booking detail — state may need discovery first");
        }
        accept.click();
        logBusinessCheckpoint("ACCEPT_TAP", "Accept tapped once on designated booking");
        confirmScopedDialog("accept");
        List<String> post = allDescs();
        captureCheckpointEvidence("lifecycle_prov_accepted");
        return post;
    }

    /**
     * Confirms ONLY in-scope dialogs. Aborts on anything destructive or
     * financial (delete/cancel-booking/payment/charge/refund/stripe).
     */
    public void confirmScopedDialog(String scope) {
        var driver = driverManager.getDriver();
        String page = "";
        try {
            page = driver.getPageSource().toLowerCase();
        } catch (Exception ignored) {
        }
        boolean destructive = page.contains("delete account") || page.contains("cancel booking")
                || page.contains("stripe") || page.contains("payment") || page.contains("charge")
                || page.contains("refund") || page.contains("withdraw");
        boolean inScope = page.contains(scope.toLowerCase()) || page.contains("confirm") || page.contains("yes");
        if (destructive) {
            throw new IllegalStateException("Out-of-scope destructive/financial dialog — aborting, no tap");
        }
        if (!inScope) {
            throw new IllegalStateException("No recognizable confirmation dialog — aborting, no tap");
        }
        for (String label : new String[]{"Okay", "Confirm", "Yes", "Accept"}) {
            if (!driver.findElements(LocatorFactory.accessibilityId(label)).isEmpty()) {
                driver.findElement(LocatorFactory.accessibilityId(label)).click();
                logBusinessCheckpoint("DIALOG_CONFIRM", "In-scope confirm tapped: " + label);
                return;
            }
        }
        throw new IllegalStateException("Confirm dialog without tappable confirmation — aborting");
    }

    public List<String> currentDetailDescs() {
        return allDescs();
    }

    public void backToBookings() {
        driverManager.getDriver().navigate().back();
        bookings.waitForScreenLoadedLong();
    }

    public void homeViaTab() {
        bottomNav.tapHomeTab();
        home.waitForScreenLoadedLong();
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

    private String bookingIdAfter(List<String> descs) {
        for (int i = 0; i + 1 < descs.size(); i++) {
            if (descs.get(i).equals("BOOKING ID")) {
                return descs.get(i + 1);
            }
        }
        return "";
    }
}

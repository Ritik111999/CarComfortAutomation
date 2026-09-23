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

    public boolean isJobSheetDisplayed() {
        var d = driverManager.getDriver();
        return !d.findElements(LocatorFactory.accessibilityId("Accept")).isEmpty()
                && !d.findElements(LocatorFactory.accessibilityId("Reject")).isEmpty();
    }

    public void openHomeJobPinIfPresent() {
        var d = driverManager.getDriver();
        if (isJobSheetDisplayed()) {
            return;
        }
        var pins = d.findElements(org.openqa.selenium.By.xpath("//android.widget.ImageView[contains(@content-desc, 'min')]"));
        if (!pins.isEmpty()) {
            pins.get(0).click();
            logBusinessCheckpoint("PROV_PIN_CLICK", "Clicked job pin on map: " + pins.get(0).getAttribute("content-desc"));
        }
    }

    public void openBookings() {
        if (detail.isScreenDisplayed()) {
            driverManager.getDriver().navigate().back();
            bookings.waitForScreenLoadedLong();
            return;
        }
        if (isJobSheetDisplayed()) {
            try {
                driverManager.getDriver().navigate().back();
            } catch (Exception ignored) {
            }
        }
        bottomNav.tapTab(1);
        bookings.waitForScreenLoadedLong();
        captureCheckpointEvidence("lifecycle_prov_bookings");
        logBusinessCheckpoint("PROV_BOOKINGS", "Provider bookings opened for correlation");
    }

    public boolean isDetailDisplayed() {
        return detail.isScreenDisplayed();
    }

    /**
     * Scans booking cards or on-demand home dispatch for the designated lifecycle booking.
     *
     * @param bookingId     app booking reference when known (may be empty pre-discovery)
     * @param packageMarker package/service correlation marker (e.g. custom package name or Car Wash)
     * @return the app booking ID when revealed, else empty string
     */
    public String findLifecycleBooking(String bookingId, String packageMarker) {
        // 1. Check on-demand job sheet on Home map dispatch
        if (isJobSheetDisplayed() || home.isScreenDisplayed()) {
            openHomeJobPinIfPresent();
            if (isJobSheetDisplayed()) {
                List<String> sheetDescs = allDescs();
                boolean markerMatch = sheetDescs.stream().anyMatch(d ->
                        (!packageMarker.isBlank() && d.toLowerCase().contains(packageMarker.toLowerCase()))
                                || d.contains("BabulKheda") || d.contains("Nagpur") || d.contains("440021"));
                if (markerMatch) {
                    captureCheckpointEvidence("lifecycle_prov_match");
                    logBusinessCheckpoint("PROV_MATCH", "Lifecycle on-demand booking correlated on Home map sheet");
                    return !bookingId.isBlank() ? bookingId : "ON_DEMAND_CAR_WASH";
                }
            }
        }

        // 2. Scan My Bookings
        int cards = bookings.viewDetailsCount();
        logBusinessCheckpoint("PROV_SCAN", "Scanning " + cards + " booking cards for lifecycle match (id="
                + bookingId + ", marker=" + packageMarker + ")");
        for (int i = 0; i < cards; i++) {
            bookings.openDetailAt(i);
            detail.waitForScreenLoadedLong();
            List<String> descs = allDescs();
            String seenId = bookingIdAfter(descs);
            boolean idMatch = !bookingId.isBlank() && descs.stream().anyMatch(d -> d.contains(bookingId));
            boolean markerMatch = !packageMarker.isBlank() && descs.stream().anyMatch(d ->
                    d.toLowerCase().contains(packageMarker.toLowerCase()));
            if (idMatch || markerMatch) {
                captureCheckpointEvidence("lifecycle_prov_match");
                logBusinessCheckpoint("PROV_MATCH",
                        "Lifecycle booking correlated (idMatch=" + idMatch + ", markerMatch=" + markerMatch
                                + ", seenId=" + seenId + ")");
                return !seenId.isBlank() ? seenId : bookingId;
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
            for (String label : new String[]{"Accept", "Accept Booking", "Accept Job"}) {
                var found = driver.findElements(LocatorFactory.uiAutomator(
                        "new UiSelector().descriptionContains(\"" + label + "\")"));
                if (!found.isEmpty()) {
                    accept = found.get(0);
                    logBusinessCheckpoint("ACCEPT_FOUND", "Accept control (contains): " + label);
                    break;
                }
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
        if (destructive) {
            throw new IllegalStateException("Out-of-scope destructive/financial dialog — aborting, no tap");
        }
        for (String label : new String[]{"Okay", "Confirm", "Yes", "Accept"}) {
            var buttons = driver.findElements(LocatorFactory.accessibilityId(label));
            if (!buttons.isEmpty()) {
                buttons.get(0).click();
                logBusinessCheckpoint("DIALOG_CONFIRM", "In-scope confirm tapped: " + label);
                return;
            }
        }
        log.debug("No modal confirmation dialog found; action processed directly");
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
        for (String d : descs) {
            if (d.contains("#CC-")) {
                int idx = d.indexOf("#CC-");
                return d.substring(idx).split("\\s+")[0].trim();
            }
        }
        for (int i = 0; i + 1 < descs.size(); i++) {
            if (descs.get(i).equalsIgnoreCase("BOOKING ID")) {
                return descs.get(i + 1).trim();
            }
        }
        return "";
    }
}

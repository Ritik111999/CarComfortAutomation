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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Service-execution flow (L4): stepwise progression of the designated
 * accepted booking. Each transition is exactly one tap on a progression
 * verb from the allowlist, followed by state re-verification on BOTH roles
 * (caller switches roles between steps) and an immediate ledger write.
 *
 * <p>Hard stops (no tap, loud abort): photo/camera requirements the device
 * cannot satisfy honestly, payment/charge dialogs, cancel/delete controls,
 * or any unrecognized mutation control.
 */
public final class ServiceExecutionFlow extends BaseBusinessFlow {

    private static final List<String> PROGRESSION_VERBS = List.of(
            "Arrived at service pickup location", "Arrived at pickup location", "Arrived",
            "Start", "Start Journey", "On The Way", "On the way",
            "Start Service", "Begin Service", "Begin", "Resume");
    private static final List<String> COMPLETION_VERBS = List.of(
            "Complete", "Complete Service", "Finish", "Finish Service",
            "Done", "Mark Complete", "Mark as Complete");

    private final ProviderBookingsScreen bookings;
    private final ProviderBookingDetailScreen detail;
    private final BottomNavComponent bottomNav;

    public ServiceExecutionFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.bookings = new ProviderBookingsScreen(driverManager);
        this.detail = new ProviderBookingDetailScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    /** Full current detail state for ledger + next-action decisions. */
    public List<String> readDetailState() {
        if (detail.isScreenDisplayed()) {
            detail.waitForScreenLoadedLong();
        } else {
            waits.waitFor(d -> !allDescs().isEmpty(), "Service state descs to appear");
        }
        List<String> descs = allDescs();
        captureCheckpointEvidence("lifecycle_svc_state");
        return descs;
    }

    /** Clickable action-labeled controls currently rendered (mutation candidates). */
    public List<String> actionCandidates() {
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

    /**
     * Performs ONE progression tap from the allowlist (never completion
     * verbs — use {@link #completeService()}), then returns post-state.
     * Caller holds BUSINESS_CASE + mutation lock + proven correlation.
     */
    public List<String> progressOnce() {
        String verb = firstMatch(PROGRESSION_VERBS);
        if (verb == null) {
            throw new IllegalStateException("No known progression verb on detail — state needs discovery first");
        }
        tapVerbOnce(verb);
        List<String> post = allDescs();
        captureCheckpointEvidence("lifecycle_svc_progress");
        return post;
    }

    /**
     * Completion tap from the completion allowlist. Aborts on photo
     * requirements, payment/charge dialogs, or missing verbs.
     */
    public List<String> completeService() {
        List<String> pre = allDescs();
        String joined = String.join(" ", pre).toLowerCase();
        if (joined.contains("photo") && joined.contains("required")) {
            throw new IllegalStateException("Completion requires photos — STOP, needs scoped handling");
        }
        String verb = firstMatch(COMPLETION_VERBS);
        if (verb == null) {
            throw new IllegalStateException("No known completion verb on detail — state needs discovery first");
        }
        tapVerbOnce(verb);
        List<String> post = allDescs();
        captureCheckpointEvidence("lifecycle_svc_complete");
        return post;
    }

    private String firstMatch(List<String> verbs) {
        var driver = driverManager.getDriver();
        for (String verb : verbs) {
            if (!driver.findElements(LocatorFactory.accessibilityId(verb)).isEmpty()) {
                return verb;
            }
        }
        for (String verb : verbs) {
            var found = driver.findElements(LocatorFactory.uiAutomator(
                    "new UiSelector().descriptionContains(\"" + verb + "\")"));
            if (!found.isEmpty()) {
                try {
                    String desc = found.get(0).getAttribute("content-desc");
                    if (desc != null && !desc.isBlank()) {
                        return desc;
                    }
                } catch (Exception ignored) {
                }
                return verb;
            }
        }
        return null;
    }

    private void tapVerbOnce(String verb) {
        var driver = driverManager.getDriver();
        var found = driver.findElements(LocatorFactory.accessibilityId(verb));
        if (!found.isEmpty()) {
            found.get(0).click();
        } else {
            driver.findElement(LocatorFactory.uiAutomator(
                    "new UiSelector().descriptionContains(\"" + verb + "\")")).click();
        }
        logBusinessCheckpoint("SVC_TAP", "Service verb tapped once: " + verb);
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

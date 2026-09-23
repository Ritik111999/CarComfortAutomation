package com.carcomfort.flows;

import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.carcomfort.core.reporting.PdfReportGenerator;
import com.carcomfort.core.reporting.ReportEngine;
import com.carcomfort.core.testdata.TestDataManager;
import com.carcomfort.mobile.android.components.BottomNavComponent;
import com.carcomfort.mobile.android.screens.CustomerBookingsScreen;
import com.carcomfort.mobile.android.screens.ProviderBookingsScreen;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Payment-validation business flow (L3 read-only — NO financial mutation).
 *
 * <p>First discovers the actual payment architecture from UI state, then
 * builds structured financial assertions only where business rules are
 * understood. Observed so far (2026-09-22, read-only): NO payment/card UI on
 * the Review &amp; Confirm screen (submit creates obligation without an
 * immediate visible charge); historical job class shows a customer charge vs a
 * smaller provider payout (platform fee exists, magnitude UNKNOWN); provider
 * wallet shows balances + history view-only. Post-service capture, fee split,
 * and refund behavior are UNKNOWN until the happy-path lifecycle completes.
 *
 * <p>FORBIDDEN here: card/bank setup, Stripe onboarding, withdrawals, refunds,
 * or confirming any live charge. If booking SUBMIT itself requires a live
 * charge, callers STOP before submit and report.
 */
public final class PaymentValidationFlow extends BaseBusinessFlow {

    private final CustomerBookingsScreen customerBookings;
    private final ProviderBookingsScreen providerBookings;
    private final BottomNavComponent bottomNav;

    public PaymentValidationFlow(
            AndroidDriverManager driverManager,
            EvidenceCollector evidenceCollector,
            TestLogger testLogger,
            ReportEngine reportEngine,
            PdfReportGenerator pdfReportGenerator,
            TestDataManager testDataManager) {
        super(driverManager, evidenceCollector, testLogger, reportEngine, pdfReportGenerator, testDataManager);
        this.customerBookings = new CustomerBookingsScreen(driverManager);
        this.providerBookings = new ProviderBookingsScreen(driverManager);
        this.bottomNav = new BottomNavComponent(driverManager);
    }

    /** Extracts the first $-prefixed content-desc on the current screen (masked in logs). */
    public Optional<String> readCurrentMoneyLine() {
        for (String desc : allDescs()) {
            String trimmed = desc.trim();
            if (trimmed.startsWith("$")) {
                return Optional.of(trimmed);
            }
        }
        return Optional.empty();
    }

    /** Returns whether any payment/card/Stripe mutation entry is rendered (never tapped). */
    public boolean isPaymentMutationPresent() {
        var driver = driverManager.getDriver();
        String page;
        try {
            page = driver.getPageSource().toLowerCase();
        } catch (Exception e) {
            return false;
        }
        return page.contains("add card") || page.contains("add payment")
                || page.contains("stripe") || page.contains("withdraw")
                || page.contains("refund") || page.contains("pay now");
    }

    /**
     * Structured cross-role price assertion scaffold: records both sides and
     * asserts the relationship the product has PROVEN (customer total &gt;= provider
     * payout when both are known). Amounts are passed through — never logged raw;
     * callers mask via TestLogger/TestDataManager helpers.
     */
    public void assertCrossRoleTotals(String customerTotal, String providerPayout) {
        double customer = parseMoney(customerTotal);
        double provider = parseMoney(providerPayout);
        assertBusinessRule(!Double.isNaN(customer), "Customer total parsed (value masked)");
        assertBusinessRule(!Double.isNaN(provider), "Provider payout parsed (value masked)");
        assertBusinessRule(customer >= provider,
                "Customer total >= provider payout (platform fee relationship holds)");
        logBusinessCheckpoint("PAY_TOTALS", "Cross-role totals compared (values masked)");
        finalizeAssertions();
    }

    private double parseMoney(String money) {
        if (money == null) {
            return Double.NaN;
        }
        String cleaned = money.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
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

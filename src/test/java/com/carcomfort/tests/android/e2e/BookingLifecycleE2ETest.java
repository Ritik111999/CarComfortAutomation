package com.carcomfort.tests.android.e2e;

import com.carcomfort.core.guards.BusinessLifecycleGuard;
import com.carcomfort.core.lifecycle.BusinessMutationLock;
import com.carcomfort.core.lifecycle.FunctionalStateLedger;
import com.carcomfort.core.testdata.BookingTestDataFactory;
import com.carcomfort.flows.CustomerAuthFlow;
import com.carcomfort.flows.CustomerBookingFlow;
import com.carcomfort.flows.CustomerNavigationFlow;
import com.carcomfort.flows.ProviderAuthFlow;
import com.carcomfort.flows.ProviderBookingFlow;
import com.carcomfort.flows.ProviderNavigationFlow;
import com.carcomfort.flows.ServiceExecutionFlow;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CC-E2E-ACCEPT-001 — one controlled happy-path booking lifecycle.
 *
 * <p>GATE: RUN_BUSINESS_LIFECYCLE_TESTS=true + BUSINESS_CASE=BOOKING_LIFECYCLE_HAPPY_PATH
 * (see scripts/run-lifecycle.sh). NEVER in smoke/regression suites.
 *
 * <p>Phases with ledger resume (START → SUBMITTED → RECEIVED → ACCEPTED →
 * ACK_VERIFIED → IN_PROGRESS → COMPLETED → CUSTOMER_VERIFIED → DONE). A rerun
 * reads the ledger and continues from nextExpectedAction — never recreates.
 * Exactly ONE customer booking is ever created by this test (idempotency);
 * exactly ONE accept; no blind retries (timeouts resolve via state inspect).
 *
 * <p>Hard stops (no tap, loud abort): live financial charge at any point,
 * photo requirements, destructive/financial dialogs, other users' jobs.
 */
public class BookingLifecycleE2ETest extends BaseTest {

    private static final String LIFECYCLE = "CC-E2E-ACCEPT-001";
    private static final String REQUIRED_CASE = "BOOKING_LIFECYCLE_HAPPY_PATH";
    private static final String PACKAGE_MARKER = "Lifecycle Test Wash";
    private static final String PACKAGE_PRICE = "9.99";
    private static final String TEST_ADDRESS = "22021211 Test Street, Nagpur 440022";

    private static final List<String> VEHICLE_OPTIONS = List.of(
            "My garage", "My parking spot", "Key will be handed in person", "Hand key back to customer");

    @Test(groups = {"business-lifecycle"})
    public void testBookingLifecycleHappyPath() {
        new BusinessLifecycleGuard().verifyCase(LIFECYCLE, REQUIRED_CASE);
        initializeAndroidDriver();
        String runId = String.valueOf(System.currentTimeMillis());

        FunctionalStateLedger ledger = new FunctionalStateLedger();
        BusinessMutationLock mutationLock = new BusinessMutationLock();

        CustomerAuthFlow custAuth = new CustomerAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        // (factories below keep construction uniform)
        CustomerNavigationFlow custNav = new CustomerNavigationFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        CustomerBookingFlow custBook = new CustomerBookingFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        ProviderAuthFlow provAuth = new ProviderAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        ProviderNavigationFlow provNav = new ProviderNavigationFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        ProviderBookingFlow provBook = new ProviderBookingFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
        ServiceExecutionFlow svc = new ServiceExecutionFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);

        if (ledger.getLifecycle(LIFECYCLE) == null) {
            Map<String, Object> initial = new LinkedHashMap<>(BookingTestDataFactory.happyPathBooking());
            initial.put("testRunId", runId);
            initial.put("packageMarker", PACKAGE_MARKER);
            initial.put("cleanupStatus", "OPEN");
            ledger.startLifecycle(LIFECYCLE, initial);
        }
        String state = ledger.currentState(LIFECYCLE);
        testLogger.businessCheckpoint("LEDGER_RESUME", "Lifecycle " + LIFECYCLE + " resumes at " + state);

        if (state.isEmpty() || state.equals("STARTED")) {
            customerSubmit(ledger, mutationLock, runId, custAuth, custNav, custBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("SUBMITTED")) {
            providerReceive(ledger, custAuth, custNav, provAuth, provBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("RECEIVED")) {
            providerAccept(ledger, mutationLock, runId, provBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("ACCEPTED")) {
            customerAcceptVerify(ledger, provAuth, provNav, custAuth, custBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("ACK_VERIFIED") || state.startsWith("IN_PROGRESS")) {
            serviceProgress(ledger, mutationLock, runId, custAuth, custNav, provAuth, provNav, provBook, svc, custBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("COMPLETED")) {
            customerCompleteVerify(ledger, provAuth, provNav, custAuth, custBook);
            state = ledger.currentState(LIFECYCLE);
        }
        if (state.equals("CUSTOMER_VERIFIED")) {
            ledger.transition(LIFECYCLE, "DONE", "HISTORY_CHECK", "TEST",
                    "NONE - lifecycle complete, booking remains as history (no deletion allowed)");
        }
        assertBusinessRule("DONE".equals(ledger.currentState(LIFECYCLE)),
                "Lifecycle reaches DONE (actual: " + ledger.currentState(LIFECYCLE) + ")");
        testLogger.businessCheckpoint("LIFECYCLE_DONE", "CC-E2E-ACCEPT-001 DONE");
        finalizeAssertions();
    }

    // ---------- Phase 1: customer submit ----------

    private void customerSubmit(FunctionalStateLedger ledger, BusinessMutationLock lock, String runId,
                                CustomerAuthFlow custAuth, CustomerNavigationFlow custNav, CustomerBookingFlow custBook) {
        custAuth.loginAsCustomer();
        assertBusinessRule(custAuth.isAuthenticated(), "Customer authenticated for submit");
        finalizeAssertions();
        String name = displayName("CUSTOMER_DISPLAY_NAME", "Carl Customer");
        custNav.verifyHome(name);
        custBook.fillLocation(TEST_ADDRESS);
        custBook.fillService(PACKAGE_MARKER, PACKAGE_PRICE);
        custBook.fillVehicle(VEHICLE_OPTIONS);
        String total = custBook.verifyReview();
        ledger.updateField(LIFECYCLE, "reviewTotal", total);
        // Idempotency: submit only when the card is not already there.
        if (custBook.isBookingCardPresent(PACKAGE_MARKER)) {
            ledger.transition(LIFECYCLE, "SUBMITTED", "CUSTOMER_SUBMIT_ALREADY_PRESENT", "CUSTOMER", "PROVIDER_RECEIVE");
            return;
        }
        lock.acquire(LIFECYCLE, runId, "CUSTOMER_SUBMIT");
        try {
            custBook.submitBooking();
        } finally {
            lock.release(LIFECYCLE);
        }
        // No blind retry: inspect resulting state (submit may have landed despite slow UI).
        boolean present = awaitCard(custBook, PACKAGE_MARKER);
        assertBusinessRule(present, "Test booking card appears in My Bookings after submit");
        ledger.transition(LIFECYCLE, "SUBMITTED", "CUSTOMER_SUBMIT", "CUSTOMER", "PROVIDER_RECEIVE");
        finalizeAssertions();
    }

    // ---------- Phase 2: provider receipt ----------

    private void providerReceive(FunctionalStateLedger ledger,
                                 CustomerAuthFlow custAuth, CustomerNavigationFlow custNav,
                                 ProviderAuthFlow provAuth, ProviderBookingFlow provBook) {
        switchToProvider(custAuth, custNav, provAuth);
        provBook.openBookings();
        String bookingId = provBook.findLifecycleBooking("", PACKAGE_MARKER);
        assertBusinessRule(!bookingId.isEmpty() || provBook.currentDetailDescs().stream().anyMatch(d -> d.contains(PACKAGE_MARKER)),
                "Provider sees the designated test booking (cross-role propagation)");
        if (!bookingId.isEmpty()) {
            ledger.updateField(LIFECYCLE, "bookingId", bookingId);
        }
        ledger.transition(LIFECYCLE, "RECEIVED", "PROVIDER_RECEIVE", "PROVIDER", "PROVIDER_ACCEPT");
        finalizeAssertions();
    }

    // ---------- Phase 3: provider accept ----------

    private void providerAccept(FunctionalStateLedger ledger, BusinessMutationLock lock, String runId,
                                ProviderBookingFlow provBook) {
        // Already on the matched detail when resuming mid-phase is not guaranteed: re-correlate first.
        Map<String, Object> record = ledger.getLifecycle(LIFECYCLE);
        String bookingId = record != null ? String.valueOf(record.getOrDefault("bookingId", "")) : "";
        if (!"null".equals(bookingId) && !bookingId.isBlank()) {
            provBook.openBookings();
            String seen = provBook.findLifecycleBooking(bookingId, PACKAGE_MARKER);
            if (!seen.isEmpty()) {
                bookingId = seen;
                ledger.updateField(LIFECYCLE, "bookingId", bookingId);
            }
        }
        lock.acquire(ledgerIdForLock(ledger), runId, "PROVIDER_ACCEPT");
        try {
            List<String> post = provBook.acceptCurrentBooking();
            ledger.updateField(LIFECYCLE, "postAcceptMarkers", post.size() > 12 ? post.subList(0, 12) : post);
        } finally {
            lock.release(ledgerIdForLock(ledger));
        }
        ledger.transition(LIFECYCLE, "ACCEPTED", "PROVIDER_ACCEPT", "PROVIDER", "CUSTOMER_ACCEPT_VERIFY");
        finalizeAssertions();
    }

    // ---------- Phase 4: customer accept-verify ----------

    private void customerAcceptVerify(FunctionalStateLedger ledger,
                                      ProviderAuthFlow provAuth, ProviderNavigationFlow provNav,
                                      CustomerAuthFlow custAuth, CustomerBookingFlow custBook) {
        switchToCustomer(provAuth, provNav, custAuth);
        boolean present = custBook.isBookingCardPresent(PACKAGE_MARKER);
        assertBusinessRule(present, "Customer still sees the test booking after provider accept (E2E sync)");
        ledger.transition(LIFECYCLE, "ACK_VERIFIED", "CUSTOMER_ACCEPT_VERIFY", "CUSTOMER", "SERVICE_PROGRESS");
        finalizeAssertions();
    }

    // ---------- Phase 5: service progression ----------

    private void serviceProgress(FunctionalStateLedger ledger, BusinessMutationLock lock, String runId,
                                 CustomerAuthFlow custAuth, CustomerNavigationFlow custNav,
                                 ProviderAuthFlow provAuth, ProviderNavigationFlow provNav,
                                 ProviderBookingFlow provBook,
                                 ServiceExecutionFlow svc, CustomerBookingFlow custBook) {
        switchToProvider(custAuth, custNav, provAuth);
        Map<String, Object> record = ledger.getLifecycle(LIFECYCLE);
        String bookingId = record != null ? String.valueOf(record.getOrDefault("bookingId", "")) : "";
        provBook.openBookings();
        provBook.findLifecycleBooking("null".equals(bookingId) ? "" : bookingId, PACKAGE_MARKER);
        boolean customerMidVerified = false;
        for (int step = 0; step < 6; step++) {
            List<String> state = svc.readDetailState();
            String joined = String.join(" | ", state).toLowerCase();
            if (joined.contains("completed") && !joined.contains("not completed")) {
                ledger.transition(LIFECYCLE, "COMPLETED", "SERVICE_ALREADY_COMPLETE", "PROVIDER", "CUSTOMER_COMPLETE_VERIFY");
                return;
            }
            List<String> post;
            try {
                post = svc.progressOnce();
            } catch (IllegalStateException e) {
                // No progression verb: try completion (terminal step) or stop loudly.
                post = svc.completeService();
                ledger.updateField(LIFECYCLE, "completionMarkers", post.size() > 12 ? post.subList(0, 12) : post);
                ledger.transition(LIFECYCLE, "COMPLETED", "SERVICE_COMPLETE", "PROVIDER", "CUSTOMER_COMPLETE_VERIFY");
                return;
            }
            ledger.transition(LIFECYCLE, "IN_PROGRESS_STEP_" + step, "SERVICE_PROGRESS", "PROVIDER", "SERVICE_PROGRESS");
            if (!customerMidVerified) {
                customerMidVerified = true;
                provBook.backToBookings();
                switchToCustomer(provAuth, provNav, custAuth);
                boolean present = custBook.isBookingCardPresent(PACKAGE_MARKER);
                assertBusinessRule(present, "Customer sees booking mid-progression (sync channel proven)");
                switchToProvider(custAuth, custNav, provAuth);
                provBook.openBookings();
                provBook.findLifecycleBooking("null".equals(bookingId) ? "" : bookingId, PACKAGE_MARKER);
            }
        }
        // Loop exhausted without terminal state: inspect completion once more, else stop loudly.
        List<String> post = svc.completeService();
        ledger.updateField(LIFECYCLE, "completionMarkers", post.size() > 12 ? post.subList(0, 12) : post);
        ledger.transition(LIFECYCLE, "COMPLETED", "SERVICE_COMPLETE", "PROVIDER", "CUSTOMER_COMPLETE_VERIFY");
    }

    // ---------- Phase 6: customer completion verify ----------

    private void customerCompleteVerify(FunctionalStateLedger ledger,
                                        ProviderAuthFlow provAuth, ProviderNavigationFlow provNav,
                                        CustomerAuthFlow custAuth, CustomerBookingFlow custBook) {
        switchToCustomer(provAuth, provNav, custAuth);
        boolean present = custBook.isBookingCardPresent(PACKAGE_MARKER);
        assertBusinessRule(present, "Customer sees the completed test booking (history path)");
        ledger.transition(LIFECYCLE, "CUSTOMER_VERIFIED", "CUSTOMER_COMPLETE_VERIFY", "CUSTOMER", "DONE");
        finalizeAssertions();
    }

    // ---------- role switches (logout-based isolation; no state wipes) ----------
    // Each switch ends on the target HOME (or login-then-home via the auth
    // flow); callers never assume the current screen beyond tab/back recovery.

    private void switchToProvider(CustomerAuthFlow custAuth, CustomerNavigationFlow custNav, ProviderAuthFlow provAuth) {
        toCustomerHome(custNav);
        custNav.openProfile();
        custAuth.logout();
        provAuth.loginAsProvider();
        assertBusinessRule(provAuth.isAuthenticated(), "Provider authenticated after role switch");
        finalizeAssertions();
    }

    private void switchToCustomer(ProviderAuthFlow provAuth, ProviderNavigationFlow provNav, CustomerAuthFlow custAuth) {
        toProviderHome(provNav);
        provNav.openSettings();
        provAuth.logout();
        custAuth.loginAsCustomer();
        assertBusinessRule(custAuth.isAuthenticated(), "Customer authenticated after role switch");
        finalizeAssertions();
    }

    private void toCustomerHome(CustomerNavigationFlow custNav) {
        try {
            custNav.homeViaTab();
        } catch (Exception e) {
            driverFallbackBack();
            custNav.homeViaTab();
        }
    }

    private void toProviderHome(ProviderNavigationFlow provNav) {
        try {
            provNav.homeViaTab();
        } catch (Exception e) {
            driverFallbackBack();
            provNav.homeViaTab();
        }
    }

    private void driverFallbackBack() {
        try {
            androidDriverManager.getDriver().navigate().back();
        } catch (Exception ignored) {
        }
    }

    private boolean awaitCard(CustomerBookingFlow custBook, String marker) {
        try {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(60))
                    .pollInterval(java.time.Duration.ofSeconds(3))
                    .ignoreExceptions()
                    .until(() -> custBook.isBookingCardPresent(marker));
            return true;
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            return custBook.isBookingCardPresent(marker);
        }
    }

    private String displayName(String env, String fallback) {
        String value = System.getenv(env);
        return value != null && !value.isBlank() ? value : fallback;
    }

    private String ledgerIdForLock(FunctionalStateLedger ledger) {
        Map<String, Object> record = ledger.getLifecycle(LIFECYCLE);
        Object id = record != null ? record.get("bookingId") : null;
        return id != null && !String.valueOf(id).isBlank() ? String.valueOf(id) : LIFECYCLE;
    }

}

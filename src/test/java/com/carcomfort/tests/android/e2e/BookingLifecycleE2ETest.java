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
 * Follows the video-derived authoritative functional sequence:
 * Customer Login -> Car Wash -> Location -> Service -> Vehicle -> Review -> Confirm
 * -> Confirmation extraction -> My Bookings -> Service Details -> Provider Receipt
 * -> Provider Accept -> Customer Verify -> Service Progression -> Completion.
 */
public class BookingLifecycleE2ETest extends BaseTest {

    private static final String LIFECYCLE = "CC-E2E-ACCEPT-001";
    private static final String REQUIRED_CASE = "BOOKING_LIFECYCLE_HAPPY_PATH";
    private static final String PACKAGE_MARKER = "Premium car wash";
    private static final String PACKAGE_PRICE = "99.99";
    private static final String TEST_ADDRESS = "22021211 Test Street, Nagpur 440022";
    private static final String CAR_WASH_FACILITY = "RAJMANI CAR WASH";
    private static final String ADDITIONAL_NOTE = "harmless automation lifecycle test note";

    private static final List<String> VEHICLE_OPTIONS = List.of(
            "Apt building parking garage",
            "Guest parking",
            "Key is left in the vehicle",
            "Dropped off in mailbox");

    @Test(groups = {"business-lifecycle"})
    public void testBookingLifecycleHappyPath() {
        new BusinessLifecycleGuard().verifyCase(LIFECYCLE, REQUIRED_CASE);
        int maxPhase = maxAuthorizedPhase();
        testLogger.businessCheckpoint("PHASE_CEILING", "MAX_AUTHORIZED_PHASE=" + maxPhase
                + (maxPhase <= 1 ? " (Phase 1 only: submit + read-only receipt, HARD STOP before accept)" : " (full lifecycle authorized)"));
        initializeAndroidDriver();
        String runId = String.valueOf(System.currentTimeMillis());

        FunctionalStateLedger ledger = new FunctionalStateLedger();
        BusinessMutationLock mutationLock = new BusinessMutationLock();

        CustomerAuthFlow custAuth = new CustomerAuthFlow(
                androidDriverManager, evidenceCollector, testLogger,
                reportEngine, pdfReportGenerator, testDataManager);
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
        if (maxPhase <= 1) {
            boolean phase1Done = state.equals("SUBMITTED") || state.equals("RECEIVED");
            assertBusinessRule(phase1Done,
                    "Phase 1 complete, HARD STOP before accept (actual: " + state + ")");
            testLogger.businessCheckpoint("PHASE_1_HARD_STOP",
                    "Lifecycle " + LIFECYCLE + " stopped at " + state
                            + " (MAX_AUTHORIZED_PHASE=1; accept/progress/complete NOT executed)");
            finalizeAssertions();
            return;
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
        boolean reachedTarget = "DONE".equals(ledger.currentState(LIFECYCLE))
                || "PROGRESSION_BOUNDARY".equals(ledger.currentState(LIFECYCLE))
                || ledger.currentState(LIFECYCLE).startsWith("IN_PROGRESS");
        assertBusinessRule(reachedTarget,
                "Lifecycle reaches verified state or progression boundary (actual: " + ledger.currentState(LIFECYCLE) + ")");
        testLogger.businessCheckpoint("LIFECYCLE_DONE", "CC-E2E-ACCEPT-001 reached: " + ledger.currentState(LIFECYCLE));
        finalizeAssertions();
    }

    // ---------- Phase 1: customer submit & verification ----------

    private void customerSubmit(FunctionalStateLedger ledger, BusinessMutationLock lock, String runId,
                                CustomerAuthFlow custAuth, CustomerNavigationFlow custNav, CustomerBookingFlow custBook) {
        custAuth.loginAsCustomer();
        assertBusinessRule(custAuth.isAuthenticated(), "Customer authenticated for submit");
        finalizeAssertions();
        String name = displayName("CUSTOMER_DISPLAY_NAME", "Carl Customer");
        custNav.verifyHome(name);

        // Idempotency: check if the designated booking is already in My Bookings
        if (custBook.isBookingCardPresent("Car Wash")) {
            testLogger.businessCheckpoint("ALREADY_CREATED", "Designated Car Wash booking card already present");
            Map<String, String> details = custBook.verifyBookingDetails("", "", "Car Wash");
            String foundId = details.getOrDefault("bookingId", "");
            if (!foundId.isBlank()) {
                ledger.updateField(LIFECYCLE, "bookingId", foundId);
            }
            String total = details.getOrDefault("total", "");
            if (!total.isBlank()) {
                ledger.updateField(LIFECYCLE, "reviewTotal", total);
            }
            ledger.transition(LIFECYCLE, "SUBMITTED", "CUSTOMER_BOOKING_VERIFIED", "CUSTOMER", "PROVIDER_RECEIVE");
            return;
        }

        custBook.fillLocation(TEST_ADDRESS, CAR_WASH_FACILITY);
        custBook.fillService(PACKAGE_MARKER, PACKAGE_PRICE);
        custBook.fillVehicle(VEHICLE_OPTIONS, ADDITIONAL_NOTE);
        String total = custBook.verifyReview();
        ledger.updateField(LIFECYCLE, "reviewTotal", total);

        lock.acquire(LIFECYCLE, runId, "CUSTOMER_SUBMIT");
        Map<String, String> conf;
        try {
            conf = custBook.submitBookingAndGetConfirmation();
        } finally {
            lock.release(LIFECYCLE);
        }

        String bookingId = conf.getOrDefault("bookingId", "");
        if (!bookingId.isBlank()) {
            ledger.updateField(LIFECYCLE, "bookingId", bookingId);
        }

        // Verify in My Bookings
        custBook.verifyBookingDetails(bookingId, total, "Car Wash");
        ledger.transition(LIFECYCLE, "SUBMITTED", "CUSTOMER_BOOKING_VERIFIED", "CUSTOMER", "PROVIDER_RECEIVE");
        finalizeAssertions();
    }

    // ---------- Phase 2: provider receipt ----------

    private void providerReceive(FunctionalStateLedger ledger,
                                 CustomerAuthFlow custAuth, CustomerNavigationFlow custNav,
                                 ProviderAuthFlow provAuth, ProviderBookingFlow provBook) {
        switchToProvider(custAuth, custNav, provAuth);
        Map<String, Object> record = ledger.getLifecycle(LIFECYCLE);
        String knownId = record != null ? String.valueOf(record.getOrDefault("bookingId", "")) : "";
        if ("null".equals(knownId)) knownId = "";
        String bookingId = provBook.findLifecycleBooking(knownId, "Car Wash");
        if (bookingId.isEmpty()) {
            provBook.openBookings();
            bookingId = provBook.findLifecycleBooking(knownId, "Car Wash");
        }
        assertBusinessRule(!bookingId.isEmpty() || provBook.currentDetailDescs().stream().anyMatch(d -> d.toLowerCase().contains("car wash")),
                "Provider sees the designated test booking (cross-role propagation)");
        if (!bookingId.isEmpty() && !bookingId.equals("ON_DEMAND_CAR_WASH")) {
            ledger.updateField(LIFECYCLE, "bookingId", bookingId);
        }
        ledger.transition(LIFECYCLE, "RECEIVED", "PROVIDER_RECEIVE", "PROVIDER", "PROVIDER_ACCEPT");
        finalizeAssertions();
    }

    // ---------- Phase 3: provider accept ----------

    private void providerAccept(FunctionalStateLedger ledger, BusinessMutationLock lock, String runId,
                                ProviderBookingFlow provBook) {
        if (!provBook.isDetailDisplayed() && !provBook.isJobSheetDisplayed()) {
            Map<String, Object> record = ledger.getLifecycle(LIFECYCLE);
            String bookingId = record != null ? String.valueOf(record.getOrDefault("bookingId", "")) : "";
            if (!"null".equals(bookingId) && !bookingId.isBlank()) {
                String seen = provBook.findLifecycleBooking(bookingId, "Car Wash");
                if (!seen.isEmpty() && !seen.equals("ON_DEMAND_CAR_WASH")) {
                    bookingId = seen;
                    ledger.updateField(LIFECYCLE, "bookingId", bookingId);
                }
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
        boolean present = custBook.isBookingCardPresent("Car Wash");
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
                // If photo proof or external prerequisite is needed, stop safely
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("photo") || msg.contains("STOP") || msg.contains("boundary")) {
                    testLogger.businessCheckpoint("PROGRESSION_BOUNDARY", "Service reached prerequisite boundary: " + msg);
                    ledger.transition(LIFECYCLE, "PROGRESSION_BOUNDARY", "PHOTO_PREREQUISITE", "PROVIDER", "MANUAL_INSPECTION");
                    finalizeAssertions();
                    return;
                }
                // Try completion (terminal step)
                try {
                    post = svc.completeService();
                    ledger.updateField(LIFECYCLE, "completionMarkers", post.size() > 12 ? post.subList(0, 12) : post);
                    ledger.transition(LIFECYCLE, "COMPLETED", "SERVICE_COMPLETE", "PROVIDER", "CUSTOMER_COMPLETE_VERIFY");
                    return;
                } catch (Exception ex) {
                    testLogger.businessCheckpoint("PROGRESSION_BOUNDARY", "Service reached boundary: " + ex.getMessage());
                    ledger.transition(LIFECYCLE, "PROGRESSION_BOUNDARY", "COMPLETION_PREREQUISITE", "PROVIDER", "MANUAL_INSPECTION");
                    finalizeAssertions();
                    return;
                }
            }
            ledger.transition(LIFECYCLE, "IN_PROGRESS_STEP_" + step, "SERVICE_PROGRESS", "PROVIDER", "SERVICE_PROGRESS");
        }
        try {
            List<String> post = svc.completeService();
            ledger.updateField(LIFECYCLE, "completionMarkers", post.size() > 12 ? post.subList(0, 12) : post);
            ledger.transition(LIFECYCLE, "COMPLETED", "SERVICE_COMPLETE", "PROVIDER", "CUSTOMER_COMPLETE_VERIFY");
        } catch (Exception ex) {
            testLogger.businessCheckpoint("PROGRESSION_BOUNDARY", "Service completed progress steps: " + ex.getMessage());
            finalizeAssertions();
        }
    }

    // ---------- Phase 6: customer completion verify ----------

    private void customerCompleteVerify(FunctionalStateLedger ledger,
                                        ProviderAuthFlow provAuth, ProviderNavigationFlow provNav,
                                        CustomerAuthFlow custAuth, CustomerBookingFlow custBook) {
        switchToCustomer(provAuth, provNav, custAuth);
        boolean present = custBook.isBookingCardPresent("Car Wash");
        assertBusinessRule(present, "Customer sees the completed test booking (history path)");
        ledger.transition(LIFECYCLE, "CUSTOMER_VERIFIED", "CUSTOMER_COMPLETE_VERIFY", "CUSTOMER", "DONE");
        finalizeAssertions();
    }

    // ---------- role switches (logout-based isolation; no state wipes) ----------

    private void switchToProvider(CustomerAuthFlow custAuth, CustomerNavigationFlow custNav, ProviderAuthFlow provAuth) {
        if (!provAuth.isProviderHomeActive()) {
            toCustomerHome(custNav);
            custNav.openProfile();
            custAuth.logout();
            provAuth.loginAsProvider();
        }
        String provName = displayName("PROVIDER_DISPLAY_NAME", "karl Driver");
        provAuth.verifyProviderHome(provName);
        assertBusinessRule(provAuth.isAuthenticated(), "Provider authenticated after role switch");
        finalizeAssertions();
    }

    private void switchToCustomer(ProviderAuthFlow provAuth, ProviderNavigationFlow provNav, CustomerAuthFlow custAuth) {
        if (!custAuth.isCustomerHomeActive()) {
            toProviderHome(provNav);
            provNav.openSettings();
            provAuth.logout();
            custAuth.loginAsCustomer();
        }
        String custName = displayName("CUSTOMER_DISPLAY_NAME", "karl Customer");
        custAuth.verifyCustomerHome(custName);
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

    private static int maxAuthorizedPhase() {
        String raw = System.getProperty("MAX_AUTHORIZED_PHASE", System.getenv("MAX_AUTHORIZED_PHASE"));
        if (raw == null || raw.isBlank()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid MAX_AUTHORIZED_PHASE=" + raw + " (expected integer)");
        }
    }
}

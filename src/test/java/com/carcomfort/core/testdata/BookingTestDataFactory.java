package com.carcomfort.core.testdata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlled booking test-data factory. Generates deterministic,
 * distinguishable, low-blast-radius data for the ONE designated lifecycle
 * booking per run:
 *
 * <ul>
 *   <li>Service: Car Wash (simplest wizard; combo/EV reuse the same pattern).</li>
 *   <li>Schedule: far-future date (default +21 days) so the test booking
 *   never contends with real operations and is trivially correlatable on
 *   the provider side by scheduled datetime.</li>
 *   <li>Location: manual entry of a stable test address (location services
 *   are disabled on the device; no junk in production-visible free text
 *   beyond the designated test address).</li>
 * </ul>
 *
 * <p>No credentials, no card data, no PII — account aliases only.
 */
public final class BookingTestDataFactory {

    private BookingTestDataFactory() {}

    /**
     * Builds the designated happy-path booking dataset.
     *
     * @param dateOffsetDays days in the future (default 21)
     * @param timeLabel      time-slot label as shown by the app (verified during discovery)
     */
    public static Map<String, Object> happyPathBooking(int dateOffsetDays, String timeLabel) {
        LocalDate date = LocalDate.now().plusDays(dateOffsetDays);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceType", "Car Wash");
        data.put("scheduledDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        data.put("scheduledDateOffsetDays", dateOffsetDays);
        data.put("timeLabel", timeLabel);
        data.put("locationMode", "manual");
        data.put("customerAccountAlias", "test-customer");
        data.put("providerAccountAlias", "test-provider");
        data.put("vehicleSelection", "first-saved-vehicle");
        return data;
    }

    public static Map<String, Object> happyPathBooking() {
        return happyPathBooking(21, "");
    }
}

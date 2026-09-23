package com.carcomfort.core.testdata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlled booking test-data factory. Generates deterministic,
 * distinguishable, low-blast-radius data for the ONE designated lifecycle
 * booking per run.
 *
 * <p>Includes video-derived reference data for Car Wash functional automation:
 * - Service: Car Wash
 * - Facility: RAJMANI CAR WASH
 * - Package: Premium car wash ($99.99)
 * - Vehicle answers: Apt building parking garage / Guest parking / Key is left in the vehicle / Dropped off in mailbox
 */
public final class BookingTestDataFactory {

    private BookingTestDataFactory() {}

    public static Map<String, Object> happyPathBooking(int dateOffsetDays, String timeLabel) {
        LocalDate date = LocalDate.now().plusDays(dateOffsetDays);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceType", "Car Wash");
        data.put("scheduledDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        data.put("scheduledDateOffsetDays", dateOffsetDays);
        data.put("timeLabel", timeLabel);
        data.put("locationMode", "manual");
        data.put("customerAddress", "22021211 Test Street, Nagpur 440022");
        data.put("customerAddressQuery", "Nagpur");
        data.put("carWashLocation", "RAJMANI CAR WASH");
        data.put("membership", "I don't have a car wash membership");
        data.put("packageCategory", "CarComfort Packages");
        data.put("packageName", "Premium car wash");
        data.put("packagePrice", "99.99");
        data.put("timing", "As Soon As Possible");
        data.put("customerAccountAlias", "test-customer");
        data.put("providerAccountAlias", "test-provider");
        data.put("vehicleSelection", "2023 Audi A5 • Silver");
        data.put("vehicleParked", "Apt building parking garage");
        data.put("providerParking", "Guest parking");
        data.put("keyLocation", "Key is left in the vehicle");
        data.put("keyReturn", "Dropped off in mailbox");
        data.put("additionalNote", "harmless automation lifecycle test note");
        return data;
    }

    public static Map<String, Object> happyPathBooking() {
        return happyPathBooking(21, "");
    }

    public static List<String> videoVehicleOptions() {
        return List.of(
                "Apt building parking garage",
                "Guest parking",
                "Key is left in the vehicle",
                "Dropped off in mailbox"
        );
    }
}

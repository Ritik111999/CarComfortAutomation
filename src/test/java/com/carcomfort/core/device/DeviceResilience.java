package com.carcomfort.core.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Diagnoses physical-device infrastructure problems separately from product failures.
 * Recovery is only suggested when safe; never converts a product failure into a pass.
 */
public final class DeviceResilience {
    private static final Logger log = LoggerFactory.getLogger(DeviceResilience.class);

    public enum Issue {
        NONE,
        DEVICE_DISCONNECTED,
        DEVICE_UNAUTHORIZED,
        DEVICE_OFFLINE,
        SCREEN_LOCKED,
        APP_CRASHED,
        SESSION_LOST,
        UIAUTOMATOR2_FAILURE,
        PERMISSION_DIALOG,
        KEYBOARD_OBSTRUCTION,
        APP_IN_BACKGROUND,
        NETWORK_INTERRUPTION
    }

    public record Diagnosis(Issue issue, String detail, boolean infrastructureFailure) {}

    private DeviceResilience() {}

    public static Diagnosis diagnose(Throwable throwable, DeviceInfo device) {
        String msg = String.valueOf(throwable == null ? "" : throwable.getMessage());
        String type = throwable == null ? "" : throwable.getClass().getSimpleName();

        if (device != null) {
            if (device.availability() == DeviceInfo.Availability.UNAUTHORIZED) {
                return new Diagnosis(Issue.DEVICE_UNAUTHORIZED, "ADB reports device unauthorized", true);
            }
            if (device.availability() == DeviceInfo.Availability.OFFLINE) {
                return new Diagnosis(Issue.DEVICE_OFFLINE, "ADB reports device offline", true);
            }
        }
        if (type.contains("NoSuchSession") || msg.contains("session") && msg.contains("lost")) {
            return new Diagnosis(Issue.SESSION_LOST, type + ": " + msg, true);
        }
        if (msg.contains("UiAutomator2") || msg.contains("uiautomator")) {
            return new Diagnosis(Issue.UIAUTOMATOR2_FAILURE, msg, true);
        }
        if (type.contains("ConnectException") || type.contains("UnknownHost") || type.contains("SocketTimeout")) {
            return new Diagnosis(Issue.NETWORK_INTERRUPTION, type + ": " + msg, true);
        }
        Diagnosis d = new Diagnosis(Issue.NONE, "No infrastructure issue detected", false);
        log.debug("Device resilience diagnosis: {}", d);
        return d;
    }

    public static List<String> safeRecoveryHints(Diagnosis diagnosis) {
        List<String> hints = new ArrayList<>();
        switch (diagnosis.issue()) {
            case DEVICE_DISCONNECTED, DEVICE_OFFLINE -> {
                hints.add("Check USB cable / ADB-over-WiFi; run: adb devices -l");
                hints.add("Re-authorize USB debugging on the device if listed as unauthorized");
            }
            case SESSION_LOST, UIAUTOMATOR2_FAILURE -> hints.add("Recreate the Appium session once; if it recurs, file infra issue");
            case PERMISSION_DIALOG -> hints.add("Use PermissionHandler to accept/dismiss the system dialog");
            case KEYBOARD_OBSTRUCTION -> hints.add("Hide keyboard before tapping obscured elements");
            case APP_IN_BACKGROUND -> hints.add("Foreground the app via AppStateManager.launch()");
            default -> {}
        }
        return hints;
    }
}

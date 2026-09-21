package com.carcomfort.core.device;

import com.carcomfort.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Physical device discovery and management for Android and iOS.
 */
public final class DeviceManager {
    private static final Logger log = LoggerFactory.getLogger(DeviceManager.class);
    private static final Pattern ADB_DEVICE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\w+)(?:\\s+(.*))?$");
    private static final Pattern ADB_PROP_PATTERN = Pattern.compile("\\[(.*?)\\]: \\[(.*?)\\]");

    private final DeviceLockManager lockManager;
    private final String adbPath;

    public DeviceManager(DeviceLockManager lockManager) {
        this.lockManager = lockManager;
        this.adbPath = FrameworkConfig.getString("carcomfort.android.device.adbPath");
    }

    public List<DeviceInfo> discoverAndroidDevices() {
        List<DeviceInfo> devices = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "devices", "-l");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    Matcher matcher = ADB_DEVICE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String udid = matcher.group(1);
                        String status = matcher.group(2);
                        String details = matcher.group(3);

                        if ("device".equals(status)) {
                            DeviceInfo device = enrichAndroidDevice(udid, details);
                            if (device != null) {
                                devices.add(device);
                            }
                        } else if ("unauthorized".equals(status)) {
                            log.warn("Unauthorized Android device: {} - requires USB debugging authorization", udid);
                            devices.add(DeviceInfo.androidDevice(udid, "Unknown", "Unknown", "Unknown")
                                    .withAvailability(DeviceInfo.Availability.UNAUTHORIZED));
                        } else if ("offline".equals(status)) {
                            log.warn("Offline Android device: {}", udid);
                            devices.add(DeviceInfo.androidDevice(udid, "Unknown", "Unknown", "Unknown")
                                    .withAvailability(DeviceInfo.Availability.OFFLINE));
                        }
                    }
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("ADB devices command failed with exit code: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to discover Android devices", e);
            Thread.currentThread().interrupt();
        }

        return devices;
    }

    private DeviceInfo enrichAndroidDevice(String udid, String details) {
        try {
            String model = extractDetail(details, "model:");
            String product = extractDetail(details, "product:");
            String device = extractDetail(details, "device:");

            String osVersion = executeAdbShell(udid, "getprop ro.build.version.release").orElse("Unknown");
            String apiLevelStr = executeAdbShell(udid, "getprop ro.build.version.sdk").orElse("0");
            int apiLevel = Integer.parseInt(apiLevelStr.trim());

            String manufacturer = executeAdbShell(udid, "getprop ro.product.manufacturer").orElse("Unknown");
            String screenDensity = executeAdbShell(udid, "wm density").orElse("");
            float density = parseDensity(screenDensity);
            String screenResolution = executeAdbShell(udid, "wm size").orElse("");

            String displayName = model != null ? model : (device != null ? device : udid);

            return DeviceInfo.androidDevice(udid, displayName, osVersion, model != null ? model : "Unknown")
                    .withDetails(manufacturer.trim(), screenResolution.trim(), density, apiLevel);
        } catch (Exception e) {
            log.warn("Failed to enrich device info for {}", udid, e);
            return DeviceInfo.androidDevice(udid, udid, "Unknown", "Unknown");
        }
    }

    private Optional<String> executeAdbShell(String udid, String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "-s", udid, "shell", command);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                String result = output.toString().trim();
                return result.isEmpty() ? Optional.empty() : Optional.of(result);
            }
        } catch (IOException | InterruptedException e) {
            log.debug("ADB shell command failed: {} {}", udid, command, e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private String extractDetail(String details, String prefix) {
        if (details == null) return null;
        int idx = details.indexOf(prefix);
        if (idx == -1) return null;
        int start = idx + prefix.length();
        int end = details.indexOf(' ', start);
        if (end == -1) end = details.length();
        return details.substring(start, end).trim();
    }

    private float parseDensity(String densityOutput) {
        try {
            if (densityOutput.contains(":")) {
                String[] parts = densityOutput.split(":");
                return Float.parseFloat(parts[1].trim());
            }
        } catch (Exception ignored) {}
        return 0f;
    }

    public Optional<DeviceInfo> selectAndroidDevice(String preferredUdid) {
        List<DeviceInfo> devices = discoverAndroidDevices();

        if (devices.isEmpty()) {
            log.error("No Android devices available");
            return Optional.empty();
        }

        if (preferredUdid != null && !preferredUdid.isBlank()) {
            return devices.stream()
                    .filter(d -> d.udid().equals(preferredUdid))
                    .findFirst()
                    .or(() -> {
                        log.warn("Preferred device {} not found, using first available", preferredUdid);
                        return devices.stream()
                                .filter(d -> d.availability() == DeviceInfo.Availability.AVAILABLE)
                                .findFirst();
                    });
        }

        return devices.stream()
                .filter(d -> d.availability() == DeviceInfo.Availability.AVAILABLE)
                .findFirst();
    }

    public boolean acquireDevice(DeviceInfo device) {
        if (device.availability() != DeviceInfo.Availability.AVAILABLE) {
            log.error("Cannot acquire device {} - not available (status: {})", device.udid(), device.availability());
            return false;
        }

        boolean acquired = lockManager.acquireLock(device);
        if (acquired) {
            log.info("Successfully acquired device: {} ({})", device.deviceName(), device.udid());
        } else {
            log.error("Failed to acquire device: {} ({})", device.deviceName(), device.udid());
        }
        return acquired;
    }

    public void releaseDevice(DeviceInfo device) {
        lockManager.releaseLock(device);
        log.info("Released device: {} ({})", device.deviceName(), device.udid());
    }

    public DeviceInfo performHealthCheck(DeviceInfo device) {
        if (device.platform() != DeviceInfo.Platform.ANDROID) {
            return device;
        }

        try {
            Optional<String> result = executeAdbShell(device.udid(), "getprop ro.build.version.release");
            if (result.isPresent()) {
                log.debug("Health check passed for device {}", device.udid());
                return device.withHealthCheck(LocalDateTime.now())
                        .withAvailability(DeviceInfo.Availability.AVAILABLE);
            }
        } catch (Exception e) {
            log.warn("Health check failed for device {}", device.udid(), e);
        }
        return device.withAvailability(DeviceInfo.Availability.ERROR);
    }

    public void startAdbServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "start-server");
            Process process = pb.start();
            process.waitFor();
            log.info("ADB server started");
        } catch (IOException | InterruptedException e) {
            log.error("Failed to start ADB server", e);
            Thread.currentThread().interrupt();
        }
    }

    public void killAdbServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "kill-server");
            Process process = pb.start();
            process.waitFor();
            log.info("ADB server stopped");
        } catch (IOException | InterruptedException e) {
            log.error("Failed to stop ADB server", e);
            Thread.currentThread().interrupt();
        }
    }
}
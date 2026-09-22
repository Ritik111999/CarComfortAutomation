package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.device.DeviceManager;
import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.tests.BaseTest;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Read-only physical-device presence probe. Passes with device info when a device
 * is attached; SKIPS (never fails) when no device is present so the safe smoke
 * suite stays green on machines without hardware attached.
 */
public class DevicePresenceProbeTest extends BaseTest {

    @Test(groups = {"smoke"})
    public void testProbePhysicalDevicePresence() {
        DeviceLockManager locks = new DeviceLockManager();
        DeviceManager devices = new DeviceManager(locks);
        List<DeviceInfo> found = devices.discoverAndroidDevices();
        testLogger.businessCheckpoint("DEVICE_PROBE", "Attached Android devices: " + found.size());
        if (found.isEmpty()) {
            throw new SkipException("No physical Android device attached - live-app discovery pending");
        }
        DeviceInfo first = found.get(0);
        testLogger.businessCheckpoint("DEVICE_FOUND",
                "Model=" + first.model() + " OS=" + first.osVersion() + " availability=" + first.availability());
        assertBusinessRule(first.udid() != null && !first.udid().isBlank(), "Device UDID present");
        finalizeAssertions();
    }
}

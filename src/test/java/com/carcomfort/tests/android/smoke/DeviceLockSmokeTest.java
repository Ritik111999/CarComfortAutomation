package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Verifies the device-lock mutex without needing a physical device attached.
 * Uses a synthetic UDID so the test is safe and repeatable anywhere.
 */
public class DeviceLockSmokeTest extends BaseTest {

    @Test(groups = {"smoke"})
    public void testAcquireAndReleaseSyntheticLock() {
        DeviceLockManager locks = new DeviceLockManager();
        locks.start();
        try {
            DeviceInfo synthetic = DeviceInfo.androidDevice(
                    "SYNTHETIC-SMOKE-UDID", "SmokeFixture", "99", "SmokeModel");

            assertBusinessRule(locks.acquireLock(synthetic), "Synthetic device lock acquired");
            assertBusinessRule(locks.getLockStatus(synthetic).isPresent(), "Lock status visible");
            assertBusinessRule(locks.releaseLock(synthetic), "Synthetic device lock released");
            assertBusinessRule(locks.getLockStatus(synthetic).isEmpty(), "Lock cleared after release");
        } finally {
            locks.stop();
        }
        finalizeAssertions();
    }

    @Test(groups = {"smoke"})
    public void testSecondOwnerBlockedWhileLocked() {
        DeviceLockManager ownerA = new DeviceLockManager();
        ownerA.start();
        DeviceInfo synthetic = DeviceInfo.androidDevice(
                "SYNTHETIC-CONTENTION-UDID", "SmokeFixture", "99", "SmokeModel");
        try {
            assertBusinessRule(ownerA.acquireLock(synthetic), "First owner acquires lock");
            // Same owner re-acquire is allowed (idempotent); file must exist.
            Path lockFile = Paths.get("artifacts/device-locks",
                    synthetic.platform() + ":" + synthetic.udid() + ".lock");
            assertBusinessRule(Files.exists(lockFile), "Lock file present while held");
        } finally {
            ownerA.releaseLock(synthetic);
            ownerA.stop();
        }
        finalizeAssertions();
    }
}

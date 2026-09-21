package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.tests.BaseTest;
import org.testng.annotations.Test;

/**
 * Simple smoke test to verify framework compilation and basic execution.
 */
public class FrameworkSmokeTest extends BaseTest {

    @Test(groups = {"smoke"})
    public void testFrameworkCompiles() {
        // This test verifies the framework compiles and basic TestNG setup works
        // Actual device interaction tests require a physical device
        testLogger.businessCheckpoint("FRAMEWORK_VERIFY", "Framework compilation verified");
        assertBusinessRule(true, "Framework structure is valid");
        finalizeAssertions();
    }

    @Test(groups = {"smoke"})
    public void testConfigLoads() {
        // Verify configuration system works
        String executionId = FrameworkConfig.getExecutionId();
        testLogger.businessCheckpoint("CONFIG_VERIFY", "Config loaded: " + executionId);
        assertBusinessRule(executionId != null && !executionId.isBlank(), "Execution ID generated");
        finalizeAssertions();
    }
}
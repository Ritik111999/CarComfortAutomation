package com.carcomfort.core.reporting;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.logging.TestLogger;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reporting abstraction - implementation uses ExtentReports.
 */
public final class ReportEngine {
    private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final ExtentReports extent;
    private final Path reportDir;
    private final String executionId;
    private final Map<String, ExtentTest> testMap = new ConcurrentHashMap<>();
    private final EvidenceCollector evidenceCollector;
    private final TestLogger testLogger;

    public ReportEngine(EvidenceCollector evidenceCollector, TestLogger testLogger) {
        this.evidenceCollector = evidenceCollector;
        this.testLogger = testLogger;
        this.executionId = FrameworkConfig.getExecutionId();
        this.reportDir = resolvePath(FrameworkConfig.getString("carcomfort.reporting.extent.reportDir"));

        try {
            Files.createDirectories(reportDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create report directory", e);
        }

        String reportName = FrameworkConfig.getString("carcomfort.reporting.extent.reportName");
        String documentTitle = FrameworkConfig.getString("carcomfort.reporting.extent.documentTitle");
        String theme = FrameworkConfig.getString("carcomfort.reporting.extent.theme");
        boolean timelineEnabled = FrameworkConfig.getBoolean("carcomfort.reporting.extent.timelineEnabled");

        String reportFile = reportDir.resolve("ExtentReport_" + executionId + "_" + FILE_TIMESTAMP.format(LocalDateTime.now()) + ".html").toString();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportFile);
        spark.config().setReportName(reportName);
        spark.config().setDocumentTitle(documentTitle);
        spark.config().setTheme(Theme.valueOf(theme));
        spark.config().setTimelineEnabled(timelineEnabled);

        this.extent = new ExtentReports();
        this.extent.attachReporter(spark);
        this.extent.setSystemInfo("Execution ID", executionId);
        this.extent.setSystemInfo("Platform", "Android");
        this.extent.setSystemInfo("Framework", "Car Comfort Automation");
        this.extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        this.extent.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));

        log.info("Report engine initialized: {}", reportFile);
    }

    private Path resolvePath(String path) {
        if (path == null) return Paths.get("");
        String resolved = VAR_PATTERN.matcher(path).replaceAll(match -> {
            String var = match.group(1);
            if (var.startsWith("?")) {
                var = var.substring(1);
            }
            if (var.contains(":-")) {
                String[] parts = var.split(":-", 2);
                String value = System.getProperty(parts[0]);
                if (value == null) value = System.getenv(parts[0]);
                return value != null ? value : parts[1];
            } else {
                String value = System.getProperty(var);
                if (value == null) value = System.getenv(var);
                return value != null ? value : match.group(0);
            }
        });
        return Paths.get(resolved);
    }

    public void setDeviceInfo(DeviceInfo device) {
        extent.setSystemInfo("Device Name", device.deviceName());
        extent.setSystemInfo("Device UDID", maskUdid(device.udid()));
        extent.setSystemInfo("OS Version", device.osVersion());
        extent.setSystemInfo("Model", device.model());
        extent.setSystemInfo("Manufacturer", device.manufacturer());
        extent.setSystemInfo("Screen Resolution", device.screenResolution());
        extent.setSystemInfo("API Level", String.valueOf(device.apiLevel()));
    }

    public void setAppInfo(String appPackage, String appVersion) {
        extent.setSystemInfo("App Package", appPackage);
        extent.setSystemInfo("App Version", appVersion);
    }

    public ExtentTest createTest(String testName, String description) {
        ExtentTest test = extent.createTest(testName, description);
        testMap.put(testName, test);
        return test;
    }

    public ExtentTest getTest(String testName) {
        return testMap.get(testName);
    }

    public void logPass(String testName, String message) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.log(Status.PASS, message);
        }
    }

    public void logFail(String testName, String message, Throwable throwable) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.log(Status.FAIL, message);
            if (throwable != null) {
                test.log(Status.FAIL, throwable);
            }
        }
    }

    public void logSkip(String testName, String message) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.log(Status.SKIP, message);
        }
    }

    public void logWarning(String testName, String message) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.log(Status.WARNING, message);
        }
    }

    public void addScreenshot(String testName, String screenshotPath, String description) {
        ExtentTest test = testMap.get(testName);
        if (test != null && screenshotPath != null) {
            try {
                test.addScreenCaptureFromPath(screenshotPath, description);
            } catch (Exception e) {
                log.warn("Failed to add screenshot to report", e);
            }
        }
    }

    public void addScreenCaptureFromBase64(String testName, String base64, String description) {
        ExtentTest test = testMap.get(testName);
        if (test != null && base64 != null) {
            test.addScreenCaptureFromBase64String(base64, description);
        }
    }

    public void assignCategory(String testName, String... categories) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.assignCategory(categories);
        }
    }

    public void assignAuthor(String testName, String author) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.assignAuthor(author);
        }
    }

    public void assignDevice(String testName, String device) {
        ExtentTest test = testMap.get(testName);
        if (test != null) {
            test.assignDevice(device);
        }
    }

    public void flush() {
        extent.flush();
        log.info("Report flushed to: {}", reportDir);
    }

    public Path getReportPath() {
        return reportDir;
    }

    public String getExecutionId() {
        return executionId;
    }

    private String maskUdid(String udid) {
        if (udid == null || udid.length() <= 8) return udid;
        return udid.substring(0, 4) + "****" + udid.substring(udid.length() - 4);
    }
}
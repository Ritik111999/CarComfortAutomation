package com.carcomfort.core.reporting;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.device.DeviceInfo;
import com.carcomfort.core.evidence.EvidenceCollector;
import com.carcomfort.core.evidence.EvidenceCollector.EvidenceItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF QA execution report generator.
 */
public final class PdfReportGenerator {
    private static final Logger log = LoggerFactory.getLogger(PdfReportGenerator.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final Path reportDir;
    private final String executionId;
    private final EvidenceCollector evidenceCollector;
    private final Map<String, TestResult> testResults = new ConcurrentHashMap<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DeviceInfo deviceInfo;
    private String appVersion;
    private String appPackage;

    public PdfReportGenerator(EvidenceCollector evidenceCollector) {
        this.evidenceCollector = evidenceCollector;
        this.executionId = FrameworkConfig.getExecutionId();
        this.reportDir = resolvePath(FrameworkConfig.getString("carcomfort.reporting.pdf.reportDir"));

        try {
            Files.createDirectories(reportDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PDF report directory", e);
        }
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

    public void startExecution() {
        this.startTime = LocalDateTime.now();
    }

    public void endExecution() {
        this.endTime = LocalDateTime.now();
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setAppInfo(String appPackage, String appVersion) {
        this.appPackage = appPackage;
        this.appVersion = appVersion;
    }

    public void recordTestResult(String testName, String testClass, String status, long durationMs,
                                 String errorMessage, List<EvidenceItem> evidence, List<String> categories) {
        testResults.put(testName, new TestResult(testName, testClass, status, durationMs, errorMessage, evidence, categories));
    }

    public Path generateReport() {
        if (startTime == null || endTime == null) {
            throw new IllegalStateException("Execution start/end time not recorded");
        }

        String fileName = "CarComfort_QA_Report_" + executionId + "_" + FILE_TIMESTAMP.format(LocalDateTime.now()) + ".pdf";
        Path reportPath = reportDir.resolve(fileName);

        try (PDDocument document = new PDDocument()) {
            addCoverPage(document);
            addExecutionSummary(document);
            addDeviceInfo(document);
            addTestResults(document);
            addFailureDetails(document);
            addEvidenceAppendix(document);

            document.save(reportPath.toFile());
            log.info("PDF report generated: {}", reportPath);
            return reportPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addCoverPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 28);
            cs.beginText();
            cs.newLineAtOffset(50, 700);
            cs.showText("Car Comfort");
            cs.endText();

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            cs.beginText();
            cs.newLineAtOffset(50, 650);
            cs.showText("QA Automation Execution Report");
            cs.endText();

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            cs.beginText();
            cs.newLineAtOffset(50, 600);
            cs.showText("Execution ID: " + executionId);
            cs.newLineAtOffset(0, -20);
            cs.showText("Date: " + TIMESTAMP.format(startTime));
            cs.newLineAtOffset(0, -20);
            cs.showText("Duration: " + formatDuration(java.time.Duration.between(startTime, endTime)));
            cs.newLineAtOffset(0, -20);
            cs.showText("Platform: Android");
            cs.endText();
        }
    }

    private void addExecutionSummary(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        long total = testResults.size();
        long passed = testResults.values().stream().filter(r -> "PASSED".equals(r.status())).count();
        long failed = testResults.values().stream().filter(r -> "FAILED".equals(r.status())).count();
        long skipped = testResults.values().stream().filter(r -> "SKIPPED".equals(r.status())).count();
        long gated = testResults.values().stream().filter(r -> "GATED".equals(r.status())).count();

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            cs.beginText();
            cs.newLineAtOffset(50, 750);
            cs.showText("Execution Summary");
            cs.endText();

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            cs.beginText();
            cs.newLineAtOffset(50, 710);
            cs.showText("Total Tests: " + total);
            cs.newLineAtOffset(0, -20);
            cs.showText("Passed: " + passed);
            cs.newLineAtOffset(0, -20);
            cs.showText("Failed: " + failed);
            cs.newLineAtOffset(0, -20);
            cs.showText("Skipped: " + skipped);
            cs.newLineAtOffset(0, -20);
            cs.showText("Gated/Excluded: " + gated);
            cs.newLineAtOffset(0, -20);
            cs.showText("Pass Rate: " + (total > 0 ? String.format("%.1f%%", (passed * 100.0 / total)) : "N/A"));
            cs.endText();
        }
    }

    private void addDeviceInfo(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            cs.beginText();
            cs.newLineAtOffset(50, 750);
            cs.showText("Device Information");
            cs.endText();

            if (deviceInfo != null) {
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.beginText();
                cs.newLineAtOffset(50, 710);
                cs.showText("Device Name: " + deviceInfo.deviceName());
                cs.newLineAtOffset(0, -18);
                cs.showText("UDID (masked): " + maskUdid(deviceInfo.udid()));
                cs.newLineAtOffset(0, -18);
                cs.showText("OS Version: " + deviceInfo.osVersion());
                cs.newLineAtOffset(0, -18);
                cs.showText("Model: " + deviceInfo.model());
                cs.newLineAtOffset(0, -18);
                cs.showText("Manufacturer: " + deviceInfo.manufacturer());
                cs.newLineAtOffset(0, -18);
                cs.showText("Screen Resolution: " + deviceInfo.screenResolution());
                cs.newLineAtOffset(0, -18);
                cs.showText("Density: " + deviceInfo.density());
                cs.newLineAtOffset(0, -18);
                cs.showText("API Level: " + deviceInfo.apiLevel());
                cs.newLineAtOffset(0, -18);
                cs.showText("Connection: " + deviceInfo.connectionType());
                cs.endText();
            }

            if (appPackage != null) {
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.beginText();
                cs.newLineAtOffset(50, 520);
                cs.showText("Application Package: " + appPackage);
                cs.newLineAtOffset(0, -18);
                cs.showText("Application Version: " + (appVersion != null ? appVersion : "Unknown"));
                cs.endText();
            }
        }
    }

    private void addTestResults(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(document, page);
        try {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            cs.beginText();
            cs.newLineAtOffset(50, 750);
            cs.showText("Test Results");
            cs.endText();

            float y = 710;
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            cs.beginText();
            cs.newLineAtOffset(50, y);
            cs.showText("Test Name");
            cs.newLineAtOffset(250, 0);
            cs.showText("Class");
            cs.newLineAtOffset(150, 0);
            cs.showText("Status");
            cs.newLineAtOffset(70, 0);
            cs.showText("Duration");
            cs.endText();

            y -= 20;
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);

            for (TestResult result : testResults.values()) {
                if (y < 50) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    y = 750;
                    cs = new PDPageContentStream(document, page);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                }

                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(truncate(result.testName(), 35));
                cs.newLineAtOffset(250, 0);
                cs.showText(truncate(result.testClass(), 20));
                cs.newLineAtOffset(150, 0);
                cs.showText(result.status());
                cs.newLineAtOffset(70, 0);
                cs.showText(result.durationMs() + "ms");
                cs.endText();

                y -= 16;
            }
        } finally {
            cs.close();
        }
    }

    private void addFailureDetails(PDDocument document) throws IOException {
        List<TestResult> failures = testResults.values().stream()
                .filter(r -> "FAILED".equals(r.status()))
                .toList();

        if (failures.isEmpty()) return;

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(document, page);
        try {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            cs.beginText();
            cs.newLineAtOffset(50, 750);
            cs.showText("Failure Details");
            cs.endText();

            float y = 710;
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

            for (TestResult result : failures) {
                if (y < 100) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    y = 750;
                    cs = new PDPageContentStream(document, page);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                }

                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText("Test: " + result.testName());
                cs.newLineAtOffset(0, -16);
                cs.showText("Class: " + result.testClass());
                cs.newLineAtOffset(0, -16);
                cs.showText("Error: " + truncate(result.errorMessage() != null ? result.errorMessage() : "No error message", 100));
                cs.endText();

                y -= 60;
            }
        } finally {
            cs.close();
        }
    }

    private void addEvidenceAppendix(PDDocument document) throws IOException {
        boolean includeScreenshots = FrameworkConfig.getBoolean("carcomfort.reporting.pdf.includeScreenshots");
        if (!includeScreenshots) return;

        int maxScreenshots = FrameworkConfig.getInt("carcomfort.reporting.pdf.maxScreenshotsPerTest");

        for (TestResult result : testResults.values()) {
            if (result.evidence() == null || result.evidence().isEmpty()) continue;

            List<EvidenceItem> screenshots = result.evidence().stream()
                    .filter(e -> e.type() == EvidenceItem.Type.SCREENSHOT)
                    .limit(maxScreenshots)
                    .toList();

            if (screenshots.isEmpty()) continue;

            for (EvidenceItem screenshot : screenshots) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    cs.beginText();
                    cs.newLineAtOffset(50, 750);
                    cs.showText("Screenshot: " + result.testName());
                    cs.endText();

                    PDImageXObject image = PDImageXObject.createFromFile(screenshot.path().toString(), document);
                    float scale = Math.min(500f / image.getWidth(), 600f / image.getHeight());
                    cs.drawImage(image, 50, 100, image.getWidth() * scale, image.getHeight() * scale);
                }
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        String sanitized = sanitizeForPdf(text);
        return sanitized.length() > maxLength ? sanitized.substring(0, maxLength - 3) + "..." : sanitized;
    }

    /**
     * PDFBox Helvetica/WinAnsi cannot encode control chars (notably newlines from
     * accessibility descs like "My Vehicles\n3") or non-Latin glyphs. Flatten them
     * so report generation never fails on real product text (verified 2026-09-22).
     */
    private String sanitizeForPdf(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                sb.append(" / ");
            } else if (c < 0x20 || c > 0xFF) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String maskUdid(String udid) {
        if (udid == null || udid.length() <= 8) return udid;
        return udid.substring(0, 4) + "****" + udid.substring(udid.length() - 4);
    }

    private String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private record TestResult(
            String testName,
            String testClass,
            String status,
            long durationMs,
            String errorMessage,
            List<EvidenceItem> evidence,
            List<String> categories
    ) {}
}
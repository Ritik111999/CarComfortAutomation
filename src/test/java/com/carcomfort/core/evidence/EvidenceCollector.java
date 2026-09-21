package com.carcomfort.core.evidence;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.driver.DriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evidence collection: screenshots, page source, videos.
 */
public final class EvidenceCollector {
    private static final Logger log = LoggerFactory.getLogger(EvidenceCollector.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final Path screenshotDir;
    private final Path pageSourceDir;
    private final Path videoDir;
    private final boolean compressScreenshots;
    private final List<EvidenceItem> currentTestEvidence = new ArrayList<>();

    public EvidenceCollector() {
        this.screenshotDir = resolvePath(FrameworkConfig.getString("carcomfort.reporting.evidence.screenshotDir"));
        this.pageSourceDir = resolvePath(FrameworkConfig.getString("carcomfort.reporting.evidence.pageSourceDir"));
        this.videoDir = resolvePath(FrameworkConfig.getString("carcomfort.reporting.evidence.videoDir"));
        this.compressScreenshots = FrameworkConfig.getBoolean("carcomfort.reporting.evidence.compressScreenshots");

        createDirectories();
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

    private void createDirectories() {
        try {
            Files.createDirectories(screenshotDir);
            Files.createDirectories(pageSourceDir);
            Files.createDirectories(videoDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create evidence directories", e);
        }
    }

    public EvidenceItem captureScreenshot(String name) {
        return captureScreenshot(name, null);
    }

    public EvidenceItem captureScreenshot(String name, DriverManager<?> driverManager) {
        WebDriver driver = driverManager != null && driverManager.isSessionActive() ? driverManager.getDriver() : null;
        if (driver == null) {
            log.warn("Cannot capture screenshot: no active driver session");
            return null;
        }

        String fileName = TIMESTAMP.format(LocalDateTime.now()) + "_" + sanitize(name) + ".png";
        Path filePath = screenshotDir.resolve(fileName);

        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, filePath.toFile());

            EvidenceItem item = new EvidenceItem(EvidenceItem.Type.SCREENSHOT, filePath, name, LocalDateTime.now());
            currentTestEvidence.add(item);
            log.debug("Screenshot captured: {}", filePath);
            return item;
        } catch (IOException e) {
            log.error("Failed to capture screenshot", e);
            return null;
        }
    }

    public EvidenceItem capturePageSource(String name) {
        return capturePageSource(name, null);
    }

    public EvidenceItem capturePageSource(String name, DriverManager<?> driverManager) {
        WebDriver driver = driverManager != null && driverManager.isSessionActive() ? driverManager.getDriver() : null;
        if (driver == null) {
            log.warn("Cannot capture page source: no active driver session");
            return null;
        }

        String fileName = TIMESTAMP.format(LocalDateTime.now()) + "_" + sanitize(name) + ".xml";
        Path filePath = pageSourceDir.resolve(fileName);

        try {
            String pageSource = driver.getPageSource();
            Files.writeString(filePath, pageSource);

            EvidenceItem item = new EvidenceItem(EvidenceItem.Type.PAGE_SOURCE, filePath, name, LocalDateTime.now());
            currentTestEvidence.add(item);
            log.debug("Page source captured: {}", filePath);
            return item;
        } catch (IOException e) {
            log.error("Failed to capture page source", e);
            return null;
        }
    }

    public EvidenceItem captureScreenshotAndPageSource(String name, DriverManager<?> driverManager) {
        EvidenceItem screenshot = captureScreenshot(name, driverManager);
        EvidenceItem pageSource = capturePageSource(name, driverManager);
        return screenshot != null ? screenshot : pageSource;
    }

    public List<EvidenceItem> getCurrentTestEvidence() {
        return new ArrayList<>(currentTestEvidence);
    }

    public void clearCurrentTestEvidence() {
        currentTestEvidence.clear();
    }

    public Optional<Path> createEvidenceArchive(String testName) {
        if (currentTestEvidence.isEmpty()) return Optional.empty();

        String archiveName = TIMESTAMP.format(LocalDateTime.now()) + "_" + sanitize(testName) + "_evidence.zip";
        Path archivePath = screenshotDir.getParent().resolve("archives").resolve(archiveName);

        try {
            Files.createDirectories(archivePath.getParent());
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archivePath))) {
                for (EvidenceItem item : currentTestEvidence) {
                    ZipEntry entry = new ZipEntry(item.type().name().toLowerCase() + "/" + item.path().getFileName());
                    zos.putNextEntry(entry);
                    Files.copy(item.path(), zos);
                    zos.closeEntry();
                }
            }
            return Optional.of(archivePath);
        } catch (IOException e) {
            log.error("Failed to create evidence archive", e);
            return Optional.empty();
        }
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record EvidenceItem(
            Type type,
            Path path,
            String name,
            LocalDateTime timestamp
    ) {
        public enum Type {
            SCREENSHOT,
            PAGE_SOURCE,
            VIDEO,
            LOG
        }
    }
}
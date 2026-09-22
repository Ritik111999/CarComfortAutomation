package com.carcomfort.core.driver;

import com.carcomfort.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Appium server lifecycle management.
 */
public final class AppiumServerManager {
    private static final Logger log = LoggerFactory.getLogger(AppiumServerManager.class);
    private static final Pattern APPIUM_READY_PATTERN = Pattern.compile("Appium REST http interface listener started on (\\S+)");

    private final String host;
    private final int port;
    private final String path;
    private final long startupTimeoutSeconds;
    private Process appiumProcess;
    private volatile boolean running = false;
    private URL serverUrl;

    public AppiumServerManager() {
        this.host = FrameworkConfig.getString("carcomfort.appium.server.host");
        this.port = FrameworkConfig.getInt("carcomfort.appium.server.port");
        this.path = FrameworkConfig.getString("carcomfort.appium.server.path");
        this.startupTimeoutSeconds = FrameworkConfig.getLong("carcomfort.appium.server.startupTimeoutSeconds");
        this.serverUrl = buildServerUrl();
    }

    public void start() {
        if (running && isServerResponsive()) {
            log.info("Appium server already running at {}", serverUrl);
            return;
        }

        if (FrameworkConfig.getBoolean("carcomfort.appium.server.autoStart")) {
            startAppiumProcess();
            waitForServerReady();
        } else {
            waitForExternalServer();
        }

        running = true;
        log.info("Appium server ready at {}", serverUrl);
    }

    private void startAppiumProcess() {
        try {
            String appiumCmd = findAppiumCommand();
            java.util.List<String> cmd = new java.util.ArrayList<>(
                    java.util.List.of(appiumCmd, "server", "-p", String.valueOf(port)));
            if (path != null && !path.isBlank() && !path.equals("/")) {
                cmd.add("--base-path");
                cmd.add(path);
            }
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            appiumProcess = pb.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(appiumProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("[Appium] {}", line);
                        Matcher matcher = APPIUM_READY_PATTERN.matcher(line);
                        if (matcher.find()) {
                            log.info("Appium server started on {}", matcher.group(1));
                        }
                    }
                } catch (IOException e) {
                    log.debug("Appium output stream closed", e);
                }
            }, "Appium-Output-Reader");
            outputThread.setDaemon(true);
            outputThread.start();

        } catch (IOException e) {
            throw new RuntimeException("Failed to start Appium server", e);
        }
    }

    private String findAppiumCommand() {
        String[] candidates = {"appium", "npx appium", "/opt/homebrew/bin/appium", "/usr/local/bin/appium"};
        for (String cmd : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
                pb.command().add("--version");
                Process p = pb.start();
                if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) {
                    return cmd.split(" ")[0];
                }
            } catch (Exception ignored) {}
        }
        return "appium";
    }

    private void waitForServerReady() {
        try {
            org.awaitility.Awaitility.await()
                    .atMost(Duration.ofSeconds(startupTimeoutSeconds))
                    .pollInterval(Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(this::isServerResponsive);
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            throw new RuntimeException("Appium server did not start within " + startupTimeoutSeconds + " seconds", e);
        }
    }

    private void waitForExternalServer() {
        try {
            org.awaitility.Awaitility.await()
                    .atMost(Duration.ofSeconds(startupTimeoutSeconds))
                    .pollInterval(Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(this::isServerResponsive);
        } catch (org.awaitility.core.ConditionTimeoutException e) {
            throw new RuntimeException("External Appium server not responsive at " + serverUrl + " within " + startupTimeoutSeconds + " seconds", e);
        }
    }

    private boolean isServerResponsive() {
        try {
            URL statusUrl = new URL(serverUrl + "/status");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) statusUrl.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public void stop() {
        if (appiumProcess != null && appiumProcess.isAlive()) {
            log.info("Stopping Appium server...");
            appiumProcess.destroy();
            try {
                if (!appiumProcess.waitFor(10, TimeUnit.SECONDS)) {
                    appiumProcess.destroyForcibly();
                    appiumProcess.waitFor(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appiumProcess.destroyForcibly();
            }
            appiumProcess = null;
        }
        running = false;
        log.info("Appium server stopped");
    }

    public boolean isRunning() {
        return running && isServerResponsive();
    }

    public URL getServerUrl() {
        return serverUrl;
    }

    private URL buildServerUrl() {
        try {
            String base = (path == null || path.isBlank()) ? "" : path;
            return new URL("http", host, port, base);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Appium server URL configuration", e);
        }
    }
}
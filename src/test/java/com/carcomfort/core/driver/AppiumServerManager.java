package com.carcomfort.core.driver;

import com.carcomfort.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
            ProcessBuilder pb = new ProcessBuilder(appiumCmd, "-p", String.valueOf(port), "-b", path);
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
        long startTime = System.currentTimeMillis();
        long timeoutMs = startupTimeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isServerResponsive()) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Appium server", e);
            }
        }
        throw new RuntimeException("Appium server did not start within " + startupTimeoutSeconds + " seconds");
    }

    private void waitForExternalServer() {
        long startTime = System.currentTimeMillis();
        long timeoutMs = startupTimeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isServerResponsive()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for external Appium server", e);
            }
        }
        throw new RuntimeException("External Appium server not responsive at " + serverUrl + " within " + startupTimeoutSeconds + " seconds");
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
            return new URL("http", host, port, path);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Appium server URL configuration", e);
        }
    }
}
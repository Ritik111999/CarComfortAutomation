package com.carcomfort.core.driver;

import com.carcomfort.core.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * WebDriver lifecycle management for PWA automation.
 */
public final class WebDriverManager implements DriverManager<WebDriver> {
    private static final Logger log = LoggerFactory.getLogger(WebDriverManager.class);

    private final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private final String role;

    public WebDriverManager(String role) {
        this.role = role;
    }

    @Override
    public WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized for " + role + ". Call initialize() first.");
        }
        return driver;
    }

    @Override
    public void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                log.info("Quitting WebDriver for {}", role);
                driver.quit();
            } catch (Exception e) {
                log.warn("Error quitting WebDriver for {}", role, e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    @Override
    public boolean isSessionActive() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) return false;
        try {
            driver.getWindowHandle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WebDriver initialize() {
        if (isSessionActive()) {
            return getDriver();
        }

        log.info("Initializing WebDriver for {}", role);

        WebDriver driver;
        boolean gridEnabled = FrameworkConfig.getBoolean("carcomfort.web.selenium.gridEnabled");
        String remoteUrl = FrameworkConfig.getString("carcomfort.web.selenium.remoteUrl");

        if (gridEnabled && remoteUrl != null && !remoteUrl.isBlank()) {
            driver = createRemoteDriver(remoteUrl);
        } else {
            driver = createLocalDriver();
        }

        configureDriver(driver);
        driverThreadLocal.set(driver);
        log.info("WebDriver initialized for {}: {}", role, driver.getClass().getSimpleName());
        return driver;
    }

    private WebDriver createLocalDriver() {
        String browser = getBrowserConfig().getString("browser");
        return switch (browser.toLowerCase()) {
            case "chrome" -> createChromeDriver();
            case "firefox" -> createFirefoxDriver();
            case "edge" -> createEdgeDriver();
            case "safari" -> createSafariDriver();
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
    }

    private WebDriver createRemoteDriver(String remoteUrl) {
        try {
            ChromeOptions options = new ChromeOptions();
            configureChromeOptions(options);
            return new RemoteWebDriver(new URL(remoteUrl), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Selenium Grid URL: " + remoteUrl, e);
        }
    }

    private WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        configureChromeOptions(options);
        return new ChromeDriver(options);
    }

    private WebDriver createFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        configureFirefoxOptions(options);
        return new FirefoxDriver(options);
    }

    private WebDriver createEdgeDriver() {
        EdgeOptions options = new EdgeOptions();
        configureEdgeOptions(options);
        return new EdgeDriver(options);
    }

    private WebDriver createSafariDriver() {
        SafariOptions options = new SafariOptions();
        return new SafariDriver(options);
    }

    private void configureChromeOptions(ChromeOptions options) {
        var chromeConfig = com.carcomfort.core.config.FrameworkConfig.getOptionalConfig("carcomfort.web.selenium.capabilities.chrome");
        if (chromeConfig.isPresent()) {
            com.typesafe.config.Config tc = chromeConfig.get();
            for (String arg : tc.getStringList("args")) {
                options.addArguments(arg);
            }
            if (tc.hasPath("prefs")) {
                options.setExperimentalOption("prefs", tc.getConfig("prefs").root().unwrapped());
            }
        }
        if (getBrowserConfig().getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
    }

    private void configureFirefoxOptions(FirefoxOptions options) {
        var firefoxConfig = com.carcomfort.core.config.FrameworkConfig.getOptionalConfig("carcomfort.web.selenium.capabilities.firefox");
        if (firefoxConfig.isPresent()) {
            com.typesafe.config.Config tc = firefoxConfig.get();
            for (String arg : tc.getStringList("args")) {
                options.addArguments(arg);
            }
        }
        if (getBrowserConfig().getBoolean("headless")) {
            options.addArguments("--headless");
        }
    }

    private void configureEdgeOptions(EdgeOptions options) {
        if (getBrowserConfig().getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
    }

    private void configureDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(
                getBrowserConfig().getInt("windowWidth"),
                getBrowserConfig().getInt("windowHeight")
        ));
    }

    private com.typesafe.config.Config getBrowserConfig() {
        String configPath = "carcomfort.web." + role;
        return com.carcomfort.core.config.FrameworkConfig.getConfig(configPath);
    }

    public void navigateToBaseUrl() {
        WebDriver driver = getDriver();
        String baseUrl = getBrowserConfig().getString("baseUrl");
        if (baseUrl != null && !baseUrl.isBlank()) {
            driver.get(baseUrl);
            log.info("Navigated to {} base URL: {}", role, baseUrl);
        }
    }

    public void navigateTo(String url) {
        getDriver().get(url);
        log.debug("Navigated to: {}", url);
    }
}
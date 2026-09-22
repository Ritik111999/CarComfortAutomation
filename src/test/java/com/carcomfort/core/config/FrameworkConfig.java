package com.carcomfort.core.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Centralized configuration management using Typesafe Config (HOCON).
 * Supports layering: reference.conf -> env-specific.conf -> system properties -> environment variables.
 */
public final class FrameworkConfig {
    private static final Logger log = LoggerFactory.getLogger(FrameworkConfig.class);
    private static final ThreadLocal<String> executionId = new ThreadLocal<>();
    private static volatile Config INSTANCE;

    private FrameworkConfig() {}

    private static Config getInstance() {
        Config instance = INSTANCE;
        if (instance == null) {
            synchronized (FrameworkConfig.class) {
                instance = INSTANCE;
                if (instance == null) {
                    instance = loadConfiguration();
                    INSTANCE = instance;
                }
            }
        }
        return instance;
    }

    public static Config get() {
        return getInstance();
    }

    public static String getExecutionId() {
        String id = executionId.get();
        if (id == null) {
            try {
                id = getInstance().hasPath("carcomfort.execution.id")
                        ? getInstance().getString("carcomfort.execution.id") : null;
            } catch (Exception e) {
                id = null;
            }
            if (id == null || id.isBlank() || id.contains("${")) {
                id = "exec-local-" + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            }
            executionId.set(id);
        }
        return id;
    }

    public static void setExecutionId(String id) {
        executionId.set(id);
        System.setProperty("execution.id", id);
    }

    public static String getString(String path) {
        return resolvePlaceholders(getInstance().getString(path));
    }

    public static String getString(String path, String defaultValue) {
        return getInstance().hasPath(path) ? resolvePlaceholders(getInstance().getString(path)) : defaultValue;
    }

    public static int getInt(String path) {
        return getInstance().getInt(path);
    }

    public static int getInt(String path, int defaultValue) {
        return getInstance().hasPath(path) ? getInstance().getInt(path) : defaultValue;
    }

    public static long getLong(String path) {
        return getInstance().getLong(path);
    }

    public static long getLong(String path, long defaultValue) {
        return getInstance().hasPath(path) ? getInstance().getLong(path) : defaultValue;
    }

    public static double getDouble(String path) {
        return getInstance().getDouble(path);
    }

    public static double getDouble(String path, double defaultValue) {
        return getInstance().hasPath(path) ? getInstance().getDouble(path) : defaultValue;
    }

    public static boolean getBoolean(String path) {
        return getInstance().getBoolean(path);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        return getInstance().hasPath(path) ? getInstance().getBoolean(path) : defaultValue;
    }

    public static java.util.List<String> getStringList(String path) {
        return getInstance().getStringList(path);
    }

    public static Config getConfig(String path) {
        return getInstance().getConfig(path);
    }

    public static boolean hasPath(String path) {
        return getInstance().hasPath(path);
    }

    public static Optional<String> getOptionalString(String path) {
        return getInstance().hasPath(path)
                ? Optional.of(resolvePlaceholders(getInstance().getString(path))) : Optional.empty();
    }

    public static Optional<Config> getOptionalConfig(String path) {
        return getInstance().hasPath(path) ? Optional.of(getInstance().getConfig(path)) : Optional.empty();
    }

    private static Config loadConfiguration() {
        try {
            String configFile = System.getProperty("config.file");
            Config baseConfig;
            try {
                baseConfig = ConfigFactory.parseResources("config/reference.conf");
            } catch (Exception e) {
                log.warn("Failed to load reference.conf from classpath, using empty config: {}", e.getMessage());
                baseConfig = ConfigFactory.empty();
            }

            Config envConfig = ConfigFactory.empty();
            if (configFile != null && !configFile.isBlank()) {
                File file = new File(configFile);
                if (file.exists()) {
                    log.info("Loading configuration from: {}", file.getAbsolutePath());
                    envConfig = ConfigFactory.parseFile(file);
                } else {
                    log.warn("Config file not found: {}", file.getAbsolutePath());
                }
            } else {
                String env = System.getProperty("env", "local");
                String resourcePath = "config/" + env + ".conf";
                try {
                    envConfig = ConfigFactory.parseResources(resourcePath);
                    log.info("Loading environment configuration from classpath: {}", resourcePath);
                } catch (Exception e) {
                    log.debug("Environment config not found: {}", resourcePath);
                }
            }

            Config systemProps = ConfigFactory.systemProperties();
            Config envVars = ConfigFactory.systemEnvironment();

            Config merged = envConfig
                    .withFallback(systemProps)
                    .withFallback(envVars)
                    .withFallback(baseConfig)
                    .resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true));

            log.info("Configuration loaded. Execution ID: {}", merged.getString("carcomfort.execution.id"));
            return merged;
        } catch (Exception e) {
            log.error("Failed to load configuration, using minimal fallback", e);
            return ConfigFactory.parseString("carcomfort.execution.id = \"fallback-\" + System.currentTimeMillis()");
        }
    }

    public static void reload() {
        // Not supported for immutable config - create new instance if needed
        throw new UnsupportedOperationException("Configuration is immutable after initialization");
    }

    private static final java.util.regex.Pattern PLACEHOLDER =
            java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Resolves ${NAME}, ${NAME:-default}, and ${?NAME} against JVM system properties
     * then environment variables. Unresolvable placeholders resolve to "".
     */
    public static String resolvePlaceholders(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        java.util.regex.Matcher m = PLACEHOLDER.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            String replacement = "";
            if (expr.startsWith("?")) {
                replacement = lookup(expr.substring(1));
            } else if (expr.contains(":-")) {
                String[] parts = expr.split(":-", 2);
                replacement = lookup(parts[0]);
                if (replacement.isEmpty()) {
                    replacement = parts[1];
                }
            } else {
                replacement = lookup(expr);
            }
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String lookup(String name) {
        String v = System.getProperty(name);
        if (v == null) {
            v = System.getenv(name);
        }
        return v != null ? v : "";
    }
}
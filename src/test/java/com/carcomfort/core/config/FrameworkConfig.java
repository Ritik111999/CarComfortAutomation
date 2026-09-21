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
            id = getInstance().getString("carcomfort.execution.id");
            executionId.set(id);
        }
        return id;
    }

    public static void setExecutionId(String id) {
        executionId.set(id);
        System.setProperty("execution.id", id);
    }

    public static String getString(String path) {
        return INSTANCE.getString(path);
    }

    public static String getString(String path, String defaultValue) {
        return INSTANCE.hasPath(path) ? INSTANCE.getString(path) : defaultValue;
    }

    public static int getInt(String path) {
        return INSTANCE.getInt(path);
    }

    public static int getInt(String path, int defaultValue) {
        return INSTANCE.hasPath(path) ? INSTANCE.getInt(path) : defaultValue;
    }

    public static long getLong(String path) {
        return INSTANCE.getLong(path);
    }

    public static long getLong(String path, long defaultValue) {
        return INSTANCE.hasPath(path) ? INSTANCE.getLong(path) : defaultValue;
    }

    public static double getDouble(String path) {
        return INSTANCE.getDouble(path);
    }

    public static double getDouble(String path, double defaultValue) {
        return INSTANCE.hasPath(path) ? INSTANCE.getDouble(path) : defaultValue;
    }

    public static boolean getBoolean(String path) {
        return INSTANCE.getBoolean(path);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        return INSTANCE.hasPath(path) ? INSTANCE.getBoolean(path) : defaultValue;
    }

    public static java.util.List<String> getStringList(String path) {
        return INSTANCE.getStringList(path);
    }

    public static Config getConfig(String path) {
        return INSTANCE.getConfig(path);
    }

    public static boolean hasPath(String path) {
        return INSTANCE.hasPath(path);
    }

    public static Optional<String> getOptionalString(String path) {
        return INSTANCE.hasPath(path) ? Optional.of(INSTANCE.getString(path)) : Optional.empty();
    }

    public static Optional<Config> getOptionalConfig(String path) {
        return INSTANCE.hasPath(path) ? Optional.of(INSTANCE.getConfig(path)) : Optional.empty();
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
}
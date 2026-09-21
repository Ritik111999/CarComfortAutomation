package com.carcomfort.core.testdata;

import com.carcomfort.core.config.FrameworkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test data management with environment isolation and secret masking.
 */
public final class TestDataManager {
    private static final Logger log = LoggerFactory.getLogger(TestDataManager.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final Path dataDir;
    private final boolean maskSecretsInLogs;
    private final String[] secretPatterns;
    private final Map<String, Object> cachedData = new ConcurrentHashMap<>();

    public TestDataManager() {
        this.dataDir = Paths.get(FrameworkConfig.getString("carcomfort.testData.dataDir"));
        this.maskSecretsInLogs = FrameworkConfig.getBoolean("carcomfort.testData.maskSecretsInLogs");
        this.secretPatterns = FrameworkConfig.getStringList("carcomfort.testData.secretPatterns").toArray(new String[0]);

        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test data directory", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T load(String fileName, Class<T> type) {
        String cacheKey = fileName + ":" + type.getName();
        return (T) cachedData.computeIfAbsent(cacheKey, k -> {
            Path filePath = dataDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                filePath = Paths.get("src/test/resources/testdata").resolve(fileName);
            }

            if (!Files.exists(filePath)) {
                log.warn("Test data file not found: {}", fileName);
                return null;
            }

            try {
                String content = Files.readString(filePath);
                content = resolvePlaceholders(content);
                T data = yamlMapper.readValue(content, type);
                log.debug("Loaded test data: {}", fileName);
                return maskSecrets(data);
            } catch (IOException e) {
                log.error("Failed to load test data: {}", fileName, e);
                return null;
            }
        });
    }

    public <T> T loadOrDefault(String fileName, Class<T> type, T defaultValue) {
        T data = load(fileName, type);
        return data != null ? data : defaultValue;
    }

    public void save(String fileName, Object data) {
        Path filePath = dataDir.resolve(fileName);
        try {
            Files.createDirectories(filePath.getParent());
            String yaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(filePath, yaml);
            cachedData.put(fileName, data);
            log.debug("Saved test data: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save test data: {}", fileName, e);
        }
    }

    public Map<String, Object> loadAsMap(String fileName) {
        return load(fileName, Map.class);
    }

    public String getString(String fileName, String key) {
        Map<String, Object> data = loadAsMap(fileName);
        return data != null ? String.valueOf(data.get(key)) : null;
    }

    private String resolvePlaceholders(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String envValue = System.getenv(key);
            String replacement = envValue != null ? envValue : System.getProperty(key, matcher.group(0));
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private <T> T maskSecrets(T data) {
        if (!maskSecretsInLogs || data == null) return data;

        String json;
        try {
            json = new ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            return data;
        }

        for (String pattern : secretPatterns) {
            json = json.replaceAll("(?i)\"(" + pattern + ")\"\\s*:\\s*\"[^\"]*\"", "\"$1\": \"***MASKED***\"");
            json = json.replaceAll("(?i)\"(" + pattern + ")\"\\s*:\\s*\\d+", "\"$1\": ***MASKED***");
        }

        try {
            return new ObjectMapper().readValue(json, (Class<T>) data.getClass());
        } catch (Exception e) {
            return data;
        }
    }

    public String maskForLogging(String input) {
        if (!maskSecretsInLogs || input == null) return input;
        String result = input;
        for (String pattern : secretPatterns) {
            result = result.replaceAll("(?i)" + pattern + "\\s*[:=]\\s*\\S+", pattern + "=***MASKED***");
        }
        return result;
    }

    public void clearCache() {
        cachedData.clear();
    }
}
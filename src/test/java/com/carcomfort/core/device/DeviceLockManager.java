package com.carcomfort.core.device;

import com.carcomfort.core.config.FrameworkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File-based device locking mechanism to prevent multiple agents/processes
 * from controlling the same physical device simultaneously.
 */
public final class DeviceLockManager {
    private static final Logger log = LoggerFactory.getLogger(DeviceLockManager.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private final Path lockDir;
    private final String ownerId;
    private final long lockTimeoutSeconds;
    private final long heartbeatIntervalSeconds;
    private final long staleLockCleanupSeconds;
    private final Map<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockHeartbeats = new ConcurrentHashMap<>();
    private java.util.concurrent.ScheduledExecutorService heartbeatScheduler;
    private volatile boolean running = false;

    public DeviceLockManager() {
        this.lockDir = resolvePath(FrameworkConfig.getString("carcomfort.deviceLock.lockDir"));
        this.ownerId = resolveVar(FrameworkConfig.getString("carcomfort.deviceLock.ownerId"));
        this.lockTimeoutSeconds = FrameworkConfig.getLong("carcomfort.deviceLock.lockTimeoutSeconds");
        this.heartbeatIntervalSeconds = FrameworkConfig.getLong("carcomfort.deviceLock.heartbeatIntervalSeconds");
        this.staleLockCleanupSeconds = FrameworkConfig.getLong("carcomfort.deviceLock.staleLockCleanupSeconds");

        try {
            Files.createDirectories(lockDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create lock directory: " + lockDir, e);
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

    private String resolveVar(String var) {
        if (var == null) return "";
        String resolved = VAR_PATTERN.matcher(var).replaceAll(match -> {
            String v = match.group(1);
            if (v.startsWith("?")) {
                v = v.substring(1);
            }
            if (v.contains(":-")) {
                String[] parts = v.split(":-", 2);
                String value = System.getProperty(parts[0]);
                if (value == null) value = System.getenv(parts[0]);
                return value != null ? value : parts[1];
            } else {
                String value = System.getProperty(v);
                if (value == null) value = System.getenv(v);
                return value != null ? value : match.group(0);
            }
        });
        return resolved;
    }

    public void start() {
        if (running) return;
        running = true;
        // Background lock heartbeat via scheduler (not test synchronization;
        // therefore exempt from the no-Thread.sleep rule governing test waits).
        heartbeatScheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeviceLock-Heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                updateHeartbeats();
                cleanupStaleLocks();
            } catch (Exception e) {
                log.error("Error in heartbeat loop", e);
            }
        }, heartbeatIntervalSeconds, heartbeatIntervalSeconds, java.util.concurrent.TimeUnit.SECONDS);
        log.info("Device lock manager started for owner: {}", ownerId);
    }

    public void stop() {
        running = false;
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdownNow();
            heartbeatScheduler = null;
        }
        releaseAllLocks();
        log.info("Device lock manager stopped");
    }

    public boolean acquireLock(DeviceInfo device) {
        if (!FrameworkConfig.getBoolean("carcomfort.deviceLock.enabled")) {
            log.warn("Device locking is disabled, allowing access to {}", device.udid());
            return true;
        }

        String lockKey = device.platform() + ":" + device.udid();
        ReentrantLock localLock = localLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        if (!localLock.tryLock()) {
            log.warn("Local lock contention for device {}", device.udid());
            return false;
        }

        try {
            File lockFile = lockDir.resolve(lockKey + ".lock").toFile();
            DeviceLock lock = readLockFile(lockFile);

            if (lock != null) {
                if (lock.ownerId().equals(ownerId)) {
                    log.info("Re-acquiring existing lock for device {} by owner {}", device.udid(), ownerId);
                    writeLockFile(lockFile, lock.withTimestamp(LocalDateTime.now()));
                    lockHeartbeats.put(lockKey, LocalDateTime.now());
                    return true;
                }

                if (isStale(lock)) {
                    log.warn("Found stale lock for device {} by owner {}, forcibly releasing", device.udid(), lock.ownerId());
                } else {
                    log.error("Device {} is locked by another owner: {} (status: {})", device.udid(), lock.ownerId(), lock.status());
                    return false;
                }
            }

            DeviceLock newLock = new DeviceLock(
                    device.platform().name(),
                    device.udid(),
                    device.deviceName(),
                    ownerId,
                    LocalDateTime.now(),
                    DeviceLock.Status.ACTIVE
            );
            writeLockFile(lockFile, newLock);
            lockHeartbeats.put(lockKey, LocalDateTime.now());
            log.info("Acquired lock for device {} by owner {}", device.udid(), ownerId);
            return true;

        } catch (IOException e) {
            log.error("Failed to acquire lock for device {}", device.udid(), e);
            return false;
        } finally {
            localLock.unlock();
        }
    }

    public boolean releaseLock(DeviceInfo device) {
        String lockKey = device.platform() + ":" + device.udid();
        ReentrantLock localLock = localLocks.get(lockKey);
        if (localLock == null || !localLock.tryLock()) {
            return false;
        }

        try {
            File lockFile = lockDir.resolve(lockKey + ".lock").toFile();
            DeviceLock lock = readLockFile(lockFile);

            if (lock != null && lock.ownerId().equals(ownerId)) {
                if (lockFile.delete()) {
                    lockHeartbeats.remove(lockKey);
                    log.info("Released lock for device {}", device.udid());
                    return true;
                } else {
                    log.warn("Failed to delete lock file for device {}", device.udid());
                    return false;
                }
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to release lock for device {}", device.udid(), e);
            return false;
        } finally {
            localLock.unlock();
        }
    }

    public Optional<DeviceLock> getLockStatus(DeviceInfo device) {
        String lockKey = device.platform() + ":" + device.udid();
        File lockFile = lockDir.resolve(lockKey + ".lock").toFile();
        try {
            return Optional.ofNullable(readLockFile(lockFile));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void releaseAllLocks() {
        try {
            Files.list(lockDir)
                    .filter(p -> p.toString().endsWith(".lock"))
                    .forEach(this::tryReleaseLockFile);
        } catch (IOException e) {
            log.error("Error releasing all locks", e);
        }
    }

    private void tryReleaseLockFile(Path lockFile) {
        try {
            DeviceLock lock = readLockFile(lockFile.toFile());
            if (lock != null && lock.ownerId().equals(ownerId)) {
                Files.deleteIfExists(lockFile);
                log.info("Released lock for device {}", lock.deviceUdid());
            }
        } catch (IOException e) {
            log.warn("Failed to release lock file {}", lockFile, e);
        }
    }

    private boolean isStale(DeviceLock lock) {
        return lock.timestamp().until(LocalDateTime.now(), ChronoUnit.SECONDS) > staleLockCleanupSeconds;
    }

    private void updateHeartbeats() {
        lockHeartbeats.forEach((lockKey, timestamp) -> {
            File lockFile = lockDir.resolve(lockKey + ".lock").toFile();
            try {
                DeviceLock lock = readLockFile(lockFile);
                if (lock != null && lock.ownerId().equals(ownerId) && lock.status() == DeviceLock.Status.ACTIVE) {
                    writeLockFile(lockFile, lock.withTimestamp(LocalDateTime.now()));
                    lockHeartbeats.put(lockKey, LocalDateTime.now());
                }
            } catch (IOException e) {
                log.debug("Heartbeat update failed for {}", lockKey, e);
            }
        });
    }

    private void cleanupStaleLocks() {
        try {
            Files.list(lockDir)
                    .filter(p -> p.toString().endsWith(".lock"))
                    .forEach(path -> {
                        try {
                            DeviceLock lock = readLockFile(path.toFile());
                            if (lock != null && isStale(lock)) {
                                log.warn("Cleaning up stale lock for device {} owned by {}", lock.deviceUdid(), lock.ownerId());
                                Files.deleteIfExists(path);
                                lockHeartbeats.remove(lock.platform() + ":" + lock.deviceUdid());
                            }
                        } catch (IOException e) {
                            log.debug("Stale lock cleanup failed for {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error during stale lock cleanup", e);
        }
    }

    private DeviceLock readLockFile(File file) throws IOException {
        if (!file.exists()) return null;
        return mapper.readValue(file, DeviceLock.class);
    }

    private void writeLockFile(File file, DeviceLock lock) throws IOException {
        mapper.writeValue(file, lock);
    }

    public record DeviceLock(
            String platform,
            String deviceUdid,
            String deviceName,
            String ownerId,
            LocalDateTime timestamp,
            Status status
    ) {
        public enum Status { ACTIVE, RELEASED, STALE }

        public DeviceLock withTimestamp(LocalDateTime newTimestamp) {
            return new DeviceLock(platform, deviceUdid, deviceName, ownerId, newTimestamp, status);
        }
    }
}
package com.carcomfort.core.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Business-mutation lock — one lock file per business object, in addition
 * to physical-device locking. Only one agent/operation may mutate a given
 * booking at a time:
 *
 * <pre>
 * artifacts/runtime/mutation-&lt;bookingRef&gt;.lock
 * </pre>
 *
 * <p>Stale locks (older than 30 min) are reclaimed; live locks cause an
 * immediate abort (never force-taken). Always released in a
 * {@code finally} block.
 */
public final class BusinessMutationLock {
    private static final Logger log = LoggerFactory.getLogger(BusinessMutationLock.class);
    private static final long STALE_SECONDS = 30 * 60;

    private final Path lockDir;

    public BusinessMutationLock() {
        this(Paths.get("artifacts/runtime"));
    }

    BusinessMutationLock(Path lockDir) {
        this.lockDir = lockDir;
    }

    private static String sanitize(String ref) {
        return ref.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /** Acquires the mutation lock for a booking ref; throws if live-locked. */
    public synchronized Path acquire(String bookingRef, String testRunId, String operation) {
        try {
            Files.createDirectories(lockDir);
            Path lock = lockDir.resolve("mutation-" + sanitize(bookingRef) + ".lock");
            String owner = System.getenv("AUTOMATION_OWNER_ID");
            if (owner == null || owner.isBlank()) {
                owner = "unknown-agent";
            }
            if (Files.exists(lock)) {
                String content = Files.readString(lock);
                long mtime = Files.getLastModifiedTime(lock).toMillis();
                long ageSec = (System.currentTimeMillis() - mtime) / 1000;
                if (ageSec < STALE_SECONDS) {
                    throw new IllegalStateException(
                            "BUSINESS MUTATION LOCKED: " + bookingRef + " owned by [" + content.trim()
                                    + "] (age " + ageSec + "s). Only one agent may mutate a lifecycle object.");
                }
                log.warn("Reclaiming stale mutation lock {} (age {}s)", lock, ageSec);
            }
            String record = "owner=" + owner + " testRunId=" + testRunId
                    + " operation=" + operation + " at=" + Instant.now();
            Files.writeString(lock, record);
            log.info("Mutation lock acquired: {} ({})", bookingRef, operation);
            return lock;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Mutation lock acquire failed for " + bookingRef, e);
        }
    }

    /** Releases the mutation lock (idempotent). */
    public synchronized void release(String bookingRef) {
        try {
            Files.deleteIfExists(lockDir.resolve("mutation-" + sanitize(bookingRef) + ".lock"));
            log.info("Mutation lock released: {}", bookingRef);
        } catch (Exception e) {
            log.warn("Mutation lock release failed for {}", bookingRef, e);
        }
    }
}

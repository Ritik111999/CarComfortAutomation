package com.carcomfort.core.lifecycle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Git-ignored functional-state ledger
 * ({@code artifacts/runtime/FUNCTIONAL_STATE_LEDGER.json}).
 *
 * <p>Every controlled business object (test booking) is recorded here
 * immediately after each verified transition: current state, last
 * action/actor, next expected action, cleanup status. The next agent run
 * reads the ledger first — never recreates a booking to rediscover state.
 * Supports START / RESUME / VERIFY: callers check
 * {@link #getLifecycle} before acting (idempotency).
 *
 * <p>Never stores passwords, tokens, or card data — account aliases only.
 */
public final class FunctionalStateLedger {
    private static final Logger log = LoggerFactory.getLogger(FunctionalStateLedger.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Map<String, Object>>>() {}.getType();

    private final Path ledgerFile;
    private final Map<String, Map<String, Object>> lifecycles = new ConcurrentHashMap<>();

    public FunctionalStateLedger() {
        this(Paths.get("artifacts/runtime/FUNCTIONAL_STATE_LEDGER.json"));
    }

    FunctionalStateLedger(Path ledgerFile) {
        this.ledgerFile = ledgerFile;
        load();
    }

    private synchronized void load() {
        try {
            if (Files.exists(ledgerFile)) {
                String json = Files.readString(ledgerFile);
                Map<String, Map<String, Object>> stored = GSON.fromJson(json, MAP_TYPE);
                if (stored != null) {
                    lifecycles.putAll(stored);
                }
                log.debug("Ledger loaded: {} lifecycles from {}", lifecycles.size(), ledgerFile);
            }
        } catch (Exception e) {
            log.warn("Ledger load failed (starting empty): {}", e.getMessage());
        }
    }

    private synchronized void persist() {
        try {
            Files.createDirectories(ledgerFile.getParent());
            Files.writeString(ledgerFile, GSON.toJson(lifecycles));
        } catch (Exception e) {
            throw new IllegalStateException("Ledger persist failed: " + ledgerFile, e);
        }
    }

    /** Returns the lifecycle record, or null when never started. */
    public synchronized Map<String, Object> getLifecycle(String lifecycleId) {
        Map<String, Object> record = lifecycles.get(lifecycleId);
        return record != null ? new LinkedHashMap<>(record) : null;
    }

    /** Creates (or replaces) a lifecycle record at START. */
    public synchronized void startLifecycle(String lifecycleId, Map<String, Object> initial) {
        Map<String, Object> record = new LinkedHashMap<>(initial);
        record.put("lifecycleId", lifecycleId);
        record.putIfAbsent("currentBusinessState", "STARTED");
        record.put("updatedAt", Instant.now().toString());
        lifecycles.put(lifecycleId, record);
        persist();
        log.info("Ledger START {} state={}", lifecycleId, record.get("currentBusinessState"));
    }

    /** Records one verified transition immediately (never deferred to test end). */
    public synchronized void transition(String lifecycleId, String newState,
                                        String lastAction, String lastActor, String nextExpectedAction) {
        Map<String, Object> record = lifecycles.get(lifecycleId);
        if (record == null) {
            throw new IllegalStateException("Ledger has no lifecycle " + lifecycleId + " — START it first");
        }
        record.put("currentBusinessState", newState);
        record.put("lastAction", lastAction);
        record.put("lastActor", lastActor);
        record.put("nextExpectedAction", nextExpectedAction);
        record.put("updatedAt", Instant.now().toString());
        persist();
        log.info("Ledger {} -> {} ({} by {}, next {})",
                lifecycleId, newState, lastAction, lastActor, nextExpectedAction);
    }

    /** Arbitrary field update (e.g. bookingId once the app reveals it). */
    public synchronized void updateField(String lifecycleId, String field, Object value) {
        Map<String, Object> record = lifecycles.get(lifecycleId);
        if (record == null) {
            throw new IllegalStateException("Ledger has no lifecycle " + lifecycleId);
        }
        record.put(field, value);
        record.put("updatedAt", Instant.now().toString());
        persist();
    }

    public synchronized String currentState(String lifecycleId) {
        Map<String, Object> record = lifecycles.get(lifecycleId);
        return record != null ? String.valueOf(record.getOrDefault("currentBusinessState", "")) : "";
    }
}

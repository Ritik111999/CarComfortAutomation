package com.carcomfort.core.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable device identity and state information.
 */
public record DeviceInfo(
        @JsonProperty("platform") Platform platform,
        @JsonProperty("deviceName") String deviceName,
        @JsonProperty("deviceId") String deviceId,
        @JsonProperty("udid") String udid,
        @JsonProperty("osVersion") String osVersion,
        @JsonProperty("appVersion") String appVersion,
        @JsonProperty("connectionType") ConnectionType connectionType,
        @JsonProperty("availability") Availability availability,
        @JsonProperty("currentOwner") String currentOwner,
        @JsonProperty("lockStatus") LockStatus lockStatus,
        @JsonProperty("lastHealthCheck") @JsonDeserialize(using = LocalDateTimeDeserializer.class) @JsonSerialize(using = LocalDateTimeSerializer.class) LocalDateTime lastHealthCheck,
        @JsonProperty("model") String model,
        @JsonProperty("manufacturer") String manufacturer,
        @JsonProperty("screenResolution") String screenResolution,
        @JsonProperty("density") float density,
        @JsonProperty("apiLevel") int apiLevel
) {
    public enum Platform {
        ANDROID, IOS
    }

    public enum ConnectionType {
        USB, WIFI, NETWORK
    }

    public enum Availability {
        AVAILABLE, BUSY, OFFLINE, UNAUTHORIZED, ERROR, MAINTENANCE
    }

    public enum LockStatus {
        FREE, LOCKED, STALE
    }

    public static DeviceInfo androidDevice(String udid, String deviceName, String osVersion, String model) {
        return new DeviceInfo(
                Platform.ANDROID,
                deviceName,
                udid,
                udid,
                osVersion,
                null,
                ConnectionType.USB,
                Availability.AVAILABLE,
                null,
                LockStatus.FREE,
                LocalDateTime.now(),
                model,
                null,
                null,
                0f,
                0
        );
    }

    public static DeviceInfo iosDevice(String udid, String deviceName, String osVersion, String model) {
        return new DeviceInfo(
                Platform.IOS,
                deviceName,
                udid,
                udid,
                osVersion,
                null,
                ConnectionType.USB,
                Availability.AVAILABLE,
                null,
                LockStatus.FREE,
                LocalDateTime.now(),
                model,
                null,
                null,
                0f,
                0
        );
    }

    public DeviceInfo withAvailability(Availability availability) {
        return new DeviceInfo(platform, deviceName, deviceId, udid, osVersion, appVersion, connectionType,
                availability, currentOwner, lockStatus, lastHealthCheck, model, manufacturer, screenResolution, density, apiLevel);
    }

    public DeviceInfo withLockStatus(LockStatus lockStatus, String owner) {
        return new DeviceInfo(platform, deviceName, deviceId, udid, osVersion, appVersion, connectionType,
                availability, owner, lockStatus, lastHealthCheck, model, manufacturer, screenResolution, density, apiLevel);
    }

    public DeviceInfo withAppVersion(String appVersion) {
        return new DeviceInfo(platform, deviceName, deviceId, udid, osVersion, appVersion, connectionType,
                availability, currentOwner, lockStatus, lastHealthCheck, model, manufacturer, screenResolution, density, apiLevel);
    }

    public DeviceInfo withHealthCheck(LocalDateTime timestamp) {
        return new DeviceInfo(platform, deviceName, deviceId, udid, osVersion, appVersion, connectionType,
                availability, currentOwner, lockStatus, timestamp, model, manufacturer, screenResolution, density, apiLevel);
    }

    public DeviceInfo withDetails(String manufacturer, String screenResolution, float density, int apiLevel) {
        return new DeviceInfo(platform, deviceName, deviceId, udid, osVersion, appVersion, connectionType,
                availability, currentOwner, lockStatus, lastHealthCheck, model, manufacturer, screenResolution, density, apiLevel);
    }

    public boolean isAvailableFor(String owner) {
        return availability == Availability.AVAILABLE &&
                (lockStatus == LockStatus.FREE || (lockStatus == LockStatus.STALE && !Objects.equals(currentOwner, owner)));
    }

    public boolean isOwnedBy(String owner) {
        return Objects.equals(currentOwner, owner) && lockStatus == LockStatus.LOCKED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceInfo that)) return false;
        return Objects.equals(udid, that.udid) && platform == that.platform;
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, udid);
    }
}
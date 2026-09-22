package com.carcomfort.mobile.android;

import com.carcomfort.core.config.FrameworkConfig;
import com.carcomfort.core.driver.AndroidDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Controlled app-state transitions. Never uninstalls, clears data, or resets auth
 * state unless a scenario explicitly authorizes it via configuration.
 */
public final class AppStateManager {
    private static final Logger log = LoggerFactory.getLogger(AppStateManager.class);

    private final AndroidDriverManager drivers;

    public AppStateManager(AndroidDriverManager drivers) {
        this.drivers = drivers;
    }

    private String appPackage() {
        return FrameworkConfig.getString("carcomfort.android.app.package");
    }

    public void launch() {
        drivers.getDriver().activateApp(appPackage());
        log.debug("App launched: {}", appPackage());
    }

    public void terminate() {
        drivers.getDriver().terminateApp(appPackage());
        log.debug("App terminated: {}", appPackage());
    }

    public void restart() {
        terminate();
        launch();
    }

    public void background(Duration duration) {
        drivers.getDriver().runAppInBackground(duration);
        log.debug("App backgrounded for {}", duration);
    }

    public boolean isInstalled() {
        return drivers.getDriver().isAppInstalled(appPackage());
    }

    public void guardedReset(boolean explicitlyAuthorized) {
        if (!explicitlyAuthorized) {
            throw new IllegalStateException(
                    "App reset requires explicit scenario authorization; refusing to clear production state.");
        }
        drivers.getDriver().terminateApp(appPackage());
        log.warn("Authorized app reset executed for {}", appPackage());
    }
}

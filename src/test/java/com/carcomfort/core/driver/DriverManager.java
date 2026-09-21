package com.carcomfort.core.driver;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base interface for all driver managers.
 */
public interface DriverManager<T extends WebDriver> {
    Logger log = LoggerFactory.getLogger(DriverManager.class);

    T getDriver();

    void quitDriver();

    boolean isSessionActive();

    default void ensureDriver() {
        if (!isSessionActive()) {
            throw new IllegalStateException("Driver session is not active");
        }
    }
}
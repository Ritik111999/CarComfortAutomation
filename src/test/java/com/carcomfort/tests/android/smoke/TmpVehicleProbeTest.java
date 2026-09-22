package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.core.device.DeviceManager;
import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.driver.AppiumServerManager;
import com.carcomfort.mobile.android.LocatorFactory;
import com.carcomfort.mobile.android.screens.LoginScreen;
import com.carcomfort.mobile.android.screens.RoleSelectionScreen;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/** Straight-line vehicle-step probe: deterministic path, per-dropdown dumps, first-option picks, Review dump, STOP, BACK home. */
public class TmpVehicleProbeTest {
    private AndroidDriver driver; private Path mapDir;
    private void settle() { try { new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> { try { d.findElements(By.xpath("//*")); return true; } catch (Exception e) { return false; } }); } catch (Exception ignored) {} try { Thread.sleep(1500); } catch (InterruptedException ignored) {} }
    private List<String> descs() { List<String> out = new ArrayList<>(); try { for (WebElement el : driver.findElements(By.xpath("//*[@content-desc]"))) { try { String d = el.getAttribute("content-desc"); if (d != null && !d.isBlank()) out.add(d.replaceAll("\\s+"," ").trim()); } catch (Exception ignored) {} } } catch (Exception ignored) {} return out; }
    private void dump(String id) {
        settle();
        try {
            String src = driver.getPageSource();
            if (!src.contains("io.carcomfort.app")) { System.out.println("VPROBE OUTSIDE_APP " + id); return; }
            Files.writeString(mapDir.resolve(id + ".xml"), src);
            try { var s = driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE); Files.copy(s.toPath(), mapDir.resolve(id + ".png"), StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignored) {}
            List<String> d = descs();
            System.out.println("VPROBE_DUMP " + id + " n=" + d.size());
            for (String x : d) System.out.println("  VPD " + id + " | " + x);
        } catch (Exception e) { System.out.println("VPROBE_DUMP_FAIL " + id + " " + e.getMessage()); }
    }
    private void tapNext() {
        for (WebElement el : driver.findElements(By.xpath("//*[@content-desc]"))) {
            try {
                String d = el.getAttribute("content-desc");
                if (d != null && d.toLowerCase().startsWith("next")) { System.out.println("VPROBE_NEXT [" + d.replaceAll("\\s+"," ").trim() + "]"); el.click(); settle(); return; }
            } catch (Exception ignored) {}
        }
        System.out.println("VPROBE_NO_NEXT");
    }

    @Test(groups = {"smoke"})
    public void vehicleProbe() throws Exception {
        DeviceLockManager lock = new DeviceLockManager(); lock.start();
        DeviceManager dm = new DeviceManager(lock); dm.startAdbServer();
        AppiumServerManager server = new AppiumServerManager();
        AndroidDriverManager drivers = new AndroidDriverManager(lock, dm, server);
        mapDir = Paths.get("artifacts/evidence/mapping"); Files.createDirectories(mapDir);
        try {
            drivers.initialize(); driver = drivers.getDriver();
            settle();
            List<String> d0 = descs();
            if (!d0.contains("Enter Service Details")) {
                RoleSelectionScreen role = new RoleSelectionScreen(drivers);
                LoginScreen login = new LoginScreen(drivers);
                if (d0.contains("Select your role")) { role.selectCustomer(); settle(); }
                login.loginAs(System.getenv("CUSTOMER_TEST_EMAIL"), System.getenv("CUSTOMER_TEST_PASSWORD"));
                login.waitForLoginGone(); settle();
            }
            System.out.println("VPROBE_HOME=" + descs().contains("Enter Service Details"));
            driver.findElement(LocatorFactory.accessibilityId("Car Wash")).click(); settle();
            // location: manual address + suggestion
            driver.findElement(LocatorFactory.accessibilityId("Or Enter Address Manually")).click(); settle();
            var af = driver.findElements(LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")"));
            af.get(0).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
            af.get(0).sendKeys("22021211 Test Street, Nagpur 440022");
            try { if (driver.isKeyboardShown()) driver.hideKeyboard(); } catch (Exception ignored) {}
            settle();
            for (String s : descs()) { if (s.startsWith("Nagpur, Nagpur,")) { driver.findElement(LocatorFactory.accessibilityId(s)).click(); System.out.println("VPROBE_SUGGESTION " + s); settle(); break; } }
            tapNext(); // -> service
            // service: no membership + package fields + SCHEDULE (far-future, low blast radius)
            if (!driver.findElements(LocatorFactory.accessibilityId("I don't have a car wash membership")).isEmpty())
                driver.findElement(LocatorFactory.accessibilityId("I don't have a car wash membership")).click();
            settle();
            // form expands async after the membership tap — wait for the 2 package fields
            try {
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(10))
                        .pollInterval(java.time.Duration.ofMillis(500))
                        .ignoreExceptions()
                        .until(() -> !driver.findElements(LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")")).isEmpty());
            } catch (org.awaitility.core.ConditionTimeoutException e) {
                System.out.println("VPROBE_NO_PACKAGE_FIELDS abort to home");
                for (int i = 0; i < 10; i++) {
                    if (descs().contains("Enter Service Details")) break;
                    try { driver.navigate().back(); } catch (Exception ignored) { break; }
                    settle();
                }
                return;
            }
            var pf = driver.findElements(LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")"));
            System.out.println("VPROBE_PACKAGE_FIELDS=" + pf.size());
            if (pf.size() >= 2) {
                pf.get(0).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
                pf.get(0).sendKeys("Discovery Wash");
                pf.get(1).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
                pf.get(1).sendKeys("9.99");
                try { if (driver.isKeyboardShown()) driver.hideKeyboard(); } catch (Exception ignored) {}
                settle();
            }
            // schedule path (NOT ASAP): far-future slot avoids real dispatch pressure
            if (!driver.findElements(LocatorFactory.accessibilityId("Schedule")).isEmpty()) {
                driver.findElement(LocatorFactory.accessibilityId("Schedule")).click();
                System.out.println("VPROBE_TAPPED Schedule"); settle();
                dump("VPROBE-SCHEDULE-001");
                if (!driver.findElements(LocatorFactory.accessibilityId("Select")).isEmpty()) {
                    driver.findElement(LocatorFactory.accessibilityId("Select")).click();
                    System.out.println("VPROBE_TAPPED Select-datetime"); settle();
                    dump("VPROBE-DATETIME-001");
                    // Material time picker: accept current time via OK (draft), expect date picker next
                    if (!driver.findElements(LocatorFactory.accessibilityId("OK")).isEmpty()) {
                        driver.findElement(LocatorFactory.accessibilityId("OK")).click();
                        System.out.println("VPROBE_TAPPED time-OK"); settle();
                        dump("VPROBE-DATE-001");
                    }
                    // Keyboard can cover/virtualize the schedule section: close it WITHOUT BACK
                    // (BACK pops the wizard). Then scroll down and continue.
                    try { driver.hideKeyboard(); System.out.println("VPROBE_HIDE_KB"); } catch (Exception e) { System.out.println("VPROBE_HIDE_KB_FAIL"); }
                    try {
                        driver.findElement(new io.appium.java_client.AppiumBy.ByAndroidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true)).scrollForward()"));
                        System.out.println("VPROBE_SCROLLED_DOWN"); settle();
                    } catch (Exception e) { System.out.println("VPROBE_SCROLL_FAIL " + e.getMessage()); }
                    dump("VPROBE-DATEPICKER-001");
                    // Time value is tappable — open it to find the date control
                    boolean timeTapped = false;
                    for (String s : descs()) {
                        if (s.matches(".*\\d{1,2}:\\d{2} (AM|PM).*")) {
                            String v = s.replaceAll("\\s+", " ").trim();
                            driver.findElement(LocatorFactory.accessibilityId(v)).click();
                            System.out.println("VPROBE_TAPPED time-value [" + v + "]"); settle();
                            timeTapped = true;
                            break;
                        }
                    }
                    if (timeTapped) dump("VPROBE-DATEPICKER2-001");
                }
            }
            tapNext(); // -> vehicle
            dump("VPROBE-VEHICLE-001");            // each dropdown: open, dump options, pick first non-chrome option
            for (int q = 0; q < 4; q++) {
                var drops = driver.findElements(LocatorFactory.accessibilityId("Select an option"));
                System.out.println("VPROBE_DROPDOWNS_LEFT=" + drops.size());
                if (drops.isEmpty()) break;
                drops.get(0).click(); settle();
                List<String> opts = descs();
                System.out.println("VPROBE_OPTIONS_" + q + ":");
                for (String o : opts) System.out.println("  VOPT | " + o);
                dump("VPROBE-VEH-OPT-" + q + "-001");
                String pick = null;
                for (String o : opts) {
                    String l = o.toLowerCase();
                    if (o.equals("Select an option") || o.startsWith("Where ") || o.startsWith("Enter ")
                            || o.startsWith("Next") || o.equals("Vehicle Details") || o.equals("Service Notes")
                            || o.matches("[1234]") || l.contains("required fields") || l.contains("seamless service")
                            || o.startsWith("Additional") || o.startsWith("Select Your")
                            || o.contains("2025 BMW")) continue;
                    pick = o; break;
                }
                if (pick != null) { driver.findElement(LocatorFactory.accessibilityId(pick)).click(); System.out.println("VPROBE_PICKED [" + pick + "]"); settle(); }
                else { try { driver.navigate().back(); } catch (Exception ignored) {} System.out.println("VPROBE_NO_PICK_back"); settle(); }
                settle();
            }
            dump("VPROBE-VEHICLE-FILLED-001");
            tapNext(); // -> Review & Payment (summary — STOP before any submit)
            dump("VPROBE-REVIEW-001");
            // BACK out to home (draft discarded)
            for (int i = 0; i < 12; i++) {
                if (descs().contains("Enter Service Details")) break;
                try { driver.navigate().back(); } catch (Exception ignored) { break; }
                settle();
            }
            System.out.println("VPROBE_BACK_HOME=" + descs().contains("Enter Service Details"));
        } finally {
            try { drivers.quitDriver(); } catch (Exception ignored) {}
            try { server.stop(); } catch (Exception ignored) {}
            try { lock.stop(); } catch (Exception ignored) {}
        }
    }
}

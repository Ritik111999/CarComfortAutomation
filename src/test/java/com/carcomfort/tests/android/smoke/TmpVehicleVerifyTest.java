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

/** Focused vehicle-step probe: deterministic path, per-question answer verification. Draft only. */
public class TmpVehicleVerifyTest {
    private AndroidDriver driver; private Path mapDir;
    private void settle() { try { new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> { try { d.findElements(By.xpath("//*")); return true; } catch (Exception e) { return false; } }); } catch (Exception ignored) {} try { Thread.sleep(1500); } catch (InterruptedException ignored) {} }
    private List<String> descs() { List<String> out = new ArrayList<>(); try { for (WebElement el : driver.findElements(By.xpath("//*[@content-desc]"))) { try { String d = el.getAttribute("content-desc"); if (d != null && !d.isBlank()) out.add(d.replaceAll("\\s+"," ").trim()); } catch (Exception ignored) {} } } catch (Exception ignored) {} return out; }
    private void dump(String id) {
        settle();
        try {
            String src = driver.getPageSource();
            if (!src.contains("io.carcomfort.app")) { System.out.println("V go outside " + id); return; }
            Files.writeString(mapDir.resolve(id + ".xml"), src);
            List<String> d = descs();
            System.out.println("V dump " + id + " n=" + d.size());
            for (String x : d) System.out.println("  VVD " + id + " | " + x);
        } catch (Exception e) { System.out.println("V dump fail " + id); }
    }

    @Test(groups = {"smoke"})
    public void vehicleVerify() throws Exception {
        DeviceLockManager lock = new DeviceLockManager(); lock.start();
        DeviceManager dm = new DeviceManager(lock); dm.startAdbServer();
        AppiumServerManager server = new AppiumServerManager();
        AndroidDriverManager drivers = new AndroidDriverManager(lock, dm, server);
        mapDir = Paths.get("artifacts/evidence/mapping"); Files.createDirectories(mapDir);
        try {
            drivers.initialize(); driver = drivers.getDriver();
            settle();
            List<String> d0 = descs();
            if (!d0.contains("Enter Service Details") && !d0.contains("Welcome to Car Comfort")
                    && !d0.contains("Select your role")) {
                System.out.println("V DRAFT_OPEN backing out first");
                for (int i = 0; i < 6; i++) {
                    try { driver.navigate().back(); } catch (Exception ignored) { break; }
                    settle();
                    if (descs().contains("Enter Service Details")) break;
                }
                d0 = descs();
            }
            if (!d0.contains("Enter Service Details")) {
                RoleSelectionScreen role = new RoleSelectionScreen(drivers);
                LoginScreen login = new LoginScreen(drivers);
                if (d0.contains("Select your role")) { role.selectCustomer(); settle(); }
                login.loginAs(System.getenv("CUSTOMER_TEST_EMAIL"), System.getenv("CUSTOMER_TEST_PASSWORD"));
                login.waitForLoginGone(); settle();
            }
            driver.findElement(LocatorFactory.accessibilityId("Car Wash")).click(); settle();
            driver.findElement(LocatorFactory.accessibilityId("Or Enter Address Manually")).click(); settle();
            var af = driver.findElements(LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")"));
            af.get(0).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
            af.get(0).sendKeys("22021211 Test Street, Nagpur 440022");
            try { if (driver.isKeyboardShown()) driver.hideKeyboard(); } catch (Exception ignored) {}
            settle();
            for (String s : descs()) { if (s.startsWith("Nagpur, Nagpur,")) { driver.findElement(LocatorFactory.accessibilityId(s)).click(); settle(); break; } }
            tapNext();
            if (!driver.findElements(LocatorFactory.accessibilityId("I don't have a car wash membership")).isEmpty())
                driver.findElement(LocatorFactory.accessibilityId("I don't have a car wash membership")).click();
            settle();
            var pf = driver.findElements(LocatorFactory.uiAutomator("new UiSelector().className(\"android.widget.EditText\")"));
            pf.get(0).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
            pf.get(0).sendKeys("Lifecycle Test Wash");
            pf.get(1).click(); settle(); try { driver.navigate().back(); } catch (Exception ignored) {}
            pf.get(1).sendKeys("9.99");
            try { if (driver.isKeyboardShown()) driver.hideKeyboard(); } catch (Exception ignored) {}
            settle();
            tapNext(); // -> vehicle
            dump("VVEH-001");
            // Exercise the REAL flow method (not a reimplementation)
            com.carcomfort.mobile.android.screens.CustomerBookingWizardScreen wiz =
                    new com.carcomfort.mobile.android.screens.CustomerBookingWizardScreen(drivers);
            java.util.List<String> opts = java.util.Arrays.asList(
                    "My garage", "My parking spot", "Key will be handed in person", "Hand key back to customer");
            wiz.answerVehicleDropdowns(opts);
            System.out.println("V FLOW_DONE unansweredLeft=" + (wiz.hasUnansweredDropdowns() ? "YES" : "NO"));
            dump("VVEH-FLOW-001");
            // Next state
            boolean nextClickable = false;
            try {
                var nexts = driver.findElements(LocatorFactory.accessibilityId("Next: Review & Payment"));
                if (!nexts.isEmpty()) nextClickable = nexts.get(0).isDisplayed() && nexts.get(0).isEnabled();
            } catch (Exception ignored) {}
            System.out.println("V NEXT_CLICKABLE=" + nextClickable);
            // BACK home (draft discarded)
            for (int i = 0; i < 12; i++) {
                if (descs().contains("Enter Service Details")) break;
                try { driver.navigate().back(); } catch (Exception ignored) { break; }
                settle();
            }
            System.out.println("V BACK_HOME=" + descs().contains("Enter Service Details"));
        } finally {
            try { drivers.quitDriver(); } catch (Exception ignored) {}
            try { server.stop(); } catch (Exception ignored) {}
            try { lock.stop(); } catch (Exception ignored) {}
        }
    }

    private void tapNext() {
        for (WebElement el : driver.findElements(By.xpath("//*[@content-desc]"))) {
            try {
                String d = el.getAttribute("content-desc");
                if (d != null && d.toLowerCase().startsWith("next")) { el.click(); settle(); return; }
            } catch (Exception ignored) {}
        }
    }
}

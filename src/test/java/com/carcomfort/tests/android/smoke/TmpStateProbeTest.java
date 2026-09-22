package com.carcomfort.tests.android.smoke;

import com.carcomfort.core.device.DeviceLockManager;
import com.carcomfort.core.device.DeviceManager;
import com.carcomfort.core.driver.AndroidDriverManager;
import com.carcomfort.core.driver.AppiumServerManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import java.nio.file.*;
import java.util.*;

/** Temp read-only state probe. No taps. */
public class TmpStateProbeTest {
    @Test(groups = {"smoke"})
    public void probe() throws Exception {
        DeviceLockManager lock = new DeviceLockManager(); lock.start();
        DeviceManager dm = new DeviceManager(lock); dm.startAdbServer();
        AppiumServerManager server = new AppiumServerManager();
        AndroidDriverManager drivers = new AndroidDriverManager(lock, dm, server);
        try {
            drivers.initialize();
            var driver = drivers.getDriver();
            Thread.sleep(2000);
            String src = driver.getPageSource();
            Path out = Paths.get("artifacts/evidence/mapping/tmp-state.xml");
            Files.createDirectories(out.getParent());
            Files.writeString(out, src);
            System.out.println("TMP_SIZE=" + src.length());
            for (WebElement el : driver.findElements(By.xpath("//*[@content-desc]"))) {
                try { String d = el.getAttribute("content-desc"); if (d != null && !d.isBlank()) System.out.println("TMP_DESC | " + d.replaceAll("\\s+"," ").trim()); } catch (Exception ignored) {}
            }
        } finally { try { drivers.quitDriver(); } catch (Exception ignored) {} try { server.stop(); } catch (Exception ignored) {} try { lock.stop(); } catch (Exception ignored) {} }
    }
}

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverInitialize {
    protected static WebDriver driver;
    Logger logger;

    DriverInitialize() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        chromeOptions.setCapability("unhandledPromptBehavior", "accept");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebElement findElementWithTimeout(String locator, int timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
        return element;
    }

    public WebElement findElementWithTimeout(String locator) {
        return findElementWithTimeout(locator, 5);
    }
}

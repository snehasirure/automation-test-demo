package com.example.automationtesting.selenium;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.testng.Assert.assertEquals;

public class LoginTest {

    private WebElement username;
    private WebElement password;
    private WebElement login;
    private WebDriver driver;

    static ExtentTest test;
    static ExtentReports report;

    @Parameters({"browser"})
    @BeforeClass
    public void beforeTest(@Optional String browser) {

        String os = System.getProperty("os.name").toLowerCase();
        boolean isOperatingSystemWindows = os.contains("win"); //Assuming we test only in Mac and Windows

        if (browser == null) {
            //Use Chrome as default
            browser = "chrome";
            System.setProperty("webdriver.chrome.driver", isOperatingSystemWindows ? "webdrivers/windows/chromedriver.exe" : "webdrivers/mac/chromedriver");
            driver = new ChromeDriver();
        } else {
            if (browser.equalsIgnoreCase("firefox")) {
                System.setProperty("webdriver.gecko.driver", isOperatingSystemWindows ? "webdrivers/windows/geckodriver.exe" : "webdrivers/mac/geckodriver");
                driver = new FirefoxDriver();
            } else if (browser.equalsIgnoreCase("chrome")) {
                System.setProperty("webdriver.chrome.driver", isOperatingSystemWindows ? "webdrivers/windows/chromedriver.exe" : "webdrivers/mac/chromedriver");
                driver = new ChromeDriver();
            } else if (browser.equalsIgnoreCase("edge")) {
                System.setProperty("webdriver.edge.driver", isOperatingSystemWindows ? "webdrivers/windows/msedgedriver.exe" : "webdrivers/mac/msedgedriver");
                driver = new EdgeDriver();
            }
        }

        String fileName = "generated-reports/"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "/LoginTestReport-" + os + "-" + browser + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + ".html";
        report = new ExtentReports(fileName);

        driver.manage().window().maximize();
    }

    @BeforeMethod
    private void resetToLoginPageAndReloadKeyData() {
        driver.navigate().to("https://www.saucedemo.com/");

        username = driver.findElement(By.id("user-name"));
        password = driver.findElement(By.id("password"));
        login = driver.findElement(By.id("login-button"));
    }

    @Test
    public void loginWithLockedOutUserGivesLockError() {
        test = report.startTest("LoginWithLockedOutUserGivesLockErrorTest");
        username.sendKeys("locked_out_user");
        password.sendKeys("secret_sauce");

        login.click();

        String actualUrl = "https://www.saucedemo.com/";
        String expectedUrl = driver.getCurrentUrl();
        assertEquals(expectedUrl, actualUrl);

        WebElement errorMessageBlock = driver.findElement(By.className("error-message-container"));
        assertEquals(errorMessageBlock.getText(), "Epic sadface: Sorry, this user has been locked out.");
        test.log(LogStatus.PASS, "Received locked user message when using user for which access is locked");
    }

    @Test
    public void loginWithCorrectUserCredentials() {
        test = report.startTest("LoginWithCorrectUserCredentialsTest");
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");

        login.click();

        String expectedUrl = "https://www.saucedemo.com/inventory.html";
        String actualUrl = driver.getCurrentUrl();
        assertEquals(expectedUrl, actualUrl);
        test.log(LogStatus.PASS, "Navigated to Inventory page on successful login using valid credentials");
    }

    @Test
    public void loginWithIncorrectUserNameFails() {
        test = report.startTest("LoginWithIncorrectUserNameFailsTest");
        username.sendKeys("sdfc_user");
        password.sendKeys("secret_sauce");

        login.click();

        String actualUrl = "https://www.saucedemo.com/";
        String expectedUrl = driver.getCurrentUrl();
        assertEquals(expectedUrl, actualUrl);

        WebElement errorMessageBlock = driver.findElement(By.className("error-message-container"));
        assertEquals(errorMessageBlock.getText(), "Epic sadface: Username and password do not match any user in this service");
        test.log(LogStatus.PASS, "Received access error when using non-existing user");
    }

    @AfterTest
    private void exit() {
        driver.quit();
    }

    @AfterClass
    public static void endTest() {
        report.endTest(test);
        report.flush();
    }
}
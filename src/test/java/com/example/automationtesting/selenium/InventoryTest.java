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
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class InventoryTest {

    private static final String INVENTORY_URL = "https://www.saucedemo.com/inventory.html";
    private static final String CART_URL = "https://www.saucedemo.com/cart.html";
    private static final String CHECKOUT_MAIN_URL = "https://www.saucedemo.com/checkout-step-one.html";
    private static final String CHECKOUT_OVERVIEW_URL = "https://www.saucedemo.com/checkout-step-two.html";
    private static final String CHECKOUT_COMPLETE_URL = "https://www.saucedemo.com/checkout-complete.html";

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
                + "/InventoryTestReport-" + os + "-" + browser + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + ".html";

        report = new ExtentReports(fileName);

        driver.manage().window().maximize();
    }

    @BeforeMethod
    private void resetToLoginPageAndReloadKeyData() {
        driver.navigate().to("https://www.saucedemo.com/");

        username = driver.findElement(By.id("user-name"));
        password = driver.findElement(By.id("password"));
        login = driver.findElement(By.id("login-button"));

        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");

        login.click();
    }

    @Test
    public void testInventoryManagementActions() {
        test = report.startTest("TestInventoryManagementActions");

        //Verify that current URL is inventory page.
        assertEquals(driver.getCurrentUrl(), INVENTORY_URL);

        //Sort the Items using the Price filter (Highest to Lowest)
        verifyProductSorting();

        //Check that the price of the “Sauce Labs Fleece Jacket” is 49.99 dollars
        WebElement suaceLabsFleeceJacketItem = driver.findElement(By.xpath(".//div[contains(., 'Sauce Labs Fleece Jacket') and @class='inventory_item']"));
        String suaceLabsFleeceJacket_item_price = suaceLabsFleeceJacketItem.findElement(By.className("inventory_item_price")).getText();
        assertEquals(suaceLabsFleeceJacket_item_price, "$49.99");

        //Add the “Sauce Labs Fleece Jacket” to the basket
        WebElement shoppingCartBadgeBeforeAddingAnyProducts = driver.findElement(By.className("shopping_cart_link"));
        assertEquals(shoppingCartBadgeBeforeAddingAnyProducts.getText(), "", "Incorrect size of shopping basket");

        suaceLabsFleeceJacketItem.findElement(By.id("add-to-cart-sauce-labs-fleece-jacket")).click();
        WebElement shoppingCartBadgeAfterAddingFirstProduct = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals(shoppingCartBadgeAfterAddingFirstProduct.getText(), "1", "Incorrect size of shopping basket");

        //Check that the price of the “Sauce Labs Backpack” is 29 dollars
        WebElement suaceLabsBackpackItem = driver.findElement(By.xpath(".//div[contains(., 'Sauce Labs Backpack') and @class='inventory_item']"));
        String suaceLabsBackpackItemPrice = suaceLabsBackpackItem.findElement(By.className("inventory_item_price")).getText();
        assertEquals(suaceLabsBackpackItemPrice, "$29.99");

        //Add the “Sauce Labs Backpack” to the basket
        WebElement shoppingCartBadgeBeforeAddingBackpack = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals(shoppingCartBadgeBeforeAddingBackpack.getText(), "1", "Incorrect size of shopping basket");

        suaceLabsBackpackItem.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        WebElement shoppingCartBadgeAfterAddingBackpack = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals(shoppingCartBadgeAfterAddingBackpack.getText(), "2", "Incorrect size of shopping basket");

        //Navigate to Cart page
        driver.findElement(By.className("shopping_cart_link")).click();
        assertEquals(driver.getCurrentUrl(), CART_URL);

        //Check that Fleece Jacket and Backpack are available in cart
        List<WebElement> cartItems = driver.findElements(By.className("cart_item"));
        assertEquals(cartItems.size(), 2, "Cart should contain 2 items");
        List<String> inventoryItemNames = cartItems.stream()
                .map(webElement -> webElement.findElement(By.className("inventory_item_name")))
                .map(WebElement::getText)
                .collect(Collectors.toList());
        assertTrue(inventoryItemNames.containsAll(Arrays.asList("Sauce Labs Fleece Jacket", "Sauce Labs Backpack")));

        //Navigate to checkout page
        driver.findElement(By.id("checkout")).click();
        assertEquals(driver.getCurrentUrl(), CHECKOUT_MAIN_URL);

        driver.findElement(By.id("first-name")).sendKeys("Sneha");
        driver.findElement(By.id("last-name")).sendKeys("S");
        driver.findElement(By.id("postal-code")).sendKeys("1366");

        driver.findElement(By.id("continue")).click();
        assertEquals(driver.getCurrentUrl(), CHECKOUT_OVERVIEW_URL);

        WebElement paymentInfoText = driver.findElement(By.xpath(".//div[contains(., 'Payment Information') and @class='summary_info_label']"));
        WebElement paymentInfoValue = paymentInfoText.findElement(By.xpath("following-sibling::div"));
        assertEquals(paymentInfoValue.getText(), "SauceCard #31337");

        WebElement shippingInfoText = driver.findElement(By.xpath(".//div[contains(., 'Shipping Information') and @class='summary_info_label']"));
        WebElement shippingInfoValue = shippingInfoText.findElement(By.xpath("following-sibling::div"));
        assertEquals(shippingInfoValue.getText(), "FREE PONY EXPRESS DELIVERY!");

        WebElement summaryTotal = driver.findElement(By.className("summary_total_label"));
        assertTrue(summaryTotal.getText().contains("$86.38"));

        driver.findElement(By.id("finish")).click();
        assertEquals(driver.getCurrentUrl(), CHECKOUT_COMPLETE_URL);

        test.log(LogStatus.PASS, "All Product inventory actions are performed successfully");
    }

    private void verifyProductSorting() {
        WebElement defaultProductSortOption = driver.findElement(By.className("active_option"));
        assertEquals(defaultProductSortOption.getText(), "NAME (A TO Z)");
        Select productSortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        productSortDropdown.selectByValue("hilo");
        WebElement highToLowOption = driver.findElement(By.className("active_option"));
        assertEquals(highToLowOption.getText(), "PRICE (HIGH TO LOW)");
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

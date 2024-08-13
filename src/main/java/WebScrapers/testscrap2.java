package WebScrapers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.io.IOException;

public class testscrap2 {

    private static final String DB_URL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String UK = "622a65b4671f2c8b98fac83f";
        String USA = "622a65bd671f2c8b98faca1a";
        String EUROPE = "622a659af0bac38678ed1398";
        String Australia = "622a65b0671f2c8b98fac759";

        String[] locations = {EUROPE, Australia, UK, USA};

        // Create a thread pool with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (String location : locations) {
            executor.execute(() -> {
                try {
                    scrapeJobs(location);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
    }

    private static void scrapeJobs(String location) throws SQLException, IOException {
        WebDriver driver = null;
        Connection connection = null;
        int totalJobsAppended = 0;
        List<String[]> jobDetailsList = new ArrayList<>();

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("--disable-gpu");
            driver = new ChromeDriver(options);

            System.out.println("ADDING JOBS FROM \"jobgether.com\" for location: " + location);
            driver.get("https://jobgether.com/search-offers?locations=" + location
                    + "&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f");
            driver.manage().window().maximize();
            

            handlePopUp(driver);

            WebElement resultCountElement = getElementIfExists(driver,
                    "//div[contains(@class,'sort_counter_container')]/div/div[1]");
            String resultText = resultCountElement.getText();
            String[] parts = resultText.split(" ");
            int totalJobCount = Integer.parseInt(parts[0].trim());
            System.out.println("Total jobs found: " + totalJobCount);

            for (int i = 1; i <= totalJobCount; i++) {
                String companyName = "";
                String jobTitle = "";
                String jobLocation = "";
                String jobURL = "";
                String companyWebsite = "";
                String companySize = "";
                String dateCreated = "";

                System.out.println("Adding Jobs for " + location + "...");

                WebElement jobTitleElement = getElementIfExists(driver,
                        "(//div[@id='offer-body'])[" + i + "]/div/div/h3");
                if (jobTitleElement == null) {
                    try {
                        if (i % 35 == 0) {
                            WebElement seeMore = getElementIfExists(driver,
                                    "//a[normalize-space()='See more']");
                            if (seeMore != null) {
                                seeMore.click();
                                jobTitleElement = getElementIfExists(driver,
                                        "(//div[@id='offer-body'])[" + i + "]/div/div/h3");
                            }
                            sleepRandom();
                        }
                    } catch (Exception e) {
                        System.out.println("Inner break performed at " + i);
                        break;
                    }
                }

                if (i % 2 == 0 && i <= totalJobCount) {
                    int j = i - 1;
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
                            driver.findElement(By.xpath("(//div[@id='offer-body'])[" + j + "]/div/div/h3")));
                }

                if (jobTitleElement != null) {
                    jobTitle = jobTitleElement.getText();
                }

                WebElement jobLinkElement = getElementIfExists(driver,
                        "(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]");

                if (jobLinkElement != null) {
                    jobLinkElement.click();
                    sleepRandom();
                }

                List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(1));
                jobURL = driver.getCurrentUrl();

                // Extract additional details
                WebElement jobLocationElement = getElementIfExists(driver,
                        "//div[@id='offer_general_data']//span[contains(.,'Work from:')]/following-sibling::div");
                if (jobLocationElement != null) {
                    jobLocation = jobLocationElement.getText();
                }

                WebElement companyNameElement = getElementIfExists(driver,
                        "//div[contains(@class,'flex justify-center')]/following-sibling::span[1]");
                if (companyNameElement != null) {
                    companyName = companyNameElement.getText();
                }

                WebElement companyUrlElement = getElementIfExists(driver,
                        "//div[contains(@class,'flex justify-center')]/following-sibling::a");
                if (companyUrlElement != null) {
                    companyWebsite = companyUrlElement.getAttribute("href");
                }

                WebElement companySizeElement = getElementIfExists(driver,
                        "//div[contains(@class,'flex justify-center')]/following-sibling::div//span");
                if (companySizeElement != null) {
                    companySize = companySizeElement.getText();
                }

                dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                List<String> validSizes = Arrays.asList("11 - 50", "2 - 10", "51 - 200");

                // Add job details to the list
                if (validSizes.contains(companySize)) {
                    jobDetailsList.add(new String[]{jobTitle, jobLocation, jobURL, companyName, companySize,
                            companyWebsite, "jobgether.com", dateCreated});
                }

                // Close the job detail tab and switch back
                driver.close();
                driver.switchTo().window(tabs.get(0));

                if (i % 35 == 0) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
                                driver.findElement(
                                        By.xpath("(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]")));
                    } finally {
                        WebElement seeMore = getElementIfExists(driver, "//a[normalize-space()='See more']");
                        if (seeMore != null) {
                            seeMore.click();
                        }
                    }

                    sleepRandom();
                }
            }

        } catch (Exception e) {
            System.out.println("Code Not executed completely for location -- " + location);
            e.printStackTrace();
        } finally {
            // SQL connection setup
            connection = DriverManager.getConnection(DB_URL);

            // SQL queries
            String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";
            ResultSet resultSet = null;
            // Check and insert jobs into the database
            String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            for (String[] jobDetails : jobDetailsList) {
                String jobURL = jobDetails[2];

                // Check if job URL already exists
                PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                checkStatement.setString(1, jobURL);
                resultSet = checkStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    // Insert new job listing
                    PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                    for (int j = 0; j < jobDetails.length; j++) {
                        insertStatement.setString(j + 1, jobDetails[j]);
                    }
                    insertStatement.executeUpdate();
                    insertStatement.close();
                    totalJobsAppended++;
                }
                resultSet.close();
                checkStatement.close();
            }
            if (totalJobsAppended != 0) {
                System.out.println(totalJobsAppended + " jobs added to DB from location: " + location);
            } else {
                System.out.println("No new jobs found from location: " + location);
            }

            if (driver != null) {
                driver.quit();
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static WebElement getElementIfExists(WebDriver driver, String xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            return null;
        }
    }

    private static void sleepRandom() {
        try {
            int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 2 seconds
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handlePopUp(WebDriver driver) {
        try {
            // Check if the pop-up exists
            WebElement closeButton = getElementIfExists(driver, "//button[@data-pc-section='closebutton']");
            if (closeButton != null) {
                closeButton.click();
                System.out.println("Pop-up closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 
}

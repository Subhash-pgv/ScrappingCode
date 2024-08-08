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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class JobScrapping3 {
    public static void main(String[] args) {
        WebDriver driver = null;
        Connection connection = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("--disable-gpu");
            driver = new ChromeDriver(options);

            String UK = "622a65b4671f2c8b98fac83f";
            String USA = "622a65bd671f2c8b98faca1a";
            String EUROPE = "622a659af0bac38678ed1398";
            String Australia = "622a65b0671f2c8b98fac759";

            int totalJobsAppended = 0;

            String[] locations = {EUROPE, Australia,UK, USA};
            for (String location : locations) {
                driver.get("https://jobgether.com/search-offers?locations=" + location + "&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f");
//              boolean t = ((ChromeDriver) driver).getCapabilities().getBrowserName().contains("Headless");
//                if(!t) {
               	driver.manage().window().maximize();
//                }else {
//                MaximizeWindowIfNot.maximizeWindowIfNot(driver);
//               }
                Thread.sleep(10000);
                
                handlePopUp(driver); // Handle pop-ups before interacting with elements
                

                System.out.println("ADDING JOBS FROM \"jobgether.com\"");

                

                // SQL connection setup
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
                connection = DriverManager.getConnection(connectionURL);

                // SQL queries
                String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";

                WebElement resultCountElement = driver.findElement(By.xpath("//div[contains(@class,'sort_counter_container')]/div/div[1]"));
                String resultText = resultCountElement.getText();
                String[] parts = resultText.split(" ");
                int totalJobCount = Integer.parseInt(parts[0].trim());
                System.out.println(totalJobCount);
		
        
       
        for(int i =1; i<=totalJobCount;i++) {
                    String companyName = "";
                    String jobTitle = "";
                    String jobLocation = "";
                    String jobURL = "";
                    String companyWebsite = "";
                    String source = "jobgether.com";
                    String companySize = "";
                    String dateCreated = "";

                    System.out.println("Adding Jobs to DB please wait until it shows completed.....");

                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                    
                    WebElement jobTitleElement = getElementIfExists(driver, "(//div[@id='offer-body'])[" + i + "]/div/div/h3");
                    if(jobTitleElement == null) {
                    	break;
                    }
    
                    if (i % 2 == 0&&i<=totalJobCount) {
                        int j = i - 1;
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//div[@id='offer-body'])[" + j + "]/div/div/h3")));
                    }

					//WebElement jobTitleElement = getElementIfExists(driver, "(//div[@id='offer-body'])[" + i + "]/div/div/h3");
					
                    if (jobTitleElement != null) {
                        jobTitle = jobTitleElement.getText();
                    }

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]")));
                    WebElement jobLinkElement = getElementIfExists(driver, "(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]");

                    if (jobLinkElement != null) {
                        jobLinkElement.click();
                        sleepRandom();
                    }
                        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                        driver.switchTo().window(tabs.get(1));
                        jobURL = driver.getCurrentUrl();

                        // Extract additional details
                        WebElement jobLocationElement = getElementIfExists(driver, "//div[@id='offer_general_data']//span[contains(.,'Work from:')]/following-sibling::div");
                        if (jobLocationElement != null) {
                            jobLocation = jobLocationElement.getText();
                        }

                        WebElement companyNameElement = getElementIfExists(driver, "//div[contains(@class,'flex justify-center')]/following-sibling::span[1]");
                        if (companyNameElement != null) {
                            companyName = companyNameElement.getText();
                        }

                        WebElement companyUrlElement = getElementIfExists(driver, "//div[contains(@class,'flex justify-center')]/following-sibling::a");
                        if (companyUrlElement != null) {
                            companyWebsite = companyUrlElement.getAttribute("href");
                        }

                        WebElement companySizeElement = getElementIfExists(driver, "//div[contains(@class,'flex justify-center')]/following-sibling::div//span");
                        if (companySizeElement != null) {
                            companySize = companySizeElement.getText();
                        }

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        dateCreated = now.format(formatter);

                        List<String> validSizes = Arrays.asList("11 - 50", "2 - 10", "51 - 200");

                        // Check if job URL already exists
                        PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                        checkStatement.setString(1, jobURL);
                        ResultSet resultSet = checkStatement.executeQuery();
                        if (resultSet.next() && resultSet.getInt(1) == 0) {
                            if (validSizes.contains(companySize)) {
                                // Insert new job listing
                                PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                                insertStatement.setString(1, jobTitle);
                                insertStatement.setString(2, jobLocation);
                                insertStatement.setString(3, jobURL);
                                insertStatement.setString(4, companyName);
                                insertStatement.setString(5, companySize);
                                insertStatement.setString(6, companyWebsite);
                                insertStatement.setString(7, source);
                                insertStatement.setString(8, dateCreated);
                                insertStatement.executeUpdate();
                                insertStatement.close();
                                totalJobsAppended++;
                            }
                        }

                        resultSet.close();
                        checkStatement.close();

                        driver.close();
                        driver.switchTo().window(tabs.get(0));

                        if (i % 35 == 0) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]")));
                            driver.findElement(By.xpath("//a[normalize-space()='See more']")).click();
                            sleepRandom();
                        }
                       
                }
   
            }
            System.out.println("Total jobs appended: " + totalJobsAppended);
        } catch (Exception e) {
            e.printStackTrace();
           
        } finally {
            if (driver != null) {
              //  driver.quit();
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
        	  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20)); 
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
            WebElement closeButton = getElementIfExists(driver, "//button[@data-pc-section='closebutton']"); // Replace with actual class or ID
            if (closeButton != null) {
                closeButton.click();
                System.out.println("pop-up closed");
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }
    }
    

}

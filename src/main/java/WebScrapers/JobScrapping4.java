package WebScrapers;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;

public class JobScrapping4 {
    public static void main(String[] args) {
        WebDriver driver = null;
        Connection connection = null;
        try {
            ChromeOptions options = new ChromeOptions();
             options.addArguments("--headless");
             options.addArguments("--window-size=1920x1080");
             options.addArguments("--disable-gpu");
            driver = new ChromeDriver(options);
            Actions actions = new Actions(driver);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            driver.get("https://www.workingnomads.com/jobs?location=europe,australia,usa,uk&category=development");
            driver.manage().window().maximize();
            
            System.out.println("ADDING JOBS FROM \"workingnomads.com\"");

            Thread.sleep(5000);

          

            // SQL connection setup
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
            connection = DriverManager.getConnection(connectionURL);

            // SQL queries
            String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";

            WebElement resultCountElement = driver.findElement(By.xpath("(//div[contains(@class,'total-number')])[1]"));
            String resultText = resultCountElement.getText();
            String parts =  resultText.split(" ")[0];
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            int totalJobCount=0;
            try {
                Number number = format.parse(parts);
                totalJobCount = number.intValue();
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            int totalJobsAppended = 0;

            for (int i = 1; i <= totalJobCount; i++) {
                String companyName = "";
                String jobTitle = "";
                String jobLocation = "";
                String jobURL = "";
                String companyWebsite = "";
                String source = "workingnomads.com";
                String companySize = "";
                String dateCreated = "";
                
                System.out.println("Adding JObs to DB please wait untill it shows completed.....");

                
                
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[@class='job-cols']//h4/a)["+ i +"]")));
                WebElement jobTitleElement = getElementIfExists(driver, "(//div[@class='job-cols']//h4[1]//a)["+ i +"]");
                //Make webelement on focus 
                if(i%2==0) {
                	int j=i-1;
                	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//div[@class='job-cols']//h4/a)["+ j +"]")));
                }
                
                if (jobTitleElement != null) {
                    jobTitle = jobTitleElement.getText();
                    
                }
                
               
            	wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("(//div[@class='job-cols']//h4/a)["+ i +"]")));
                WebElement jobLinkElement = getElementIfExists(driver, "(//div[@class='job-cols']//h4[1]/a)["+ i +"]");
               
                if (jobLinkElement != null) {
                	jobURL =  jobLinkElement.getAttribute("href");
                    sleepRandom();

                    
                    // Extract additional details
                    WebElement jobLocationElement = getElementIfExists(driver, "(//div[contains(@class,'boxes')]//div[contains(@ng-show,'source.locations')])[" + i +"]");
                    if (jobLocationElement != null) {
                        jobLocation = jobLocationElement.getText();
                    }

                    String URL = null;
                    WebElement companyNameElement = getElementIfExists(driver, "(//div[@class='job-cols'])["+i+"]//div[contains(@class,'company')]/a");
                    if (companyNameElement != null) {
                        companyName = companyNameElement.getText();
                        URL = companyNameElement.getAttribute("href");
                       
                    }
                    
                    
                    String script = "window.open(arguments[0], '_blank');";
    		        js.executeScript(script, URL);
                    
                    List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(1));
                    sleepRandom();
                    

                    WebElement companyUrlElement1 = getElementIfExists(driver,"//div[@class='company-links']/a");
                    if (companyUrlElement1 != null) {
                        companyWebsite = companyUrlElement1.getAttribute("href");
                    }

                    WebElement companyUrlElement2 = getElementIfExists(driver,"//div[@class='job-company']//a");
                    if (companyUrlElement2 != null) {
                        companyWebsite = companyUrlElement2.getAttribute("href");
                    }
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    dateCreated = now.format(formatter);

                    // Check if job URL already exists
                    PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                    checkStatement.setString(1, jobURL);
                    ResultSet resultSet = checkStatement.executeQuery();
                   
                    if (resultSet.next() && resultSet.getInt(1) == 0) { 	
                    	
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
                    
                    
                    resultSet.close();
                    checkStatement.close();

                    driver.close();
                    driver.switchTo().window(tabs.get(0));

                    if (i % 50 == 0) {
                       
                        driver.findElement(By.xpath("//div[@class='show-more']")).click();
                    }
                }
            }

            System.out.println("Total jobs appended: " + totalJobsAppended);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
               // driver.quit();
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
            List<WebElement> elements = driver.findElements(By.xpath(xpath));
            if (elements.size() > 0) {
                return elements.get(0);
            }
        } catch (NoSuchElementException e) {
            // Element is not found, return null
        }
        return null;
    }
  

    private static void sleepRandom() {
        try {
            int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 2 seconds
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JobScrapping1 {
	public static void main(String[] args)
			throws IOException, InterruptedException, SQLException, ClassNotFoundException {

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--window-size=1920x1080");
		options.addArguments("--disable-gpu");
		WebDriver driver = new ChromeDriver(options);
		Actions actions = new Actions(driver);

		JavascriptExecutor js = (JavascriptExecutor) driver;

		driver.get("https://account.ycombinator.com/?continue=https%3A%2F%2Fwww.workatastartup.com%2F");
		driver.manage().window().maximize();
		sleepRandom();
		System.out.println("ADDING JOBS FROM \"www.ycombinator.com\"");

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		// sql connection set up
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";

		Connection connection = DriverManager.getConnection(connectionURL);

		String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName,employeeCount,companyWebsite,source,dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		String checkSQL = "SELECT COUNT(*) FROM jobListings WHERE jobUrl = ?";

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("(//div[@class='MuiFormControl-root input-group'])[1]")));

		driver.findElement(By.xpath("//input[@id='ycid-input']")).sendKeys("vikram-katta");

		WebElement element = driver.findElement(By.xpath("//label[normalize-space()='Password']"));
		actions.moveToElement(element).click().perform();

		driver.findElement(By.xpath("//input[@id='password-input']")).sendKeys("ABCD@1432");
		driver.findElement(By.xpath("//span[@class='MuiButton-label']")).click();
		sleepRandom();

		openUrl( driver);
		WebElement resultCountElement = driver.findElement(By.xpath("//p[contains(normalize-space(.), 'matching startups')]"));;
		int totalmatching =0;
		if(resultCountElement !=null) {
			String resultText = resultCountElement.getText();
			sleepRandom();
			String[] parts = resultText.split(" ");
			String numberStr = parts[1].trim();
			// Total number of comapnies
			totalmatching = Integer.parseInt(numberStr);
		}else {
			driver.close();
		
			openUrl( driver);	
			}
	
		int totalJobsAppended = 0;
		String employeeCount = null;
		String companyWebsite = null;
		String source = "ycombinator.com";
		String dateCreated = null;
		String msg="";
		WebElement matching = null;
		WebElement employeesElement1 = null;
		WebElement employeesElement2=null;
		try {
		
			
			for (int i = 1; i <= totalmatching; i++) {
				System.out.println("Adding JObs to DB please wait untill it shows completed.....");

				String xpathExpression = String.format(
						"(//div[contains(@class,'mb-5 rounded pb-4')])[%d]//div[contains(@class,'font-medium')]", i);
				WebElement jobListing = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathExpression)));
				jobListing.click();
				sleepRandom();

				List<String> tab = new ArrayList<>(driver.getWindowHandles());
				sleepRandom();
				driver.switchTo().window(tab.get(2));

				wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a")));
				List<WebElement> jobTitles = driver.findElements(
						By.xpath("//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a"));

				for (int j = 1; j <= jobTitles.size(); j++) {
					String companyName = driver.findElement(By.xpath("//span[@class='company-name hover:underline']"))
							.getText();
					
					if( companyName =="companyName") {
						
						System.out.println("block");
						
					}
					String JobTitle = driver.findElement(By
							.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a)["
									+ j + "]"))
							.getText();
					String JobLocation = driver.findElement(
							By.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name'])["
									+ j + "]//following-sibling::div/span[1]"))
							.getText();
					String JobURL = driver.findElement(By
							.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a)["
									+ j + "]"))
							.getAttribute("href");

					companyWebsite = driver.findElement(By.xpath("(//div[@class='text-sm'])[1]/div[1]/div/div[2]/a"))
							.getAttribute("href");

					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					dateCreated = now.format(formatter);

					try {
					 employeesElement1 = getElementIfExists(driver, "//i[contains(@title,'people')]/following-sibling::div");
					 
					 employeesElement2 = getElementIfExists(driver, "//i[contains(@title,'person')]/following-sibling::div");
					
					}finally{
					if (employeesElement1 != null) {
						String employeeNumTExt = employeesElement1.getText();
						String[] splits = employeeNumTExt.split(" ");
						employeeCount = splits[0].trim();
					}else if(employeesElement2 != null) {
						String employeeNumTExt = employeesElement2.getText();
						String[] splits = employeeNumTExt.split(" ");
						employeeCount = splits[0].trim();
					}else {
						employeeCount= null;
						
					}
				}
					

					// Check if job URL already exists
					PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
					checkStatement.setString(1, JobURL);
					ResultSet resultSet = checkStatement.executeQuery();
					if (resultSet.next() && resultSet.getInt(1) == 0) {
						
						// Insert new job listing
						PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
						insertStatement.setString(1, JobTitle);
						insertStatement.setString(2, JobLocation);
						insertStatement.setString(3, JobURL);
						insertStatement.setString(4, companyName);
						insertStatement.setString(5, employeeCount);
						insertStatement.setString(6, companyWebsite);
						insertStatement.setString(7, source);
						insertStatement.setString(8, dateCreated);
						insertStatement.executeUpdate();
						insertStatement.close();
						
						totalJobsAppended++;

						System.out.println(msg);
						
					}
					resultSet.close();
					checkStatement.close();
				}
				driver.close();
				driver.switchTo().window(tab.get(1));

				if (i == totalmatching) {
					System.out.println("Searched all companies for new jobs");

					if (i == totalJobsAppended) {
						System.out.println("All (" + i + ") companies' jobs added to DB successfully.");
					} else if (totalJobsAppended > 0) {
						System.out.println(totalJobsAppended + " jobs added to DB successfully.");
					} else {
						System.out.println("No new jobs found");
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Code Not executed completely");
			e.printStackTrace();
		} finally {
			driver.quit();
			connection.close();
		}

	}
	
	   private static WebElement getElementIfExists(WebDriver driver, String xpath) {
	        try {
	            List<WebElement> elements = driver.findElements(By.xpath(xpath));
	            if (elements.size() > 0) {
	                return elements.get(0);
	            }
	        } catch (NoSuchElementException e) {
	                e.getStackTrace();
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
	
	private static void openUrl(WebDriver driver) throws InterruptedException {
		
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	
        String URL = "https://www.workatastartup.com/companies?companySize=seed&companySize=small&demographic=any&hasEquity=any&hasSalary=any&industry=any&interviewProcess=any&jobType=any&layout=list-compact&locations=US&locations=GB&locations=AU&locations=AT&locations=BE&locations=BG&locations=HR&locations=CY&locations=CZ&locations=DK&locations=FI&locations=FR&locations=DE&locations=GR&locations=HU&locations=IT&locations=MT&locations=NL&role=eng&sortBy=created_desc&tab=any&usVisaNotRequired=any";
		String script = "window.open(arguments[0], '_blank');";
		sleepRandom();
		js.executeScript(script, URL);
		Thread.sleep(5000);

		List<String> tabs = new ArrayList<>(driver.getWindowHandles());
		driver.switchTo().window(tabs.get(1));

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(normalize-space(.), 'matching startups')]")));
		sleepRandom();
	}
}
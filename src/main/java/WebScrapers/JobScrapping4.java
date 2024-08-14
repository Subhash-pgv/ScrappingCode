package WebScrapers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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
import java.io.File;
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

		List<String[]> jobDetailsList = new ArrayList<>();
		int totalJobsAppended = 0;
		String source = null;
		try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless");
			options.addArguments("--window-size=1920x1080");
			options.addArguments("--disable-gpu");
			driver = new ChromeDriver(options);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			driver.get("https://www.workingnomads.com/jobs?location=europe,australia,usa,uk&category=development");
			driver.manage().window().maximize();

			System.out.println("ADDING JOBS FROM \"workingnomads.com\"");

			Thread.sleep(5000);

			WebElement resultCountElement = driver.findElement(By.xpath("(//div[contains(@class,'total-number')])[1]"));
			String resultText = resultCountElement.getText();
			String parts = resultText.split(" ")[0];
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			int totalJobCount = 0;
			try {
				Number number = format.parse(parts);
				totalJobCount = number.intValue();

			} catch (Exception e) {
				e.printStackTrace();
				// takeScreenshot( driver,"error");
				
				 File screenshotFile = takeScreenshotGit(driver, "error");
				 commitScreenshot(screenshotFile);
			}

			for (int i = 1; i <= totalJobCount; i++) {
				String companyName = "";
				String jobTitle = "";
				String jobLocation = "";
				String jobURL = "";
				String companyWebsite = "";
				source = "workingnomads.com";
				String employeeCount = "";
				String dateCreated = "";

				System.out.println("looking Job " + i + " from " + source + " please wait until it shows completed.....");

				WebElement jobTitleElement = getElementIfExists(driver,
						"(//div[@class='job-cols']//h4[1]//a)[" + i + "]");
				// Make webelement on focus
				if (i % 2 == 0) {
					int j = i - 1;
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
							driver.findElement(By.xpath("(//div[@class='job-cols']//h4/a)[" + j + "]")));
				}

				String relativeTime = "";
				String featured ="";
				WebElement dateElement = getElementIfExists(driver,
						"(//div[@ng-show='!job._source.premium' and contains(@class, 'date')])["+ i +"]");
				
				WebElement featuredElement = getElementIfExists(driver,
						"(//div[normalize-space()='Featured'])["+ i +"]");
				if (featuredElement != null) {
					featured = featuredElement.getText();
				}
				
				if (dateElement != null) {
					relativeTime = dateElement.getText();
				}

				if ((isLessThanThreeDays(relativeTime))||featured!="Featured"||i%40==0) {

					if (jobTitleElement != null) {
						jobTitle = jobTitleElement.getText();

					}

					WebElement jobLinkElement = getElementIfExists(driver,
							"(//div[@class='job-cols']//h4[1]/a)[" + i + "]");

					if (jobLinkElement != null) {
						jobURL = jobLinkElement.getAttribute("href");
						sleepRandom();

						// Extract additional details
						WebElement jobLocationElement = getElementIfExists(driver,
								"(//div[contains(@class,'boxes')]//div[contains(@ng-show,'source.locations')])[" + i+ "]");
						if (jobLocationElement != null) {
							jobLocation = jobLocationElement.getText();
						}

						String URL = null;
						WebElement companyNameElement = getElementIfExists(driver,
								"(//div[@class='job-cols'])[" + i + "]//div[contains(@class,'company')]/a");
						if (companyNameElement != null) {
							companyName = companyNameElement.getText();
							URL = companyNameElement.getAttribute("href");

						}

						String script = "window.open(arguments[0], '_blank');";
						js.executeScript(script, URL);

						List<String> tabs = new ArrayList<>(driver.getWindowHandles());
						driver.switchTo().window(tabs.get(1));
						sleepRandom();

						WebElement companyUrlElement1 = getElementIfExists(driver, "//div[@class='company-links']/a");
						WebElement companyUrlElement2 = getElementIfExists(driver, "//div[@class='job-company']//a");

						companyWebsite = (companyUrlElement1 != null)? companyUrlElement1.getAttribute("href")
								: (companyUrlElement2 != null ? companyUrlElement2.getAttribute("href") : null);

						LocalDateTime now = LocalDateTime.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						dateCreated = now.format(formatter);

						jobDetailsList.add(new String[] { jobTitle, jobLocation, jobURL, companyName, employeeCount,
								companyWebsite, source, dateCreated });

						driver.close();
						driver.switchTo().window(tabs.get(0));
					}
					if (i % 50 == 0) {

						driver.findElement(By.xpath("//div[@class='show-more']")).click();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// takeScreenshot( driver,"error");
			
			 File screenshotFile = takeScreenshotGit(driver, "error");
			 commitScreenshot(screenshotFile);
		} finally {

			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
				connection = DriverManager.getConnection(connectionURL);

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

				// Summary of results

				if (totalJobsAppended > 0) {
					System.out.println(totalJobsAppended + " jobs added to DB successfully.--" + source);
				} else {
					System.out.println("No new jobs found.--" + source);
				}

			} catch (Exception e) {
				System.out.println("Error in Jobs adding to data base. -- " + source);
				e.printStackTrace();
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

	private static boolean isLessThanThreeDays(String relativeTime) {
		if (relativeTime.contains("minute") || relativeTime.contains("seconds") || relativeTime.contains("hour")) {
			return true; // If it contains minute or second, it is within 3 days
		} else if (relativeTime.contains("day")) {
			String[] parts = relativeTime.split(" ");
			int days = Integer.parseInt(parts[1]); // Get the number of days
			return days < 3; // Check if it's less than 3 days
		}
		return false; // Default case, if not matched
	}
	
	private static void takeScreenshot(WebDriver driver, String fileName) {
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/svegi/eclipse-workspace/WebScrapers/ExtendReports/screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(source, destination);
			System.out.println("Screenshot taken: " + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 private static File takeScreenshotGit(WebDriver driver, String fileName) {
	        File screenshotFile = null;
	        try {
	            TakesScreenshot ts = (TakesScreenshot) driver;
	            screenshotFile = ts.getScreenshotAs(OutputType.FILE);
	            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
	            
	            // Modify this path to your Git folder path
	            File destination = new File("path/to/your/git/repo/screenshots/"
	                    + fileName + "_" + timestamp + ".png");
	            
	            FileUtils.copyFile(screenshotFile, destination);
	            System.out.println("Screenshot taken: " + destination.getPath());
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return screenshotFile;
	    }

	    private static void commitScreenshot(File screenshotFile) {
	        try {
	            String command = "git add " + screenshotFile.getPath() +
	                             " && git commit -m 'Added screenshot for error' " +
	                             " && git push";
	            Runtime.getRuntime().exec(command);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}

package WebScrapers;

import org.openqa.selenium.By;
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
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




public class testscrap1 {
	public static void main(String[] args) throws IOException, InterruptedException {

		ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920x1080");
        options.addArguments("--disable-gpu");
		WebDriver driver = new ChromeDriver(options);
		Actions actions = new Actions(driver);

		driver.get("https://account.ycombinator.com/?continue=https%3A%2F%2Fwww.workatastartup.com%2F");
		driver.manage().window().maximize();
		sleepRandom();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = LocalDateTime.now().format(formatter);
		String fileName = "job_listings1_" + timestamp + ".csv";

		FileWriter csvWriter = new FileWriter(fileName);
		csvWriter.append("Job Title,Job Location,Job URL,Company Name\n");

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("(//div[@class='MuiFormControl-root input-group'])[1]")));

		driver.findElement(By.xpath("//input[@id='ycid-input']")).sendKeys("vikram-katta");

		WebElement element = driver.findElement(By.xpath("//label[normalize-space()='Password']"));
		actions.moveToElement(element).click().perform();

		driver.findElement(By.xpath("//input[@id='password-input']")).sendKeys("ABCD@1432");
		driver.findElement(By.xpath("//span[@class='MuiButton-label']")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='role']//div[text()='Any']")))
				.click();

		driver.findElement(By.xpath("//div[@id='react-select-2-option-1']")).click();
		

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='companySize']//div[text()='Any']")))
				.click();

		driver.findElement(By.xpath("//div[@id='react-select-4-option-1']")).click();

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@id='companySize']//div[contains(text(),'1 - 10 people')]")))
				.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='react-select-4-option-2']")))
				.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'Select...')]")))
				.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[.='Remote only']"))).click();

		Thread.sleep(5000);
		WebElement resultCountElement = driver
				.findElement(By.xpath("//p[contains(normalize-space(.), 'matching startups')]"));
		String resultText = resultCountElement.getText();
		sleepRandom();
		String[] parts = resultText.split(" ");
		String numberStr = parts[1].trim();
		// Total number of comapnies
		int totalmatchings = Integer.parseInt(numberStr);

		try {
			System.out.println("Adding JObs to CSV please wait untill it shows completed.....");
			for (int i = 1; i <= totalmatchings; i++) {

				String xpathExpression = String.format(
						"(//div[contains(@class,'mb-5 rounded pb-4')])[%d]//div[contains(@class,'font-medium')]", i);
				WebElement jobListing = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathExpression)));
				jobListing.click();
				sleepRandom();

				List<String> tabs = new ArrayList<>(driver.getWindowHandles());
				driver.switchTo().window(tabs.get(1));

				wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a")));
				List<WebElement> jobTitles = driver.findElements(
						By.xpath("//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a"));

				for (int j = 1; j <= jobTitles.size(); j++) {
					String comapnyName = driver.findElement(By.xpath("//span[@class='company-name hover:underline']"))
							.getText();
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

					// Escape and wrap fields in quotes
					csvWriter.append("\"").append(JobTitle.replace("\"", "\"\"")).append("\",").append("\"")
							.append(JobLocation.replace("\"", "\"\"")).append("\",").append("\"")
							.append(JobURL.replace("\"", "\"\"")).append("\",").append("\"")
							.append(comapnyName.replace("\"", "\"\"")).append("\"\n");
				}
				driver.close();
				driver.switchTo().window(tabs.get(0));
				
				if (i == totalmatchings) {
					System.out.println("All (" + totalmatchings + ")Comapnies Jobs added to CSV Successfully.");
				}
			}
		} catch (Exception e) {
			System.out.println("Code Not executed completely");
			e.printStackTrace();
		} finally {
			csvWriter.flush();
			csvWriter.close();
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
}

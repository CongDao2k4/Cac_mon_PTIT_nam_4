package com.web_crawling;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TakeReviewOnProduct {
    private static boolean checkAccessable(String url) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.comment-list")));

            String title = driver.getTitle();
            System.out.println("    Kết nối thành công: " + url + " (title = " + title + ")");
            return true;
        } catch (Exception e) {
            System.err.println("    Lỗi khi tải trang với WebDriver: " + url + "  - " + e.getMessage());
            return false;
        } finally {
            if (driver != null) driver.quit();
        }
    }

    public static void takeCmt(String url) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        WebDriver driver = null;

        url = (url.lastIndexOf("?") >= 0) ? url.substring(0, url.lastIndexOf("?")) : url;
        url = url + "/danh-gia";
        System.out.println("Bắt đầu truy cập trang web link : " + url);

        int page = 0;
        while (true) {
            page++;
            String url_per_page = String.format("%s?page=%d", url, page);
            if (!checkAccessable(url_per_page)) {
                return;
            }
            try {
                driver = new ChromeDriver(options);
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));

                driver.get(url_per_page);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("ul.comment-list")));

//                List<WebElement> comments = driver.findElements(By.cssSelector("ul.comment-list"));
                List<WebElement> comments = driver.findElements(By.cssSelector("ul.comment-list li"));

                if (comments.isEmpty()) {
                    System.out.println("    Lỗi do không còn comment ở page" + url_per_page);
                    break;
                }

                List<WebElement[]> pairs = new ArrayList<>();
                for (WebElement item : comments) {
                    WebElement star = item.findElement(By.cssSelector("div.cmt-top-star"));
                    WebElement comment = item.findElement(By.cssSelector("p.cmt-txt"));
                    pairs.add(new WebElement[]{star, comment});
                }

                saveToCsv(pairs, url, url_per_page);

            } catch (Exception e) {
                System.err.println("    Lỗi do driver khi thăm dò page  " + url_per_page);
            } finally {
                if (driver != null) driver.quit();
            }
        }
        if (driver != null) driver.quit();
    }

    private static void saveToCsv(List<WebElement[]> pairs, String url, String url_per_page) {
        System.out.println("    Bắt đầu lấy các comment ở "+ url);
        Path dir = Paths.get("src", "main", "resources", "output");

        String sp = url.substring(url.lastIndexOf("dienmayxanh.com/")+15).replaceAll("/","__");
        sp = sp + ".csv";
        Path filePath = dir.resolve(sp);

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile(), true))) {
            if (filePath.toFile().length() == 0) {
                writer.writeNext(new String[]{"data", "label", "num_stars"});
            }

            for (WebElement[] x : pairs) {
//                List<WebElement> children = x.findElements(By.xpath("*")); //cái này là WebElement tfim By.cssSelector("ul.comment-list")
//                for (WebElement y : children) {
//                    List<WebElement> numUpvotes = y.findElements(By.className("iconcmt-starbuy"));
//                    List<WebElement> numDownvotes = y.findElements(By.className("iconcmt-unstarbuy"));
//                    WebElement content = y.findElement(By.className("cmt-txt"));
//                    String text = content.getText().trim();
//                    if (!text.isBlank()) {
////                        text = Jsoup.parse(text).text();
////                        System.out.println(text);
////                        System.out.println(numUpvotes.size());
////                        System.out.println(numDownvotes.size());
//                        String data = Jsoup.parse(text).text();
//                        String label = (numUpvotes.size() > 3) ? "1" : "0";
//                        writer.writeNext(new String[]{data, label});
//                    }
//                }
                List<WebElement> numUpvotes = x[0].findElements(By.className("iconcmt-starbuy"));
                List<WebElement> numDownvotes = x[0].findElements(By.className("iconcmt-unstarbuy"));
                String data = Jsoup.parse(x[1].getText().trim()).text();

                String label = "0";
                if(numUpvotes.size() > 3) label = "2";
                else if(numUpvotes.size() == 3) label = "1";
                else label = "0";

                String numStar = "" + numUpvotes.size();
                if(data.length() > 10) {
                    writer.writeNext(new String[]{data, label, numStar});
                }
            }
            System.out.println("    Ghi csv xong từ comment page  " + url_per_page);
        } catch (IOException e) {
            System.err.println("    Lỗi ghi comment từ page  " + url_per_page);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Path dir = Paths.get("src", "main", "resources", "csv");
        Path filePath = dir.resolve("quat_links.csv");

        // Sử dụng Virtual Threads
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
                List<String[]> allRows = reader.readAll();
                allRows.remove(0); // Bỏ hàng tiêu đề
                for (int i = 0; i < allRows.size(); i += 1) {
                    takeCmt(allRows.get(i)[1]);
                }
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
        }
    }
}

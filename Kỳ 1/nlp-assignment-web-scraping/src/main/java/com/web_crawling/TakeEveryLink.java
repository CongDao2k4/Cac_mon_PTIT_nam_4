package com.web_crawling;

import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TakeEveryLink {

    private static HttpClient httpClient;
    private static Map<String, String> headers;
    private static final String BASE_URL = "https://www.dienmayxanh.com";
    private static ArrayList<String> list_of_Error_links;

    public TakeEveryLink() {
        list_of_Error_links = new ArrayList<>();
        initHttpClient();
    }

    private void initHttpClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9," +
                "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Language", "vi,en-US;q=0.9,en;q=0.8");
        headers.put("Referer", BASE_URL);
    }

    private boolean checkAcceptable(String url) {
        // Cách 1: Kiểm tra URL bằng HttpClient (nhanh và hiệu quả)
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()) // Sửa lỗi ở đây
                    .timeout(Duration.ofSeconds(20));

            // Thêm các headers đã được cấu hình
            headers.forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            if (statusCode >= 400) {
                System.err.println("HttpRequest kiểm tra URL không thể truy cập (HTTP " + statusCode + "): " + url);
                return false;
            }
        } catch (Exception e) {
            System.err.println("HttpRequest kiểm tra Lỗi khi kiểm tra URL bằng HttpClient: " + url + " - " + e.getMessage());
            return false;
        }

        // Cách 2: Nếu HttpClient thành công, dùng WebDriver để kiểm tra nội dung trang
        WebDriver driver = null;
        try {
            // ... (phần code WebDriver không đổi)
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.listproduct")));

            String title = driver.getTitle();
            System.out.println("Kết nối thành công: " + url + " (title = " + title + ")");
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi tải trang với WebDriver: " + url + " - " + e.getMessage());
            return false;
        } finally {
            if (driver != null) driver.quit();
        }
    }

    public void takeLinks() {
        System.out.println("=== Bắt đầu crawl Điện Máy Xanh ===");
        String[] categories = {
//                "o-cung-di-dong"
//                "may-giat", "dieu-hoa", "tu-lanh", "tivi", "may-say-quan-ao", "binh-tam-nong-lanh", "tu-dong", "may-rua-chen", "micro-cac-loai",
//                "quat-dieu-hoa", "quat", "may-xay-sinh-to", "may-ep-trai-cay", "may-loc-nuoc", "may-loc-khong-khi","may-hut-am","bep-tu", "bep-hong-ngoai",
//                "noi-com-dien", "robot-hut-bui", "ban-ui", "bep-ga", "lo-vi-song", "binh-dun-sieu-toc",
//                "dien-thoai", "may-tinh-bang",
//                "laptop", "dong-ho-thong-minh",
//                "may-tinh-nguyen-bo", "man-hinh-may-tinh", "may-in",
//                "gia-treo-man-hinh",
//                "pc-may-in",
//                "may-say-toc","may-tao-kieu-toc","may-cao-rau","may-cat-toc","ghe-massage","ban-chai-danh-rang","may-tam-nuoc",
//                "bo-lau-nha", "noi",
//                "chao-chong-dinh",
//                "binh-ly-giu-nhiet","hop-dung-thuc-pham",
//                "camera-giam-sat", "flycam",
//                "tai-nghe", "dan-loa-dvd", "dan-loa-dvd-loa-karaoke-xach-tay", "dan-loa-dvd-loa-bluetooth", "dan-loa-dvd-loa-vi-tinh"
        };
        int batchSize = 4;
        for (int i = 0; i < categories.length; i += batchSize) {
            ExecutorService executor = Executors.newFixedThreadPool(batchSize);
            for (int j = i; j < i + batchSize && j < categories.length; j++) {
                String category = categories[j];
                executor.submit(() -> takeLinkPerCategory(category));
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("✅ Batch " + (i / batchSize + 1) + " đã xong");
        }
        System.out.println("=== Crawl hoàn tất ===");
    }


    private void takeLinkPerCategory(String category) {
        WebDriver driver = null;
        try {
            String url = BASE_URL + "/" + category;
            System.out.println("Bắt đầu truy cập "+ url);
            if (!checkAcceptable(url)) {
                list_of_Error_links.add(category);
                return;
            }

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.container-productbox ul.listproduct")));

            // --- Vòng lặp click "Xem thêm" ---
            By productItemSelector = By.cssSelector("ul.listproduct > li");
            int prevCount = driver.findElements(productItemSelector).size();

            while (true) {
                List<WebElement> seeMoreCandidates = driver.findElements(By.cssSelector("div.view-more a"));
                WebElement seeMore = null;
                for (WebElement cand : seeMoreCandidates) {
                    if (cand.isDisplayed() && cand.isEnabled()) {
                        seeMore = cand;
                        break;
                    }
                }

                if (seeMore == null) {
                    System.out.println("Không còn nút 'Xem thêm'. Dừng.");
                    break;
                }

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", seeMore);
                try { Thread.sleep(15000); } catch (InterruptedException ignored) {}

                try {
                    new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(seeMore));
                    seeMore.click();
                    System.out.println("Đã click nút 'Xem thêm'");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seeMore);
                    System.out.println("Đã click bằng JS (fallback)");
                }

                // chờ số lượng sản phẩm tăng lên
                try {
                    int finalPrevCount = prevCount;
                    new WebDriverWait(driver, Duration.ofSeconds(20))
                            .until(d -> d.findElements(productItemSelector).size() > finalPrevCount);
                    prevCount = driver.findElements(productItemSelector).size();
                    System.out.println("Tổng sản phẩm hiện tại: " + prevCount);
                } catch (TimeoutException te) {
                    System.out.println("Không load thêm sản phẩm. Dừng.");
                    break;
                }
            }

            // Lấy danh sách link cuối cùng
            List<WebElement> elements = driver.findElements(
                    By.cssSelector("div.container-productbox ul.listproduct li a:first-child"));
            Set<String> uniqueLinks = elements.stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(Objects::nonNull)
                    .filter(href -> !href.isBlank() && !href.equals("javascript:;"))
                    .map(href -> href.startsWith("/") ? BASE_URL + href : href)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            System.out.printf("[%s] Tổng sản phẩm: %d - Link duy nhất: %d%n",
                    category, elements.size(), uniqueLinks.size());

            saveToCsv(category, uniqueLinks);

        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý category " + category + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }
    }

    private void saveToCsv(String category, Set<String> links) {
        Path dir = Paths.get("src", "main", "resources", "csv");
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(category + "_links.csv");
            try (CSVWriter writer = new CSVWriter(new FileWriter(file.toFile()))) {
                writer.writeNext(new String[]{"Category", "Product Link"});
                for (String link : links) {
                    writer.writeNext(new String[]{category, link});
                }
            }
            System.out.println("Đã lưu file: " + file + " (" + links.size() + " link)");
        } catch (IOException e) {
            System.err.println("Không thể ghi CSV: " + e.getMessage());
        }
    }
    private String returnResultAfterCrawl() {
        if (list_of_Error_links == null || list_of_Error_links.isEmpty()) {
            return "";
        }
        return "Số lượng các category ko kết nối được là: " + list_of_Error_links.size() + list_of_Error_links.parallelStream().collect(Collectors.joining(","));
    }

    public static void main(String[] args) {
        TakeEveryLink crawler = new TakeEveryLink();
        crawler.takeLinks();
        System.out.println(crawler.returnResultAfterCrawl());
    }
}

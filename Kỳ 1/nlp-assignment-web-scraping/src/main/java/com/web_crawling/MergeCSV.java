package com.web_crawling;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MergeCSV {

    /**
     * Lấy danh sách các tệp CSV trong thư mục với các tiền tố cụ thể.
     *
     * @param directoryPath Đường dẫn đến thư mục.
     * @param prefixes      Mảng các tiền tố của tệp.
     * @return Danh sách các tệp phù hợp.
     * @throws IOException Nếu có lỗi khi truy cập thư mục.
     */
    private static List<File> getCsvFilesByPrefix(Path directoryPath, String[] prefixes) throws IOException {
        List<File> files = new ArrayList<>();
        if (!Files.isDirectory(directoryPath)) {
            return files;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "*.csv")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                for (String prefix : prefixes) {
                    if (fileName.startsWith(prefix)) {
                        files.add(entry.toFile());
                        break;
                    }
                }
            }
        }
        return files;
    }

    /**
     * Hợp nhất nội dung của nhiều tệp CSV vào một tệp duy nhất.
     * Bỏ qua hàng tiêu đề (header) của các tệp từ tệp thứ hai trở đi.
     *
     * @param files      Danh sách các tệp CSV cần hợp nhất.
     * @param outputFile Đường dẫn của tệp đầu ra.
     * @throws IOException Nếu có lỗi khi đọc/ghi tệp.
     */
    private static void mergeCsvFiles(List<File> files, Path outputFile) throws IOException {
        if (files.isEmpty()) {
            return;
        }

        // Tạo tệp đầu ra nếu chưa tồn tại
        if (!Files.exists(outputFile.getParent())) {
            Files.createDirectories(outputFile.getParent());
        }

        // Lấy nội dung của tệp đầu tiên (bao gồm cả header)
        File firstFile = files.get(0);
        FileUtils.copyFile(firstFile, outputFile.toFile());

        // Hợp nhất nội dung của các tệp còn lại (bỏ qua header)
        for (int i = 1; i < files.size(); i++) {
            File currentFile = files.get(i);
            List<String> lines = FileUtils.readLines(currentFile, "UTF-8");
            if (lines.size() > 1) { // Đảm bảo tệp có dữ liệu
                // Lấy nội dung từ dòng thứ 2 (bỏ qua header)
                List<String> dataLines = lines.subList(1, lines.size());
                FileUtils.writeLines(outputFile.toFile(), "UTF-8", dataLines, true);
            }
        }
    }

    public static void main(String[] args) {
        Path dir = Paths.get("src", "main", "resources", "output");
        Path mergedFilePath = Paths.get("src", "main", "resources", "merged_output.csv");

        // Các tiền tố cần lọc
        String[] prefixes = {"__quat", };

        try {
            // Lấy danh sách các tệp CSV cần hợp nhất
            List<File> filesToMerge = getCsvFilesByPrefix(dir, prefixes);

            if (filesToMerge.isEmpty()) {
                System.out.println("Không tìm thấy tệp nào để hợp nhất.");
                return;
            }

            // Hợp nhất các tệp
            mergeCsvFiles(filesToMerge, mergedFilePath);
            System.out.println("Đã hợp nhất thành công các tệp vào: " + mergedFilePath.toAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
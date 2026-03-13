package org.example.directoriesCreate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateFolderOrFile {
    public static void createFolder(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                System.out.println("Đã tạo thư mục: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Lỗi IO khi tạo thư mục hoặc lưu file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

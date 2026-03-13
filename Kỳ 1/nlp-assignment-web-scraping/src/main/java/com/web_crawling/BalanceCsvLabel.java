package com.web_crawling;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BalanceCsvLabel {

    private static List<String[]> readCsv(String filePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> data = reader.readAll();
            if (!data.isEmpty()) {
                data.remove(0);
            }
            return data;
        }
    }

    private static Map<String, List<String[]>> classifyByLabel(List<String[]> data) {
        Map<String, List<String[]>> classifiedData = new HashMap<>();
        classifiedData.put("0", new ArrayList<>());
        classifiedData.put("1", new ArrayList<>());
        classifiedData.put("2", new ArrayList<>());

        for (String[] row : data) {
            // Giả sử cột "Label" là cột thứ hai (chỉ số 1)
            if (row.length > 1) {
                String label = row[1];
                if (classifiedData.containsKey(label)) {
                    classifiedData.get(label).add(row);
                }
            }
        }
        return classifiedData;
    }

    private static int findMinCount(Map<String, List<String[]>> classifiedData) {
        int minCount = Integer.MAX_VALUE;
        for (List<String[]> list : classifiedData.values()) {
            minCount = Math.min(minCount, list.size());
        }
        return minCount;
    }

    private static List<String[]> equalizeData(Map<String, List<String[]>> classifiedData, int count) {
        List<String[]> equalizedData = new ArrayList<>();
        for (List<String[]> list : classifiedData.values()) {
            Collections.shuffle(list);
            equalizedData.addAll(list.subList(0, Math.min(count, list.size())));
        }
        return equalizedData;
    }

    private static void writeCsv(String filePath, List<String[]> data) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"data", "label", "num_stars"};
            writer.writeNext(header);
            writer.writeAll(data);
        }
    }

    public static void main(String[] args) {
        Path mergedFilePath = Paths.get("src", "main", "resources", "merged_output.csv");
        Path outputFilePath = Paths.get("src", "main", "resources", "balanced_output.csv");

        try {
            List<String[]> allData = readCsv(mergedFilePath.toString());
            Map<String, List<String[]>> classifiedData = classifyByLabel(allData);
            int minCount = findMinCount(classifiedData);
            List<String[]> equalizedData = equalizeData(classifiedData, minCount);
            writeCsv(outputFilePath.toString(), equalizedData);
            System.out.println("Tệp CSV đã được xử lý và lưu tại: " + outputFilePath.toAbsolutePath());
            System.out.println("Số lượng bản ghi cho mỗi nhãn (0, 1, 2) là: " + minCount);
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

}

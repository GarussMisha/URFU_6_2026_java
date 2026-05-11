package org.example.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.example.model.Country;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для парсинга CSV-файла со странами
 */
public class CSVParser {

    /**
     * Парсит CSV файл и возвращает список стран (старое имя метода для совместимости)
     *
     * @param filePath путь к CSV файлу
     * @return список объектов Country
     */
    public List<Country> parseCSV(String filePath) {
        return parse(filePath);
    }
    
    /**
     * Парсит CSV файл и возвращает список стран
     *
     * @param filePath путь к CSV файлу
     * @return список объектов Country
     */
    public List<Country> parse(String filePath) {
        List<Country> countries = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath));
             org.apache.commons.csv.CSVParser csvParser = org.apache.commons.csv.CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {
            
            for (CSVRecord record : csvParser) {
                try {
                    String name = record.get("Country or area");
                    String subregion = record.get("Subregion");
                    String region = record.get("Region");
                    
                    String internetUsersStr = record.get("Internet users");
                    String populationStr = record.get("Population");
                    
                    // Парсим числа, убирая запятые
                    long internetUsers = parseLong(internetUsersStr);
                    long population = parseLong(populationStr);
                    
                    Country country = new Country(name, subregion, region, internetUsers, population);
                    countries.add(country);
                } catch (Exception e) {
                    System.err.println("Ошибка парсинга строки: " + record);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения CSV файла: " + filePath);
            e.printStackTrace();
        }
        
        return countries;
    }
    
    /**
     * Удаляет неразрывные пробелы (NBSP - U+00A0) из строки
     */
    private String removeNBSP(String value) {
        if (value == null) {
            return null;
        }
        // Заменяем NBSP (U+00A0) на обычный пробел (U+0020)
        return value.replace("\u00A0", " ");
    }
    
    /**
     * Парсит строку как long, удаляя запятые из чисел
     */
    private long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        // Сначала удаляем NBSP, затем запятые и пробелы из строки (например, "1,234,567" -> "1234567")
        String cleanValue = removeNBSP(value).replace(",", "").trim();
        try {
            return Long.parseLong(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("Невозможно преобразовать в long: " + value);
            return 0L;
        }
    }
}

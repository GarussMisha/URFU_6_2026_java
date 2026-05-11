package org.example;

import org.example.db.DatabaseManager;
import org.example.exporter.DataExporter;
import org.example.model.Country;
import org.example.parser.CSVParser;
import org.example.query.QueryExecutor;
import org.example.visualize.Visualizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Главный класс приложения для обработки данных о странах.
 */
public class Main {
    
    private static final String CSV_PATH = "docs/Country.csv";
    private DatabaseManager dbManager;
    private QueryExecutor queryExecutor;

    public static void main(String[] args) {
        Main app = new Main();
        
        try {
            // Инициализация
            app.init();

            // Задача 1: Парсинг CSV и экспорт в БД
            System.out.println("=== ЗАДАЧА 1: Экспорт данных из CSV в БД ===");
            app.exportDataFromCSV();

            // Задача 2: Выполнение SQL запросов
            System.out.println("\n=== ЗАДАЧИ 2-4: Работа с данными в БД ===");
            app.executeQueryTasks();

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении программы:");
            e.printStackTrace();
        } finally {
            try {
                if (app.dbManager != null) {
                    app.dbManager.closeConnection();
                }
            } catch (SQLException e) {
                System.err.println("Ошибка закрытия соединения с БД: " + e.getMessage());
            }
        }
    }

    /**
     * Инициализация компонентов приложения
     */
    private void init() throws SQLException {
        dbManager = new DatabaseManager();
        queryExecutor = new QueryExecutor(dbManager.getConnection());

        // Создаем таблицу в БД
        dbManager.createTable();
    }

    /**
     * Задача 1: Парсинг CSV и экспорт данных в БД
     */
    private void exportDataFromCSV() throws IOException, SQLException {
        CSVParser parser = new CSVParser();

        // Читаем данные из CSV файла
        List<Country> countries = parser.parseCSV(CSV_PATH);
        System.out.println("Прочитано стран из CSV: " + countries.size());

        // Экспортируем в БД
        DataExporter exporter = new DataExporter(dbManager);
        exporter.exportToDatabase(countries);
    }

    /**
     * Выполнение всех SQL запросов (задачи 1-3) с сохранением результатов в файл и построение графиков
     */
    private void executeQueryTasks() throws SQLException {
        // Создаем папку для вывода если не существует
        createOutputDirectory();

        Visualizer visualizer = new Visualizer();

        // Задача 1: График процентного соотношения пользователей по субрегионам
        System.out.println("\n--- Задача 1: Процентное соотношение интернет-пользователей по субрегионам ---");
        List<Country> chartData = queryExecutor.getCountriesForChart();

        // Сохраняем результаты задачи 1 в текстовый файл для скриншотов
        saveQueryResultsToFile("Задача_1_Percentage_By_Subregion.txt", "Процентное соотношение интернет-пользователей по субрегионам:", chartData, true);
        for (Country country : chartData) {
            String line = String.format("%s: %.2f%%%n",
                    country.getSubregion(), country.getPercentage());
            System.out.print(line);
        }

        // Визуализация данных - bar chart с сохранением в PNG
        String barChartPath = "data/chart_percentage_by_subregion.png";
        visualizer.createPercentageBarChart(chartData, barChartPath);

        // Задача 2: Страна с самым низким показателем интернет-пользователей в Восточной Европе
        System.out.println("\n--- Задача 2: Страна с наименьшим количеством интернет-пользователей в Восточной Европе ---");
        Country lowestCountry = queryExecutor.getCountryWithLowestInternetUsersInEasternEurope();

        // Сохраняем результаты задачи 2 в текстовый файл для скриншотов
        saveQueryResultForTask2("Задача_2_Lowest_Eastern_Europe.txt", "Страна с наименьшим количеством интернет-пользователей в Восточной Европе:", lowestCountry);
        if (lowestCountry != null) {
            String result = String.format(Locale.US, "Страна: %s%nРегион: %s%nСубрегион: %s%nКоличество интернет-пользователей: %,d%n",
                    lowestCountry.getName(), lowestCountry.getRegion(), lowestCountry.getSubregion(),
                    lowestCountry.getInternetUsers());
            System.out.print(result);
        } else {
            String result = "Нет данных для Восточной Европы\n";
            System.out.println(result);
        }

        // Задача 3: Страна с процентом пользователей от 75% до 85%
        System.out.println("\n--- Задача 3: Страна с процентом интернет-пользователей от 75% до 85% ---");
        Country rangeCountry = queryExecutor.getCountryWithInternetUserPercentageInRange();

        // Сохраняем результаты задачи 3 в текстовый файл для скриншотов
        saveQueryResultForTask3("Задача_3_Percentage_Range.txt", "Страна с процентом интернет-пользователей от 75% до 85%:", rangeCountry);
        if (rangeCountry != null) {
            String result = String.format(Locale.US, "Страна: %s%nРегион: %s%nСубрегион: %s%nКоличество интернет-пользователей: %,d%n" +
                    "Население: %,d%nПроцент пользователей: %.2f%%%n",
                    rangeCountry.getName(), rangeCountry.getRegion(), rangeCountry.getSubregion(),
                    rangeCountry.getInternetUsers(), rangeCountry.getPopulation(), rangeCountry.getPercentage());
            System.out.print(result);
        } else {
            String result = "Нет стран с процентом пользователей от 75% до 85%\n";
            System.out.println(result);
        }

        // Дополнительная визуализация - pie chart распределения по регионам
        List<Country> allCountries = queryExecutor.getAllCountries();
        String pieChartPath = "data/chart_regions.png";
        visualizer.createRegionPieChart(allCountries, pieChartPath);
    }

    /**
     * Создает папку для вывода если не существует
     */
    private void createOutputDirectory() {
        File dir = new File("output");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("\nСоздана директория: " + (created ? "успешно" : "не удалось"));
        }
    }

    /**
     * Сохраняет результаты запроса в текстовый файл для скриншотов с данными
     */
    private void saveQueryResultsToFile(String filename, String header, List<Country> data, boolean isChartData) {
        try (FileWriter writer = new FileWriter("output/" + filename)) {
            // Добавляем заголовок с датой и временем выполнения
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            writer.write("=".repeat(60) + "\n");
            writer.write(header + "\n");
            writer.write("Дата выполнения: " + dateFormat.format(timestamp) + "\n\n");
            
            if (isChartData) {
                // Для графика - выводим все данные по субрегионам
                for (Country country : data) {
                    String line = String.format("  %s: %.2f%%\n",
                            country.getSubregion(), country.getPercentage());
                    writer.write(line);
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Ошибка записи в файл " + filename + ": " + e.getMessage());
        }
    }
    
    /**
     * Сохраняет результат для задачи 2 (одна страна)
     */
    private void saveQueryResultForTask2(String filename, String header, Country country) {
        try (FileWriter writer = new FileWriter("output/" + filename)) {
            writer.write("=".repeat(60) + "\n");
            writer.write(header + "\n\n");
            
            if (country != null) {
                String result = String.format(Locale.US, "  Страна: %s\n" +
                        "  Регион: %s\n" +
                        "  Субрегион: %s\n" +
                        "  Интернет-пользователей: %,d\n",
                        country.getName(), country.getRegion(), country.getSubregion(),
                        country.getInternetUsers());
                writer.write(result);
            } else {
                writer.write("  Нет данных для Восточной Европы\n");
            }
        } catch (java.io.IOException e) {
            System.err.println("Ошибка записи в файл " + filename + ": " + e.getMessage());
        }
    }
    
    /**
     * Сохраняет результат для задачи 3 (страна в диапазоне)
     */
    private void saveQueryResultForTask3(String filename, String header, Country country) {
        try (FileWriter writer = new FileWriter("output/" + filename)) {
            writer.write("=".repeat(60) + "\n");
            writer.write(header + "\n\n");
            
            if (country != null) {
                String result = String.format("  Страна: %s\n" +
                        "  Регион: %s\n" +
                        "  Субрегион: %s\n" +
                        "  Интернет-пользователей: %,d\n" +
                        "  Население: %,d\n" +
                        "  Процент пользователей: %.2f%%\n",
                        country.getName(), country.getRegion(), country.getSubregion(),
                        country.getInternetUsers(), country.getPopulation(), country.getPercentage());
                // Заменяем NBSP (Non-Breaking Space, U+00A0) на обычный пробел
                writer.write(result.replace('\u00A0', ' '));
            } else {
                writer.write("  Нет стран с процентом пользователей от 75% до 85%\n");
            }
        } catch (java.io.IOException e) {
            System.err.println("Ошибка записи в файл " + filename + ": " + e.getMessage());
        }
    }
}

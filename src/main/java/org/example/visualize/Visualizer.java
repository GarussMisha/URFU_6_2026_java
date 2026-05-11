package org.example.visualize;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.example.model.Country;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Класс для визуализации данных о странах с помощью диаграмм.
 */
public class Visualizer {
    
    /**
     * Создает bar chart по количеству интернет-пользователей по странам и сохраняет в PNG файл
     *
     * @param countries список стран
     * @param outputPath путь для сохранения файла (например, "data/chart_internet_users.png")
     */
    public void createInternetUsersBarChart(List<Country> countries, String outputPath) {
        // Берем топ 10 стран для наглядности
        int limit = Math.min(10, countries.size());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < limit; i++) {
            Country country = countries.get(i);
            String countryName = country.getName().length() > 20 ?
                country.getName().substring(0, 17) + "..." : country.getName();
            dataset.addValue(country.getInternetUsers(), "Интернет-пользователи", countryName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Топ стран по количеству интернет-пользователей (млн)",
            "Страна",
            "Количество пользователей (млн)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        // Сохраняем в PNG файл с увеличенным размером для лучшей читаемости
        saveChartToFile(chart, outputPath, 1200, 600);
    }
    
    /**
     * Создает pie chart для распределения интернет-пользователей по регионам
     * с отображением процента и количества пользователей
     *
     * @param countries список стран
     * @param outputPath путь для сохранения файла (например, "data/chart_regions.png")
     */
    public void createRegionPieChart(List<Country> countries, String outputPath) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // Группируем интернет-пользователей по регионам (суммируем пользователей, а не страны)
        java.util.Map<String, Long> regionUsers = new java.util.HashMap<>();
        for (Country country : countries) {
            String region = country.getRegion() != null ? country.getRegion() : "Unknown";
            long users = country.getInternetUsers();
            regionUsers.put(region, regionUsers.getOrDefault(region, 0L) + users);
        }

        // Вычисляем общее количество пользователей для процентов
        long totalUsers = regionUsers.values().stream().mapToLong(Long::longValue).sum();

        // Добавляем данные в датасет
        for (java.util.Map.Entry<String, Long> entry : regionUsers.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue().doubleValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Распределение интернет-пользователей по регионам",
            dataset,
            true, true, false
        );

        // Настраиваем отображение меток с процентом и количеством
        PiePlot plot = (PiePlot) chart.getPlot();
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})",  // Регион: Количество (Процент)
            new DecimalFormat("#,##0"),
            new DecimalFormat("0%")
        );
        plot.setLabelGenerator(labelGenerator);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.setMaximumLabelWidth(0.25);  // Ограничиваем ширину метки

        // Сохраняем в PNG файл с увеличенным размером для лучшей читаемости
        saveChartToFile(chart, outputPath, 1200, 800);
    }

    /**
     * Создает bar chart по проценту интернет-пользователей по субрегионам и сохраняет в PNG файл
     *
     * @param countries список субрегионов с данными о процентах (уже сгруппированные)
     * @param outputPath путь для сохранения файла (например, "data/chart_percentage_by_subregion.png")
     */
    public void createPercentageBarChart(List<Country> countries, String outputPath) {
        // Показываем все субрегионы без ограничения
        int limit = countries.size();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < limit; i++) {
            Country country = countries.get(i);
            String subregionName = country.getSubregion() != null ? country.getSubregion() : "Unknown";
            dataset.addValue(country.getPercentage(), "Процент пользователей (%)", subregionName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Процентное соотношение интернет-пользователей по субрегионам",
            "Субрегион",
            "Процент пользователей (%)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        // Настраиваем наклон меток оси X для длинных названий
        org.jfree.chart.plot.CategoryPlot plot = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
        org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);

        // Примечание: Метки на столбцах не работают в JFreeChart 1.5.4 из-за отсутствия методов
        // setBaseItemLabelGenerator, setBaseItemLabelsVisible и т.д. в AbstractCategoryItemRenderer
        // Для отображения процентов можно использовать легенду или подписи под графиком

        // Сохраняем в PNG файл с увеличенным размером для лучшей читаемости
        saveChartToFile(chart, outputPath, 1400, 700);
    }

    /**
     * Вспомогательный метод для сохранения JFreeChart диаграммы в PNG файл
     */
    private void saveChartToFile(JFreeChart chart, String filePath, int width, int height) {
        try (OutputStream os = new FileOutputStream(filePath)) {
            ImageIO.write(chart.createBufferedImage(width, height), "png", os);
            System.out.printf("График сохранен: %s (%dx%d)%n", filePath, width, height);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения графика в файл '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
}

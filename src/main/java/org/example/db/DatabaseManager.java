package org.example.db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер базы данных для управления подключением к SQLite и выполнения операций.
 * Реализует нормализованную схему БД до 3NF (третей нормальной формы).
 * 
 * Схема БД:
 * - regions: id, name (уникальное название региона)
 * - subregions: id, name (уникальное название), region_id (FK к regions)
 * - countries: id, name, subregion_id (FK к subregions), internet_users, population
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/countries.db";
    private Connection connection;
    
    // Кэш для хранения маппинга имен на ID регионов и субрегионов
    private Map<String, Integer> regionCache = new HashMap<>();
    private Map<String, Integer> subregionCache = new HashMap<>();

    public DatabaseManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL);
    }

    /**
     * Создание нормализованных таблиц в БД (3NF)
     */
    public void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Создаем таблицу регионов
            String regionsSql = """
                    CREATE TABLE IF NOT EXISTS regions (
                        id INTEGER PRIMARY KEY,
                        name TEXT UNIQUE NOT NULL
                    )
                    """;
            stmt.execute(regionsSql);

            // Создаем таблицу субрегионов с внешним ключом к регионам
            String subregionsSql = """
                    CREATE TABLE IF NOT EXISTS subregions (
                        id INTEGER PRIMARY KEY,
                        name TEXT UNIQUE NOT NULL,
                        region_id INTEGER REFERENCES regions(id)
                    )
                    """;
            stmt.execute(subregionsSql);

            // Создаем таблицу стран с внешним ключом к субрегионам
            String countriesSql = """
                    CREATE TABLE IF NOT EXISTS countries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        subregion_id INTEGER REFERENCES subregions(id),
                        internet_users BIGINT,
                        population BIGINT
                    )
                    """;
            stmt.execute(countriesSql);

            // Создаем индексы для оптимизации запросов
            String indexSql1 = "CREATE INDEX IF NOT EXISTS idx_countries_subregion ON countries(subregion_id)";
            String indexSql2 = "CREATE INDEX IF NOT EXISTS idx_subregions_region ON subregions(region_id)";
            stmt.execute(indexSql1);
            stmt.execute(indexSql2);
        }
    }

    /**
     * Получение или создание региона по имени (с кэшированием)
     */
    public int getOrCreateRegion(String regionName) throws SQLException {
        if (regionCache.containsKey(regionName)) {
            return regionCache.get(regionName);
        }
        
        String sql = "SELECT id FROM regions WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, regionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    regionCache.put(regionName, id);
                    return id;
                }
            }
        }
        
        // Регион не найден - создаем новый
        String insertSql = "INSERT INTO regions(name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, regionName);
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    regionCache.put(regionName, id);
                    return id;
                } else {
                    throw new SQLException("Получение ID созданного региона не удалось");
                }
            }
        }
    }

    /**
     * Получение или создание субрегиона по имени и региону (с кэшированием)
     */
    public int getOrCreateSubregion(String subregionName, String regionName) throws SQLException {
        // Создаем уникальный ключ для кэша
        String cacheKey = subregionName + "::" + regionName;
        
        if (subregionCache.containsKey(cacheKey)) {
            return subregionCache.get(cacheKey);
        }
        
        int regionId = getOrCreateRegion(regionName);
        
        // Ищем существующий субрегион
        String sql = "SELECT id FROM subregions WHERE name = ? AND region_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subregionName);
            pstmt.setInt(2, regionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    subregionCache.put(cacheKey, id);
                    return id;
                }
            }
        }
        
        // Субрегион не найден - создаем новый
        String insertSql = "INSERT INTO subregions(name, region_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, subregionName);
            pstmt.setInt(2, regionId);
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    subregionCache.put(cacheKey, id);
                    return id;
                } else {
                    throw new SQLException("Получение ID созданного субрегиона не удалось");
                }
            }
        }
    }

    /**
     * Вставка страны в БД с использованием нормализованной схемы
     */
    public int insertCountry(String name, String subregionName, String regionName, long internetUsers, long population) throws SQLException {
        // Получаем или создаем субрегион
        int subregionId = getOrCreateSubregion(subregionName, regionName);
        
        String sql = "INSERT INTO countries(name, subregion_id, internet_users, population) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, subregionId);
            pstmt.setLong(3, internetUsers);
            pstmt.setLong(4, population);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Вставка страны не удалась");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Получение ID вставленной страны не удалось");
                }
            }
        }
    }

    /**
     * Получение соединения с БД
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Закрытие соединения с БД
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    /**
     * Очистка кэша регионов и субрегионов
     */
    public void clearCache() {
        regionCache.clear();
        subregionCache.clear();
    }
}

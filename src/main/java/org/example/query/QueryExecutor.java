package org.example.query;

import org.example.model.Country;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для выполнения SQL запросов к БД стран.
 * Работает с нормализованной схемой (3NF) с использованием JOIN для получения данных о регионах и субрегионах.
 */
public class QueryExecutor {
    
    private final Connection connection;
    
    public QueryExecutor(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Задача 1: Получить данные для построения графика процентного соотношения пользователей в интернете от населения по субрегионам
     */
    public List<Country> getCountriesForChart() throws SQLException {
        String sql = """
                SELECT s.name as subregion,
                       SUM(c.internet_users) as total_internet_users,
                       SUM(c.population) as total_population,
                       CAST(SUM(c.internet_users) AS REAL) * 100 / SUM(c.population) as percentage
                FROM countries c
                JOIN subregions s ON c.subregion_id = s.id
                GROUP BY s.name
                ORDER BY percentage DESC
                """;
        
        List<Country> results = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                Country country = new Country();
                country.setSubregion(rs.getString("subregion"));
                country.setInternetUsers(rs.getLong("total_internet_users"));
                country.setPopulation(rs.getLong("total_population"));
                
                double percentage = rs.getDouble("percentage");
                if (!rs.wasNull()) {
                    country.setPercentage(percentage);
                }
                
                results.add(country);
            }
        }
        
        return results;
    }
    
    /**
     * Задача 2: Выведите название страны с наименьшим кол-вом зарегистрированных в ин-ете пользователей в Восточной Европе
     */
    public Country getCountryWithLowestInternetUsersInEasternEurope() throws SQLException {
        String sql = """
                SELECT c.id, c.name, s.name as subregion, r.name as region,
                       c.internet_users, c.population,
                       CAST(c.internet_users AS REAL) * 100 / c.population as percentage
                FROM countries c
                JOIN subregions s ON c.subregion_id = s.id
                JOIN regions r ON s.region_id = r.id
                WHERE s.name = 'Eastern Europe'
                ORDER BY c.internet_users ASC LIMIT 1
                """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) {
                Country country = new Country();
                country.setId(rs.getInt("id"));
                country.setName(rs.getString("name"));
                country.setSubregion(rs.getString("subregion"));
                country.setRegion(rs.getString("region"));
                country.setInternetUsers(rs.getLong("internet_users"));
                country.setPopulation(rs.getLong("population")); // Добавлено для вычисления процента
                
                double percentage = rs.getDouble("percentage");
                if (!rs.wasNull()) {
                    country.setPercentage(percentage);
                }
                
                return country;
            }
        }
        
        return null;
    }
    
    /**
     * Задача 3: Выведите в консоль название страны процент зарегистрированных в интернете пользователей 
     * которой находится в промежутке от 75% до 85%
     */
    public Country getCountryWithInternetUserPercentageInRange() throws SQLException {
        String sql = """
                SELECT c.id, c.name, r.name as region, s.name as subregion,
                       c.internet_users, c.population,
                       CAST(c.internet_users AS REAL) * 100 / c.population as percentage
                FROM countries c
                JOIN subregions s ON c.subregion_id = s.id
                JOIN regions r ON s.region_id = r.id
                WHERE (CAST(c.internet_users AS REAL) * 100 / c.population) BETWEEN 75 AND 85
                """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) {
                Country country = new Country();
                country.setId(rs.getInt("id"));
                country.setName(rs.getString("name"));
                country.setRegion(rs.getString("region")); // Добавлено для полноты данных
                country.setSubregion(rs.getString("subregion"));
                country.setInternetUsers(rs.getLong("internet_users"));
                country.setPopulation(rs.getLong("population"));
                
                double percentage = rs.getDouble("percentage");
                if (!rs.wasNull()) {
                    country.setPercentage(percentage);
                }
                
                return country;
            }
        }
        
        return null;
    }
    
    /**
     * Получить все страны с информацией о регионах и субрегионах
     */
    public List<Country> getAllCountries() throws SQLException {
        String sql = "SELECT c.id, c.name, r.name as region, s.name as subregion, " +
                     "c.internet_users, c.population," +
                     " CAST(c.internet_users AS REAL) * 100 / c.population as percentage " +
                     "FROM countries c " +
                     "JOIN subregions s ON c.subregion_id = s.id " +
                     "JOIN regions r ON s.region_id = r.id";
        
        List<Country> countries = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                Country country = new Country();
                country.setId(rs.getInt("id"));
                country.setName(rs.getString("name"));
                country.setSubregion(rs.getString("subregion"));
                country.setRegion(rs.getString("region"));
                country.setInternetUsers(rs.getLong("internet_users"));
                country.setPopulation(rs.getLong("population"));
                
                double percentage = rs.getDouble("percentage");
                if (!rs.wasNull()) {
                    country.setPercentage(percentage);
                }
                
                countries.add(country);
            }
        }
        
        return countries;
    }
}

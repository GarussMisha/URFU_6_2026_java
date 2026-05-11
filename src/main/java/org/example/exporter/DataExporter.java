package org.example.exporter;

import org.example.db.DatabaseManager;
import org.example.model.Country;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс для экспорта данных из CSV в БД SQLite.
 */
public class DataExporter {
    
    private final DatabaseManager dbManager;
    
    public DataExporter(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Экспортирует список стран в БД
     * 
     * @param countries список стран для экспорта
     */
    public void exportToDatabase(List<Country> countries) throws SQLException {
        System.out.println("Начало экспорта данных в БД...");
        
        for (Country country : countries) {
            int id = dbManager.insertCountry(
                country.getName(),
                country.getSubregion(),
                country.getRegion(),
                country.getInternetUsers(),
                country.getPopulation()
            );
            
            // Устанавливаем ID для объекта
            country.setId(id);
        }
        
        System.out.println("Экспорт завершен. Всего добавлено стран: " + countries.size());
    }
}

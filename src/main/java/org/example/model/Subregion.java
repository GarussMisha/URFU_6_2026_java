package org.example.model;

/**
 * Модель субрегиона для нормализованной схемы БД (3NF).
 * Субрегион принадлежит региону (связь через foreign key region_id).
 */
public class Subregion {
    private int id;
    private String name;
    private int regionId; // Foreign key к таблице regions

    public Subregion() {
        this.id = -1;
    }

    public Subregion(int id, String name, int regionId) {
        this.id = id;
        this.name = name;
        this.regionId = regionId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getRegionId() { return regionId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRegionId(int regionId) { this.regionId = regionId; }

    @Override
    public String toString() {
        return "Subregion{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionId=" + regionId +
                '}';
    }
}

package org.example.model;

/**
 * Модель региона для нормализованной схемы БД (3NF).
 * Представляет собой сущность Region с уникальным именем.
 */
public class Region {
    private int id;
    private String name;

    public Region() {
        this.id = -1;
    }

    public Region(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

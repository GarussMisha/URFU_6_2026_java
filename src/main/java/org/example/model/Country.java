package org.example.model;

/**
 * Модель страны для хранения показателей стран по интернет-пользователям и населению.
 */
public class Country {
    private int id;
    private String name;
    private String subregion;
    private String region;
    private long internetUsers;
    private long population;
    private double percentage; // Процент пользователей

    public Country() {
        this.id = -1; // Для новых записей, которые еще не сохранены в БД
    }

    public Country(String name, String subregion, String region, long internetUsers, long population) {
        this();
        this.name = name;
        this.subregion = subregion;
        this.region = region;
        this.internetUsers = internetUsers;
        this.population = population;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSubregion() { return subregion; }
    public String getRegion() { return region; }
    public long getInternetUsers() { return internetUsers; }
    public long getPopulation() { return population; }
    public double getPercentage() { return percentage; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSubregion(String subregion) { this.subregion = subregion; }
    public void setRegion(String region) { this.region = region; }
    public void setInternetUsers(long internetUsers) { this.internetUsers = internetUsers; }
    public void setPopulation(long population) { this.population = population; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    @Override
    public String toString() {
        return "Country{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", subregion='" + subregion + '\'' +
                ", region='" + region + '\'' +
                ", internetUsers=" + internetUsers +
                ", population=" + population +
                ", percentage=" + percentage +
                '}';
    }
}

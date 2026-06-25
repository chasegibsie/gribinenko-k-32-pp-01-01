package com.kartonplus.model;

import java.time.LocalDate;

public class RawMaterial {
    private int id;
    private String name;
    private String type;
    private double quantity;
    private int rolls;
    private String supplier;
    private LocalDate lastSupplyDate;
    private double dailyConsumption;

    public RawMaterial() {}

    public RawMaterial(int id, String name, String type, double quantity,
                       int rolls, String supplier, LocalDate lastSupplyDate,
                       double dailyConsumption) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.rolls = rolls;
        this.supplier = supplier;
        this.lastSupplyDate = lastSupplyDate;
        this.dailyConsumption = dailyConsumption;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public int getRolls() { return rolls; }
    public void setRolls(int rolls) { this.rolls = rolls; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public LocalDate getLastSupplyDate() { return lastSupplyDate; }
    public void setLastSupplyDate(LocalDate lastSupplyDate) {
        this.lastSupplyDate = lastSupplyDate;
    }
    public double getDailyConsumption() { return dailyConsumption; }
    public void setDailyConsumption(double dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public int getDaysRemaining() {
        if (dailyConsumption <= 0) return Integer.MAX_VALUE;
        return (int)(quantity / dailyConsumption);
    }

    public boolean isUrgent() {
        return getDaysRemaining() < 2;
    }
}
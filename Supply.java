package com.kartonplus.model;

import java.time.LocalDate;

public class Supply {
    private int id;
    private int materialId;
    private String materialName;
    private double quantity;
    private int rolls;
    private LocalDate supplyDate;
    private String supplier;
    private String documentNumber;

    public Supply() {}

    public Supply(int id, int materialId, String materialName, double quantity,
                  int rolls, LocalDate supplyDate, String supplier, String documentNumber) {
        this.id = id;
        this.materialId = materialId;
        this.materialName = materialName;
        this.quantity = quantity;
        this.rolls = rolls;
        this.supplyDate = supplyDate;
        this.supplier = supplier;
        this.documentNumber = documentNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public int getRolls() { return rolls; }
    public void setRolls(int rolls) { this.rolls = rolls; }
    public LocalDate getSupplyDate() { return supplyDate; }
    public void setSupplyDate(LocalDate supplyDate) { this.supplyDate = supplyDate; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
}
package com.kartonplus.model;

import java.time.LocalDate;

public class Order {
    private int id;
    private String orderNumber;
    private String clientName;
    private String productType;
    private double quantity;
    private String status;
    private LocalDate productionDate;
    private LocalDate shippingDate;
    private double materialConsumption;

    public Order() {}

    public Order(int id, String orderNumber, String clientName, String productType,
                 double quantity, String status, LocalDate productionDate,
                 LocalDate shippingDate, double materialConsumption) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.clientName = clientName;
        this.productType = productType;
        this.quantity = quantity;
        this.status = status;
        this.productionDate = productionDate;
        this.shippingDate = shippingDate;
        this.materialConsumption = materialConsumption;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }
    public LocalDate getShippingDate() { return shippingDate; }
    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }
    public double getMaterialConsumption() { return materialConsumption; }
    public void setMaterialConsumption(double materialConsumption) {
        this.materialConsumption = materialConsumption;
    }
}
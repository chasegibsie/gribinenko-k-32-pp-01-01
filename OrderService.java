package com.kartonplus.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kartonplus.model.Order;
import com.kartonplus.util.HttpClientHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            JsonArray result = HttpClientHelper.get("orders", "order=created_at.desc");

            for (int i = 0; i < result.size(); i++) {
                JsonObject obj = result.get(i).getAsJsonObject();
                Order order = new Order(
                        obj.get("id").getAsInt(),
                        obj.get("order_number").getAsString(),
                        obj.get("client_name").getAsString(),
                        obj.get("product_type").getAsString(),
                        obj.get("quantity").getAsDouble(),
                        obj.get("status").getAsString(),
                        obj.has("production_date") && !obj.get("production_date").isJsonNull() ?
                                LocalDate.parse(obj.get("production_date").getAsString()) : null,
                        obj.has("shipping_date") && !obj.get("shipping_date").isJsonNull() ?
                                LocalDate.parse(obj.get("shipping_date").getAsString()) : null,
                        obj.has("material_consumption") && !obj.get("material_consumption").isJsonNull() ?
                                obj.get("material_consumption").getAsDouble() : 0
                );
                orders.add(order);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order addOrder(Order order) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("order_number", order.getOrderNumber());
            data.addProperty("client_name", order.getClientName());
            data.addProperty("product_type", order.getProductType());
            data.addProperty("quantity", order.getQuantity());
            data.addProperty("status", order.getStatus() != null ? order.getStatus() : "В производстве");
            if (order.getProductionDate() != null) {
                data.addProperty("production_date", order.getProductionDate().toString());
            }
            if (order.getShippingDate() != null) {
                data.addProperty("shipping_date", order.getShippingDate().toString());
            }
            data.addProperty("material_consumption", order.getMaterialConsumption());

            JsonObject result = HttpClientHelper.post("orders", data);
            if (result != null) {
                order.setId(result.get("id").getAsInt());
                return order;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateOrder(Order order) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("order_number", order.getOrderNumber());
            data.addProperty("client_name", order.getClientName());
            data.addProperty("product_type", order.getProductType());
            data.addProperty("quantity", order.getQuantity());
            data.addProperty("status", order.getStatus());
            data.addProperty("material_consumption", order.getMaterialConsumption());
            if (order.getProductionDate() != null) {
                data.addProperty("production_date", order.getProductionDate().toString());
            }
            if (order.getShippingDate() != null) {
                data.addProperty("shipping_date", order.getShippingDate().toString());
            }

            HttpClientHelper.patch("orders", "id=eq." + order.getId(), data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getActiveOrders() {
        List<Order> orders = getAllOrders();
        return orders.stream()
                .filter(o -> "В производстве".equals(o.getStatus()))
                .toList();
    }

    public List<Order> getTodayShipments() {
        List<Order> orders = getAllOrders();
        LocalDate today = LocalDate.now();

        return orders.stream()
                .filter(o -> o.getShippingDate() != null &&
                        (o.getShippingDate().equals(today) ||
                                o.getShippingDate().equals(today.plusDays(1))))
                .limit(5)
                .toList();
    }
}
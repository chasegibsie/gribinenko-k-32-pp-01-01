package com.kartonplus.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kartonplus.model.RawMaterial;
import com.kartonplus.util.HttpClientHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaterialService {

    public List<RawMaterial> getAllMaterials() {
        List<RawMaterial> materials = new ArrayList<>();
        try {
            JsonArray result = HttpClientHelper.get("raw_materials", null);

            for (int i = 0; i < result.size(); i++) {
                JsonObject obj = result.get(i).getAsJsonObject();
                RawMaterial material = new RawMaterial(
                        obj.get("id").getAsInt(),
                        obj.get("name").getAsString(),
                        obj.get("type").getAsString(),
                        obj.get("quantity").getAsDouble(),
                        obj.get("rolls").getAsInt(),
                        obj.has("supplier") && !obj.get("supplier").isJsonNull() ?
                                obj.get("supplier").getAsString() : "",
                        obj.has("last_supply_date") && !obj.get("last_supply_date").isJsonNull() ?
                                LocalDate.parse(obj.get("last_supply_date").getAsString()) : null,
                        obj.get("daily_consumption").getAsDouble()
                );
                materials.add(material);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return materials;
    }

    public RawMaterial addMaterial(RawMaterial material) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("name", material.getName());
            data.addProperty("type", material.getType());
            data.addProperty("quantity", material.getQuantity());
            data.addProperty("rolls", material.getRolls());
            data.addProperty("supplier", material.getSupplier());
            if (material.getLastSupplyDate() != null) {
                data.addProperty("last_supply_date", material.getLastSupplyDate().toString());
            }
            data.addProperty("daily_consumption", material.getDailyConsumption());

            JsonObject result = HttpClientHelper.post("raw_materials", data);
            if (result != null) {
                material.setId(result.get("id").getAsInt());
                return material;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateMaterial(RawMaterial material) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("name", material.getName());
            data.addProperty("type", material.getType());
            data.addProperty("quantity", material.getQuantity());
            data.addProperty("rolls", material.getRolls());
            data.addProperty("supplier", material.getSupplier());
            data.addProperty("daily_consumption", material.getDailyConsumption());

            HttpClientHelper.patch("raw_materials", "id=eq." + material.getId(), data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMaterial(int id) {
        try {
            HttpClientHelper.delete("raw_materials", "id=eq." + id);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalQuantity() {
        List<RawMaterial> materials = getAllMaterials();
        return materials.stream().mapToDouble(RawMaterial::getQuantity).sum();
    }

    public int getTotalRolls() {
        List<RawMaterial> materials = getAllMaterials();
        return materials.stream().mapToInt(RawMaterial::getRolls).sum();
    }

    public List<RawMaterial> getUrgentMaterials() {
        List<RawMaterial> materials = getAllMaterials();
        return materials.stream()
                .filter(RawMaterial::isUrgent)
                .toList();
    }
}
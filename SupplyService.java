package com.kartonplus.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kartonplus.model.Supply;
import com.kartonplus.util.HttpClientHelper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupplyService {

    public List<Supply> getAllSupplies() {
        List<Supply> supplies = new ArrayList<>();
        try {
            JsonArray result = HttpClientHelper.get("supplies", "select=*,raw_materials(name)");

            for (int i = 0; i < result.size(); i++) {
                JsonObject obj = result.get(i).getAsJsonObject();
                Supply supply = new Supply(
                        obj.get("id").getAsInt(),
                        obj.get("material_id").getAsInt(),
                        obj.has("raw_materials") && !obj.get("raw_materials").isJsonNull() ?
                                obj.getAsJsonObject("raw_materials").get("name").getAsString() : "",
                        obj.get("quantity").getAsDouble(),
                        obj.get("rolls").getAsInt(),
                        LocalDate.parse(obj.get("supply_date").getAsString()),
                        obj.has("supplier") && !obj.get("supplier").isJsonNull() ?
                                obj.get("supplier").getAsString() : "",
                        obj.has("document_number") && !obj.get("document_number").isJsonNull() ?
                                obj.get("document_number").getAsString() : ""
                );
                supplies.add(supply);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return supplies;
    }

    public Supply addSupply(Supply supply) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("material_id", supply.getMaterialId());
            data.addProperty("quantity", supply.getQuantity());
            data.addProperty("rolls", supply.getRolls());
            data.addProperty("supply_date", supply.getSupplyDate().toString());
            data.addProperty("supplier", supply.getSupplier());
            data.addProperty("document_number", supply.getDocumentNumber());

            JsonObject result = HttpClientHelper.post("supplies", data);
            if (result != null) {
                supply.setId(result.get("id").getAsInt());
                return supply;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
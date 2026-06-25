package com.kartonplus.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kartonplus.model.User;
import com.kartonplus.util.HttpClientHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {

    public User authenticate(String username, String password) {
        try {
            String query = "username=eq." + username + "&password=eq." + password;
            JsonArray result = HttpClientHelper.get("users", query);

            if (result.size() > 0) {
                JsonObject userJson = result.get(0).getAsJsonObject();
                return new User(
                        userJson.get("id").getAsInt(),
                        userJson.get("username").getAsString(),
                        userJson.get("password").getAsString(),
                        userJson.get("role").getAsString(),
                        userJson.get("full_name").getAsString()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            JsonArray result = HttpClientHelper.get("users", "order=created_at.desc");

            for (int i = 0; i < result.size(); i++) {
                JsonObject userJson = result.get(i).getAsJsonObject();
                User user = new User(
                        userJson.get("id").getAsInt(),
                        userJson.get("username").getAsString(),
                        userJson.get("password").getAsString(),
                        userJson.get("role").getAsString(),
                        userJson.get("full_name").getAsString()
                );
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean addUser(String username, String password, String fullName, String role) {
        try {
            JsonObject userData = new JsonObject();
            userData.addProperty("username", username);
            userData.addProperty("password", password);
            userData.addProperty("full_name", fullName);
            userData.addProperty("role", role);

            JsonObject result = HttpClientHelper.post("users", userData);
            return result != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        try {
            HttpClientHelper.delete("users", "id=eq." + userId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("username", user.getUsername());
            data.addProperty("full_name", user.getFullName());
            data.addProperty("role", user.getRole());

            HttpClientHelper.patch("users", "id=eq." + user.getId(), data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
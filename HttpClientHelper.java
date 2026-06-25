package com.kartonplus.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kartonplus.config.SupabaseConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpClientHelper {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    private static HttpRequest.Builder addHeaders(HttpRequest.Builder builder) {
        return builder
                .header("apikey", SupabaseConfig.SUPABASE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");
    }

    public static JsonArray get(String table, String query) throws IOException {
        try {
            String url = SupabaseConfig.getTableUrl(table);
            if (query != null && !query.isEmpty()) {
                url += "?" + query;
            }

            HttpRequest request = addHeaders(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                if (body == null || body.isEmpty()) {
                    return new JsonArray();
                }
                return gson.fromJson(body, JsonArray.class);
            } else if (response.statusCode() == 404) {
                return new JsonArray();
            } else {
                throw new IOException("HTTP error: " + response.statusCode() + " " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (Exception e) {
            throw new IOException("Network error: " + e.getMessage(), e);
        }
    }

    public static JsonObject post(String table, JsonObject data) throws IOException {
        try {
            HttpRequest request = addHeaders(HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getTableUrl(table)))
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                if (body != null && !body.isEmpty()) {
                    try {
                        JsonArray arr = gson.fromJson(body, JsonArray.class);
                        return arr.size() > 0 ? arr.get(0).getAsJsonObject() : null;
                    } catch (Exception e) {
                        return gson.fromJson(body, JsonObject.class);
                    }
                }
                return null;
            } else {
                throw new IOException("HTTP error: " + response.statusCode() + " " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (Exception e) {
            throw new IOException("Network error: " + e.getMessage(), e);
        }
    }

    public static void patch(String table, String query, JsonObject data) throws IOException {
        try {
            String url = SupabaseConfig.getTableUrl(table);
            if (query != null && !query.isEmpty()) {
                url += "?" + query;
            }

            HttpRequest request = addHeaders(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(data))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP error: " + response.statusCode() + " " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (Exception e) {
            throw new IOException("Network error: " + e.getMessage(), e);
        }
    }

    public static void delete(String table, String query) throws IOException {
        try {
            String url = SupabaseConfig.getTableUrl(table);
            if (query != null && !query.isEmpty()) {
                url += "?" + query;
            }

            HttpRequest request = addHeaders(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .DELETE())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("HTTP error: " + response.statusCode() + " " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (Exception e) {
            throw new IOException("Network error: " + e.getMessage(), e);
        }
    }

    //асинхронные методы для неблокирующих операций
    public static CompletableFuture<JsonArray> getAsync(String table, String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return get(table, query);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
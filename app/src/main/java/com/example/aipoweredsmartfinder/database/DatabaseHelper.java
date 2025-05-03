package com.example.aipoweredsmartfinder.database;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.aipoweredsmartfinder.models.Item;
import com.example.aipoweredsmartfinder.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String SERVER_URL = "http://192.168.234.4/smartfinder/api/"; // Using actual IP address with correct path
    
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Interface for database callbacks
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Login User
    public static void loginUser(String email, String password, DatabaseCallback<User> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SERVER_URL + "login.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON object for login
                JSONObject requestJson = new JSONObject();
                requestJson.put("email", email);
                requestJson.put("password", password);

                // Write to request body
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(requestJson.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONObject userJson = responseJson.getJSONObject("user");
                        User user = new User(
                                userJson.getInt("id"),
                                userJson.getString("username"),
                                userJson.getString("email"),
                                responseJson.getString("token")
                        );
                        
                        mainHandler.post(() -> callback.onSuccess(user));
                    } else {
                        String message = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    // Handle error responses
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error logging in: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Register User
    public static void registerUser(User user, DatabaseCallback<User> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                Log.d(TAG, "Starting registration process...");
                URL url = new URL(SERVER_URL + "register.php");
                Log.d(TAG, "Connecting to URL: " + url.toString());
                
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("username", user.getUsername());
                jsonParams.put("email", user.getEmail());
                jsonParams.put("password", user.getPassword());
                
                Log.d(TAG, "Request data: " + jsonParams.toString());
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonParams.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Server response code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    
                    Log.d(TAG, "Server response: " + response.toString());

                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONObject userJson = responseJson.getJSONObject("user");
                        User registeredUser = new User(
                                userJson.getInt("id"),
                                userJson.getString("username"),
                                userJson.getString("email"),
                                responseJson.getString("token")
                        );
                        
                        mainHandler.post(() -> callback.onSuccess(registeredUser));
                    } else {
                        String message = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    // Handle error responses
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error registering: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }

    // Add a new item (Lost or Found)
    public static void addItem(Item item, String token, String imageBase64, DatabaseCallback<Item> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SERVER_URL + "add_item.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                // Create JSON object for the item
                JSONObject requestJson = new JSONObject();
                requestJson.put("title", item.getTitle());
                requestJson.put("description", item.getDescription());
                requestJson.put("location", item.getLocation());
                requestJson.put("date", item.getDate());
                requestJson.put("category", item.getCategory());
                requestJson.put("type", item.getType());
                requestJson.put("user_id", item.getUserId());
                requestJson.put("image", imageBase64);

                // Write to request body
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(requestJson.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONObject itemJson = responseJson.getJSONObject("item");
                        Item addedItem = new Item(
                                itemJson.getInt("id"),
                                itemJson.getString("title"),
                                itemJson.getString("description"),
                                itemJson.getString("location"),
                                itemJson.getString("date"),
                                itemJson.getString("category"),
                                itemJson.optString("image_path", ""),
                                itemJson.getString("type"),
                                itemJson.getInt("user_id"),
                                itemJson.optString("status", "pending")
                        );
                        
                        mainHandler.post(() -> callback.onSuccess(addedItem));
                    } else {
                        String message = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    // Handle error responses
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding item: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Get items by type (lost or found)
    public static void getItems(String type, String token, DatabaseCallback<List<Item>> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "get_items.php?type=" + type;
                URL url = new URL(SERVER_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONArray itemsJson = responseJson.getJSONArray("items");
                        List<Item> items = new ArrayList<>();
                        
                        for (int i = 0; i < itemsJson.length(); i++) {
                            JSONObject itemJson = itemsJson.getJSONObject(i);
                            Item item = new Item(
                                    itemJson.getInt("id"),
                                    itemJson.getString("title"),
                                    itemJson.getString("description"),
                                    itemJson.getString("location"),
                                    itemJson.getString("date"),
                                    itemJson.getString("category"),
                                    itemJson.optString("image_path", ""),
                                    itemJson.getString("type"),
                                    itemJson.getInt("user_id"),
                                    itemJson.optString("status", "pending")
                            );
                            items.add(item);
                        }
                        
                        mainHandler.post(() -> callback.onSuccess(items));
                    } else {
                        String message = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    // Handle error responses
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting items: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Get a single item details
    public static void getItemDetails(int itemId, String token, DatabaseCallback<Item> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "get_item_details.php?id=" + itemId;
                URL url = new URL(SERVER_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONObject itemJson = responseJson.getJSONObject("item");
                        Item item = new Item(
                                itemJson.getInt("id"),
                                itemJson.getString("title"),
                                itemJson.getString("description"),
                                itemJson.getString("location"),
                                itemJson.getString("date"),
                                itemJson.getString("category"),
                                itemJson.optString("image_path", ""),
                                itemJson.getString("type"),
                                itemJson.getInt("user_id"),
                                itemJson.optString("status", "pending")
                        );
                        
                        mainHandler.post(() -> callback.onSuccess(item));
                    } else {
                        String message = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(message));
                    }
                } else {
                    // Handle error responses
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting item details: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Helper method to convert InputStream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
} 
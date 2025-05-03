package com.example.aipoweredsmartfinder.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.aipoweredsmartfinder.models.Item;
import com.example.aipoweredsmartfinder.models.User;
import com.example.aipoweredsmartfinder.models.ChatMessage;
import com.example.aipoweredsmartfinder.models.Conversation;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {
    private static final String TAG = "ApiClient";
    // Emulator URL
    private static final String BASE_URL = "http://10.0.2.2/smartfinder/api/";
    
    
    //public static final String BASE_URL = "http://192.168.50.4:80/smartfinder/api/";
    
    

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static ApiClient instance;
    private final Context context;

    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    // Interface for API callbacks
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Helper method to create login request JSON
    private static JSONObject createLoginRequest(String email, String password) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("email", email);
        requestJson.put("password", password);
        return requestJson;
    }

    // Helper method to create registration request JSON
    private static JSONObject createRegistrationRequest(User user) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("username", user.getUsername());
        requestJson.put("email", user.getEmail());
        requestJson.put("password", user.getPassword());
        return requestJson;
    }

    // Helper method to create add item request JSON
    private static JSONObject createAddItemRequest(Item item, String imageBase64) throws JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("title", item.getTitle());
        requestJson.put("description", item.getDescription());
        requestJson.put("location", item.getLocation());
        requestJson.put("date", item.getDate());
        requestJson.put("category", item.getCategory());
        requestJson.put("type", item.getType());
        requestJson.put("user_id", item.getUserId());
        requestJson.put("image", imageBase64);
        return requestJson;
    }

    // Login User
    public static void loginUser(String email, String password, ApiCallback<User> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "login.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject requestJson = createLoginRequest(email, password);

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        String status = responseJson.getString("status");
                        
                        if ("success".equals(status)) {
                            JSONObject data = responseJson.getJSONObject("data");
                            User user = new User(
                                    data.getInt("user_id"),
                                    data.getString("username"),
                                    email,
                                    data.getString("token")
                            );
                            
                            mainHandler.post(() -> callback.onSuccess(user));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
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
    public static void registerUser(User user, ApiCallback<User> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "register.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject requestJson = createRegistrationRequest(user);
                Log.d(TAG, "Register request: " + requestJson);

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Register response code: " + responseCode);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(responseCode >= 400 ? connection.getErrorStream() : 
                                connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject responseJson = new JSONObject(response.toString());
                    Log.d(TAG, "Register response: " + responseJson);

                    String status = responseJson.getString("status");
                    
                    if ("success".equals(status)) {
                        JSONObject data = responseJson.getJSONObject("data");
                        User registeredUser = new User(
                                data.getInt("user_id"),
                                data.getString("username"),
                                data.getString("email"),
                                data.getString("token")
                        );
                        
                        mainHandler.post(() -> callback.onSuccess(registeredUser));
                    } else {
                        String errorMessage = responseJson.getString("message");
                        Log.e(TAG, "Register error message: " + errorMessage);
                        mainHandler.post(() -> callback.onError(errorMessage));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error registering: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Add a new item (Lost or Found)
    public static void addItem(Item item, String token, String imageBase64, ApiCallback<Item> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "add_item.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                JSONObject requestJson = createAddItemRequest(item, imageBase64);

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONObject itemJson = responseJson.getJSONObject("item");
                            String imagePath = itemJson.optString("image_path", "");
                            Log.d(TAG, "Image path from server: " + imagePath);
                            
                            Item newItem = new Item(
                                    itemJson.getInt("id"),
                                    itemJson.getString("title"),
                                    itemJson.getString("description"),
                                    itemJson.getString("location"),
                                    itemJson.getString("date"),
                                    itemJson.getString("category"),
                                    imagePath,
                                    itemJson.getString("type"),
                                    itemJson.getInt("user_id"),
                                    itemJson.optString("status", "pending")
                            );
                            
                            mainHandler.post(() -> callback.onSuccess(newItem));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
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

    // Get Items (Lost or Found)
    public void getItems(String type, String token, ApiCallback<List<Item>> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "get_items.php?type=" + type;
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONArray itemsArray = responseJson.getJSONArray("items");
                            List<Item> items = new ArrayList<>();
                            
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject itemJson = itemsArray.getJSONObject(i);
                                String imagePath = itemJson.optString("image_path", "");
                                Log.d(TAG, "Image path from server for item " + i + ": " + imagePath);
                                
                                Item item = new Item(
                                        itemJson.getInt("id"),
                                        itemJson.getString("title"),
                                        itemJson.getString("description"),
                                        itemJson.getString("location"),
                                        itemJson.getString("date"),
                                        itemJson.getString("category"),
                                        imagePath,
                                        itemJson.getString("type"),
                                        itemJson.getInt("user_id"),
                                        itemJson.optString("status", "pending")
                                );
                                items.add(item);
                            }
                            
                            mainHandler.post(() -> callback.onSuccess(items));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
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
    public static void getItemDetails(int itemId, String token, ApiCallback<Item> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "get_item_details.php?id=" + itemId;
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONObject itemJson = responseJson.getJSONObject("item");
                            String imagePath = itemJson.optString("image_path", "");
                            Log.d(TAG, "Image path from server: " + imagePath);
                            
                            Item item = new Item(
                                    itemJson.getInt("id"),
                                    itemJson.getString("title"),
                                    itemJson.getString("description"),
                                    itemJson.getString("location"),
                                    itemJson.getString("date"),
                                    itemJson.getString("category"),
                                    imagePath,
                                    itemJson.getString("type"),
                                    itemJson.getInt("user_id"),
                                    itemJson.optString("status", "pending")
                            );
                            
                            mainHandler.post(() -> callback.onSuccess(item));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
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

    // Get chat messages for an item
    public static void getMessages(int itemId, String token, ApiCallback<List<ChatMessage>> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = "get_messages.php?item_id=" + itemId;
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                Log.d(TAG, "Getting messages for item: " + itemId);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Get messages response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        Log.d(TAG, "Get messages response: " + response.toString());
                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONArray messagesArray = responseJson.getJSONArray("messages");
                            List<ChatMessage> messages = new ArrayList<>();
                            
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject messageJson = messagesArray.getJSONObject(i);
                                ChatMessage message = new ChatMessage(
                                    messageJson.getInt("id"),
                                    messageJson.getInt("item_id"),
                                    messageJson.getInt("sender_id"),
                                    messageJson.getInt("receiver_id"),
                                    messageJson.getString("message"),
                                    messageJson.getInt("is_read") == 1,
                                    messageJson.getString("created_at"),
                                    messageJson.getString("sender_name"),
                                    messageJson.getString("receiver_name"),
                                    messageJson.getString("item_title"),
                                    messageJson.getString("item_type")
                                );
                                messages.add(message);
                            }
                            
                            mainHandler.post(() -> callback.onSuccess(messages));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            Log.e(TAG, "Get messages error: " + errorMessage);
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
                    String errorMessage = "Server returned error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    mainHandler.post(() -> callback.onError(errorMessage));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting messages: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Send a chat message
    public static void sendMessage(int itemId, String message, String token, ApiCallback<ChatMessage> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "send_message.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                JSONObject requestJson = new JSONObject();
                requestJson.put("item_id", itemId);
                requestJson.put("message", message);

                Log.d(TAG, "Sending message request: " + requestJson.toString());

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Send message response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        Log.d(TAG, "Send message response: " + response.toString());
                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONObject messageJson = responseJson.getJSONObject("data");
                            ChatMessage chatMessage = new ChatMessage(
                                messageJson.getInt("id"),
                                messageJson.getInt("item_id"),
                                messageJson.getInt("sender_id"),
                                messageJson.getInt("receiver_id"),
                                messageJson.getString("message"),
                                messageJson.getInt("is_read") == 1,
                                messageJson.getString("created_at"),
                                messageJson.getString("sender_name"),
                                messageJson.getString("receiver_name"),
                                messageJson.getString("item_title"),
                                messageJson.getString("item_type")
                            );
                            
                            mainHandler.post(() -> callback.onSuccess(chatMessage));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            Log.e(TAG, "Send message error: " + errorMessage);
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
                    String errorMessage = "Server returned error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    mainHandler.post(() -> callback.onError(errorMessage));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Get all conversations for the current user
    public static void getConversations(String token, ApiCallback<List<Conversation>> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "get_conversations.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            JSONArray conversationsArray = responseJson.getJSONArray("conversations");
                            List<Conversation> conversations = new ArrayList<>();
                            
                            for (int i = 0; i < conversationsArray.length(); i++) {
                                JSONObject convJson = conversationsArray.getJSONObject(i);
                                Conversation conversation = new Conversation(
                                    convJson.getInt("item_id"),
                                    convJson.getString("item_title"),
                                    convJson.getString("item_type"),
                                    convJson.getString("last_message"),
                                    convJson.getString("last_message_time"),
                                    convJson.getString("other_user_name"),
                                    convJson.getInt("unread_count") > 0
                                );
                                conversations.add(conversation);
                            }
                            
                            mainHandler.post(() -> callback.onSuccess(conversations));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting conversations: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Delete an item
    public static void deleteItem(int itemId, String token, ApiCallback<Void> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "delete_item.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                JSONObject requestJson = new JSONObject();
                requestJson.put("item_id", itemId);

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            mainHandler.post(() -> callback.onSuccess(null));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting item: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Change password
    public static void changePassword(String currentPassword, String newPassword, String token, ApiCallback<Void> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "change_password.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setDoOutput(true);

                JSONObject requestJson = new JSONObject();
                requestJson.put("current_password", currentPassword);
                requestJson.put("new_password", newPassword);

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(requestJson.toString());
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject responseJson = new JSONObject(response.toString());
                        boolean success = responseJson.getBoolean("success");
                        
                        if (success) {
                            mainHandler.post(() -> callback.onSuccess(null));
                        } else {
                            String errorMessage = responseJson.getString("message");
                            mainHandler.post(() -> callback.onError(errorMessage));
                        }
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error changing password: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Helper method to read response
    private static String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    // Search items
    public static void searchItems(String query, String type, String token, ApiCallback<List<Item>> callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint = String.format("search_items.php?query=%s&type=%s",
                    java.net.URLEncoder.encode(query, StandardCharsets.UTF_8.toString()),
                    type);
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(connection.getInputStream());
                    JSONObject responseJson = new JSONObject(response);
                    boolean success = responseJson.getBoolean("success");
                    
                    if (success) {
                        JSONArray itemsArray = responseJson.getJSONArray("items");
                        List<Item> items = new ArrayList<>();
                        
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject itemJson = itemsArray.getJSONObject(i);
                            String imagePath = itemJson.optString("image_path", "");
                            
                            Item item = new Item(
                                itemJson.getInt("id"),
                                itemJson.getString("title"),
                                itemJson.getString("description"),
                                itemJson.getString("location"),
                                itemJson.getString("date"),
                                itemJson.getString("category"),
                                imagePath,
                                itemJson.getString("type"),
                                itemJson.getInt("user_id"),
                                itemJson.optString("status", "pending")
                            );
                            items.add(item);
                        }
                        
                        mainHandler.post(() -> callback.onSuccess(items));
                    } else {
                        String errorMessage = responseJson.getString("message");
                        mainHandler.post(() -> callback.onError(errorMessage));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned error code: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching items: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
} 
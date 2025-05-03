package com.example.aipoweredsmartfinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aipoweredsmartfinder.models.User;

public class SessionManager {
    // Shared preferences constants
    private static final String PREF_NAME = "SmartFinderPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    // Singleton instance
    private static SessionManager instance;
    
    // Constructor
    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    // Get singleton instance
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    // Create login session
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.commit();
    }
    
    // Get stored user data
    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setId(pref.getInt(KEY_USER_ID, 0));
        user.setUsername(pref.getString(KEY_USERNAME, ""));
        user.setEmail(pref.getString(KEY_EMAIL, ""));
        user.setToken(pref.getString(KEY_TOKEN, ""));
        
        return user;
    }
    
    // Check login status
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    // Get auth token
    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }
    
    // Logout user
    public void logout() {
        editor.clear();
        editor.commit();
    }
} 
package com.example.finalyearproject;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPref {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static sharedPref mInstance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private sharedPref (Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized sharedPref getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new sharedPref(context);
        }
        return mInstance;
    }

    public void saveUser(int userId, String username, String email){
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getInt(KEY_USER_ID, -1)!=-1;
    }

    public int getUserId(){
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername(){
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail(){
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void clear(){
        editor.clear();
        editor.apply();
    }
}

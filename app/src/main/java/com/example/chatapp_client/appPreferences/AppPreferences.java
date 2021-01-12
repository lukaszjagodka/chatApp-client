package com.example.chatapp_client.appPreferences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
  public static final String KEY_PREFS_TOKEN = "token";
  public static final String KEY_PREFS_IS_TOKEN = "isToken";
  public static final String KEY_PREFS_NAME= "name";
  public static final String KEY_PREFS_EMAIL= "email";
  public static final String KEY_PREFS_REMEMBER= "remember";
  private static final String APP_SHARED_PREFS = AppPreferences.class.getName();
  private final SharedPreferences _sharedPrefs;
  private final SharedPreferences.Editor _prefsEditor;

  @SuppressLint("CommitPrefEdits")
  public AppPreferences(Context context) {
    this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
    this._prefsEditor = _sharedPrefs.edit();
  }

  public String getToken() {
    return _sharedPrefs.getString(KEY_PREFS_TOKEN, "");
  }

  public void saveToken(String token) {
    _prefsEditor.putString(KEY_PREFS_TOKEN, token);
    _prefsEditor.commit();
  }

  public boolean getIsToken() {
    return _sharedPrefs.getBoolean(KEY_PREFS_IS_TOKEN, false);
  }

  public void saveIsToken(Boolean isToken) {
    _prefsEditor.putBoolean(KEY_PREFS_IS_TOKEN, isToken);
    _prefsEditor.commit();
  }

  public String getName() {
    return _sharedPrefs.getString(KEY_PREFS_NAME, "");
  }

  public void saveName(String name) {
    _prefsEditor.putString(KEY_PREFS_NAME, name);
    _prefsEditor.commit();
  }

  public String getEmail() {
    return _sharedPrefs.getString(KEY_PREFS_EMAIL, "");
  }

  public void saveEmail(String email) {
    _prefsEditor.putString(KEY_PREFS_EMAIL, email);
    _prefsEditor.commit();
  }

  public boolean getRemember() {
    return _sharedPrefs.getBoolean(KEY_PREFS_REMEMBER, false);
  }

  public void saveRemember(Boolean remember) {
    _prefsEditor.putBoolean(KEY_PREFS_REMEMBER, remember);
    _prefsEditor.commit();
  }
}

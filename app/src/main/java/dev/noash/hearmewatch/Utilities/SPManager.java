package dev.noash.hearmewatch.Utilities;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import dev.noash.hearmewatch.Objects.Preference;
import dev.noash.hearmewatch.Objects.PreferenceList;

public class SPManager {
    private static SPManager spManager;
    private static SharedPreferences sp;
    private static final String PREFS_FILE_NAME = "user_prefs";
    private static final String IS_SERVICE_RUNNING_KEY = "is_service_running";
    private static final String PREFERRED_VIBRATION_KEY = "preferred_vibration";
    private static final String USER_NAME_KEY = "user_name";

    public static void init(Context context) {
        if (spManager == null) {
            synchronized (SPManager.class) {
                if (spManager == null) {
                    spManager = new SPManager(context);
                }
            }
        }
    }
    public SPManager(Context context) {
        sp = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static SPManager getInstance() {
        if (spManager == null) {
            throw new IllegalStateException("SPManagaer must be initialized by calling init(context) before use.");
        }
        return spManager;
    }

    public void savePreferencesFromList(PreferenceList preferenceList) {
        SharedPreferences.Editor editor = sp.edit();

        for (Map.Entry<String, Preference> entry : preferenceList.getList().entrySet()) {
            String name = entry.getKey();
            boolean isActive = entry.getValue().getActive();
            editor.putBoolean(name, isActive);
        }

        editor.apply();
    }


    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }
    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }
    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean isServiceRunning() {
        return getBoolean(IS_SERVICE_RUNNING_KEY, false);
    }

    public void setIsServiceRunning(boolean value) {
        setBoolean(IS_SERVICE_RUNNING_KEY, value);
    }

    public String getUserName() {
        return getString(USER_NAME_KEY, "Not Found");
    }
    public void setUserName(String value) {
        setString(USER_NAME_KEY, value);
    }

    public boolean isNotificationEnabled(String type) {
        return getBoolean(type, false); // default : false
    }
    public void setNotificationPreference(String type, boolean isEnabled) {
        setBoolean(type, isEnabled);
    }

    public String getPreferredVibration() {
        return getString(PREFERRED_VIBRATION_KEY, null);
    }

    public void setPreferredVibration(String value) {
        setString(PREFERRED_VIBRATION_KEY, value);
    }

    public void clearAllPreferences() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public void logAllPreferences() {
        Map<String, ?> allPrefs = sp.getAll();
        Log.d("UserPreferences", "---- Current SharedPreferences ----");

        if (allPrefs.isEmpty()) {
            Log.d("UserPreferences", "No preferences found.");
        } else {
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                Log.d("UserPreferences", entry.getKey() + " = " + entry.getValue());
            }
        }
        Log.d("UserPreferences", "----------------------------------");
    }
}

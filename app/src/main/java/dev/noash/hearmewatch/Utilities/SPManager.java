package dev.noash.hearmewatch.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

public class SPManager {
    private static SPManager spManager;
    private static final String PREFS_NAME = "user_prefs";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        if (spManager == null) {
            synchronized (DBManager.class) {
                if (spManager == null) {
                    spManager = new SPManager(context);
                }
            }
        }
    }

    public static SPManager getInstance() {
        if (spManager == null) {
            throw new IllegalStateException("SPManagaer must be initialized by calling init(context) before use.");
        }
        return spManager;
    }

    public SPManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setNotificationPreference(String type, boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(type, isEnabled);
        editor.apply();
    }

    public boolean isNotificationEnabled(String type) {
        return sharedPreferences.getBoolean(type, true); // ברירת מחדל: true
    }

    public void clearAllPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void removePreference(String type) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(type);
        editor.apply();
    }

    public void logAllPreferences() {
        Map<String, ?> allPrefs = sharedPreferences.getAll();
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

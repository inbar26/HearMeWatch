package dev.noash.hearmewatch;

import android.content.Context;
import android.content.SharedPreferences;

public class SPManager {

    private static final String PREFS_FILE_NAME = "user_prefs";
    private static final String PREFERRED_VIBRATION_KEY = "vibration_type";

    private static SPManager spManager;
    private static SharedPreferences sp;

    private SPManager(Context context) {
        sp = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (spManager == null) {
            synchronized (SPManager.class) {
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

    public void setVibrationType(String type) {
        sp.edit().putString(PREFERRED_VIBRATION_KEY, type).apply();
    }

    public String getVibrationType() {
        return sp.getString(PREFERRED_VIBRATION_KEY, "default");
    }
}

package dev.noash.hearmewatch;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.view.Window;

import androidx.core.content.ContextCompat;

import dev.noash.hearmewatch.Models.User;
import dev.noash.hearmewatch.Utilities.DataBaseManager;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataBaseManager.init(this);
    }
    public static void setStatusBar(Window window, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.statusBar));
        }
    }

}

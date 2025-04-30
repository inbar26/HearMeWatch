package dev.noash.hearmewatch;

import android.app.Application;

import dev.noash.hearmewatch.Utilities.DataBaseManager;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataBaseManager.init(this);
    }
}

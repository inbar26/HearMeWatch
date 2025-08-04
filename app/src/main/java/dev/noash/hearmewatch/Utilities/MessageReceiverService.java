package dev.noash.hearmewatch.Utilities;

import com.google.android.gms.wearable.WearableListenerService;

import android.util.Log;
import com.google.gson.Gson;
import android.os.Environment;
import android.content.Context;
import com.google.android.gms.wearable.MessageEvent;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;


public class MessageReceiverService extends WearableListenerService {
    private static final String PATH_ACK = "/acknowledge_notification";

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals(PATH_ACK)) {
            long receivedAt = System.currentTimeMillis();

            // Saves to json file
            saveReceiveTimeToJson(receivedAt);
        }
    }

    public void saveReceiveTimeToJson(long timestamp) {
        // Updates time stamp format
        String formattedReceiveTime = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date(timestamp));

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file  = new File(downloadsDir, "log_receive.json");

            // Values map
            Map<String, Object> data = new HashMap<>();
            data.put("receive_time", formattedReceiveTime);
            data.put("timestamp", timestamp);

            // Write to file
            FileWriter writer = new FileWriter(file, true);
            writer.write(new Gson().toJson(data) + "\n");
            writer.close();

            Log.d("LOG", "📄 Receive time saved to file: " + formattedReceiveTime);
        } catch (Exception e) {
            Log.e("LOG", "❌ Receive time Failed to save", e);
        }
    }
}

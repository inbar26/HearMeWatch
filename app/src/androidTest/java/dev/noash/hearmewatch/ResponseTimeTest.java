package dev.noash.hearmewatch;

import android.util.Log;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.io.BufferedReader;


public class ResponseTimeTest {

    private static final String SEND_FILE = "log_send.json";
    private static final String RECEIVE_FILE = "log_receive.json";

    @Test
    public void testResponseTimes() throws Exception {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File sendFile = new File(downloadsDir, SEND_FILE);
        File receiveFile = new File(downloadsDir, RECEIVE_FILE);

        List<Long> sendTimestamps = loadTimestamps(sendFile);
        List<Long> receiveTimestamps = loadTimestamps(receiveFile);

        if (sendTimestamps.size() != receiveTimestamps.size()) {
            Log.w("RESPONSE_TEST", "⚠️ Number of sends and receives doesn't match exactly");
        }

        int matched = Math.min(sendTimestamps.size(), receiveTimestamps.size());
        int aboveThreshold = 0;
        long totalDelay = 0;

        for (int i = 0; i < matched; i++) {
            long delay = receiveTimestamps.get(i) - sendTimestamps.get(i);
            totalDelay += delay;
            if (delay > 1000) {
                aboveThreshold++;
            }
            Log.i("RESPONSE_TEST", "🕒 Response " + i + ": " + delay + " ms");
        }

        double avg = matched > 0 ? totalDelay / (double) matched : 0;
        Log.i("RESPONSE_TEST", "\n✅ Total Messages: " + matched +
                "\n⏱️ Average Delay: " + avg + " ms" +
                "\n🚨 Above 1s: " + aboveThreshold + " / " + matched);
    }

    private List<Long> loadTimestamps(File file) {
        List<Long> timestamps = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            while ((line = reader.readLine()) != null) {
                Map<String, Object> entry = gson.fromJson(line, mapType);
                Double ts = (Double) entry.get("timestamp");
                if (ts != null) {
                    timestamps.add(ts.longValue());
                }
            }
            Log.d("RESPONSE_TEST", "✅ Loaded " + timestamps.size() + " send timestamps");
        } catch (Exception e) {
            Log.e("RESPONSE_TEST", "❌ Failed to load send log", e);
        }
        return timestamps;
    }
}

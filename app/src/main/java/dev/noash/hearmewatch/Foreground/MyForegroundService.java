package dev.noash.hearmewatch.Foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

import dev.noash.hearmewatch.Utilities.WavEncoder;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Handler handler;
    private Runnable runnable;
    private AudioRecord audioRecord;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SERVICE", "onStartCommand called");
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Listening Service")
                .setContentText("Listening to environment...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        startForeground(1, notification);
        startRecording();

        return START_STICKY;
    }

    private void startRecording() {
        if (isRecording) {
            Log.d("SERVICE", "Recording already in progress, skipping startRecording()");
            return;
        }
        int sampleRate = 16000;
        int bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        audioRecord.startRecording();
        isRecording = true;

        new Thread(() -> {
            while (isRecording) {
                try {
                    // One second call (at 16KHz with 2 bytes per sample = 32000 bytes)
                    int bytesPerSecond = sampleRate * 2;
                    byte[] buffer = new byte[bytesPerSecond];
                    int read = audioRecord.read(buffer, 0, buffer.length);

                    if (read > 0) {
                        saveBufferAsWav(buffer, read, sampleRate);
                    }

                    Thread.sleep(1000); // לחכות שנייה בין הקלטות

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void saveBufferAsWav(byte[] data, int length, int sampleRate) {
        try {
            File outputDir = getExternalFilesDir(null);
            String fileName = "audio_" + System.currentTimeMillis() + ".wav";
            File outputFile = new File(outputDir, fileName);

            byte[] trimmed = new byte[length];
            System.arraycopy(data, 0, trimmed, 0, length);

            //Log.d("SERVICE", "Saving file: " + outputFile.getAbsolutePath());
            WavEncoder.saveAsWav(trimmed, sampleRate, outputFile);

            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(this, "הוקלט:\n" + fileName, Toast.LENGTH_SHORT).show()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind (Intent intent){
        return null; //no connection between activity and service
    }

    private void createNotificationChannel () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("SERVICE", "onDestroy called");
        isRecording = false;

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                Log.e("SERVICE", "audioRecord.stop() failed: " + e.getMessage());
            }

            try {
                audioRecord.release();
            } catch (Exception e) {
                Log.e("SERVICE", "audioRecord.release() failed: " + e.getMessage());
            }

            audioRecord = null;
        }

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        stopSelf();
    }
}

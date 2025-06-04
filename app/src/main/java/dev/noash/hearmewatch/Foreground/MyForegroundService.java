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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import dev.noash.hearmewatch.EdgeImpulseProcessor;
import dev.noash.hearmewatch.ModelHelper;
import dev.noash.hearmewatch.YamnetRunner;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Handler handler;
    private AudioRecord audioRecord;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        try {
            ModelHelper.initializeModels(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("SERVICE", "Foreground service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Listening Service")
                .setContentText("Listening to environment...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        startForeground(1, notification);

        Log.d("SERVICE", "onStartCommand called");

        startRecording();

        return START_STICKY;
    }


    private void startRecording() {
        if (isRecording) {
            Log.d("SERVICE", "Recording already in progress");
            return;
        }

        final int sampleRate = 16000;
        final int targetBytes = sampleRate * 2; // 16000 samples * 2 bytes = 32000 bytes

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SERVICE", "RECORD_AUDIO permission not granted");
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

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
            ByteBuffer fullBuffer = ByteBuffer.allocateDirect(targetBytes).order(ByteOrder.LITTLE_ENDIAN);

            while (isRecording) {
                ByteBuffer tempBuffer = ByteBuffer.allocateDirect(2048).order(ByteOrder.LITTLE_ENDIAN);
                int read = audioRecord.read(tempBuffer, 2048);

                if (read > 0) {
                    tempBuffer.rewind();
                    while (tempBuffer.hasRemaining() && fullBuffer.hasRemaining()) {
                        fullBuffer.put(tempBuffer.get());
                    }

                    if (!fullBuffer.hasRemaining()) {
                        try {
                            fullBuffer.rewind();
                            float[] inputEI = new float[targetBytes / 2];
                            for (int i = 0; i < inputEI.length; i++) {
                                inputEI[i] = fullBuffer.getShort() / 32768f;
                            }
                            String resultEI = EdgeImpulseProcessor.runAudioInference(inputEI);

                            fullBuffer.rewind();
                            String resultYAM = YamnetRunner.runOnBuffer(this, fullBuffer);

                            Log.d("EdgeImpulse", "Result: " + resultEI);
                            Log.d("YAMNet", "Detected: " + resultYAM);

                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(this, "EI: " + resultEI + "\nYAM: " + resultYAM, Toast.LENGTH_SHORT).show()
                            );
                        } catch (Exception e) {
                            Log.e("SERVICE", "Error during inference: " + e.getMessage());
                        }

                        fullBuffer.clear();
                    }
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
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

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        stopSelf();
    }
}

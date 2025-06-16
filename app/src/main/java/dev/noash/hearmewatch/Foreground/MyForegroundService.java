package dev.noash.hearmewatch.Foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
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

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;

import dev.noash.hearmewatch.EdgeImpulseProcessor;
import dev.noash.hearmewatch.ModelHelper;
import dev.noash.hearmewatch.Models.MyPreference;
import dev.noash.hearmewatch.Models.User;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.YamnetRunner;

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
        final int targetBytes = sampleRate * 2;

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
                            List<String> resultYAMLabels = YamnetRunner.runOnBuffer(this, fullBuffer);


                            Log.d("EdgeImpulse", "Result: " + resultEI);
                            Log.d("YAMNet", "Detected labels: " + resultYAMLabels);

                            //this function works for only one category that called - speech
//                            if (resultYAM.toLowerCase().contains("dog")) {
//                                sendMessageToWatch(this, resultYAM);
//                            }
                            SPManager spManager = SPManager.getInstance();
                            for (String label : resultYAMLabels) {
                                if (spManager.isNotificationEnabled(label)) {
                                    Log.d("PREFERENCE_MATCH", "🎯 Match found in SharedPreferences: " + label);
                                    sendMessageToWatch(this, label);
                                } else {
                                    Log.d("PREFERENCE_CHECK", "⛔ " + label + " is not enabled in preferences");
                                }
                            }
                            //

                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(this, "EI: " + resultEI + "\nYAM: " + resultYAMLabels, Toast.LENGTH_SHORT).show()

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

    private void sendMessageToWatch(Context context, String message) {
        Log.d("SEND_TO_WATCH", "📤 Preparing to send message: " + message);

        Wearable.getNodeClient(context).getConnectedNodes().addOnSuccessListener(nodes -> {
            if (nodes.isEmpty()) {
                Log.e("SEND_TO_WATCH", "❌ No connected nodes — cannot send message!");
            } else {
                for (com.google.android.gms.wearable.Node node : nodes) {
                    Log.d("SEND_TO_WATCH", "✅ Connected to node: " + node.getDisplayName() + " (" + node.getId() + ")");
                    Wearable.getMessageClient(context).sendMessage(
                            node.getId(),
                            "/sound_alert",
                            message.getBytes()
                    ).addOnSuccessListener(aVoid -> {
                        Log.d("SEND_TO_WATCH", "✅ Message sent to " + node.getDisplayName());
                    }).addOnFailureListener(e -> {
                        Log.e("SEND_TO_WATCH", "❌ Failed to send message: " + e.getMessage());
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("SEND_TO_WATCH", "❌ Failed to get connected nodes: " + e.getMessage());
        });
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

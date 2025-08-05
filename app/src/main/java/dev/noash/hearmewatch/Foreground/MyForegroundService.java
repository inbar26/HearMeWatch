package dev.noash.hearmewatch.Foreground;

import android.util.Log;
import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.content.Context;
import android.app.Notification;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.android.gms.wearable.Wearable;


import java.io.File;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.io.FileWriter;
import java.nio.ByteOrder;
import java.util.TimeZone;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.text.SimpleDateFormat;

import dev.noash.hearmewatch.ModelHelper;
import dev.noash.hearmewatch.Objects.NameLabel;
import dev.noash.hearmewatch.YamnetRunner;
import dev.noash.hearmewatch.Objects.SoundLabel;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.EdgeImpulseProcessor;

public class MyForegroundService extends Service {

    private AudioRecord audioRecord;
    private Handler handler;
    private boolean isRecording = false;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String BASE_SOUND_MESSAGE = "A sound was detected nearby \n";
    public static final String PATH_SOUND_NOTIFICATION = "/sound_alert";
    private static final long NOTIFICATION_EXPIRY_DURATION = 10_000L;
    public static HashMap<String, Long> notificationTimestamps = new HashMap<>();

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

                            // Run inference using Edge Impulse and YAMNet models
                            String resultEI = EdgeImpulseProcessor.runAudioInference(inputEI);
                            fullBuffer.rewind(); // Reset buffer before YAMNet inference
                            List<String> resultYAMLabels = YamnetRunner.runOnBuffer(this, fullBuffer);

                            // Log raw model results
                            Log.d("EdgeImpulse", "Result: " + resultEI);
                            Log.d("YAMNet", "Detected labels: " + resultYAMLabels);

                            // Handle Name Calling option (Edge impulse)
                            String topEI_Label = extractTopLabel(resultEI);// Extract the top label from Edge Impulse output
                            String userName = SPManager.getInstance().getUserName();
                            handleNameCallingNotification(this, topEI_Label, userName);

                            // Handle sounds results (YAMNet)
                            for (String label : resultYAMLabels) {
                                switch (label) {
                                    case "Dog":
                                    case "Bark":
                                    case "Whimper (dog)":
                                        handleSoundLabelNotification(this, SoundLabel.DOG_BARKING);
                                        break;
                                    case "Baby cry, infant cry":
                                    case "Whimper":
                                    case "Crying, sobbing":
                                        handleSoundLabelNotification(this, SoundLabel.BABY_CRYING);
                                        break;

                                    case "Ambulance (siren)":
                                    case "Emergency vehicle":
                                    case "Police car (siren)":
                                        handleSoundLabelNotification(this, SoundLabel.EMERGENCY_VEHICLE);
                                        break;

                                    case "Car alarm":
                                    case "Toot":
                                    case "Vehicle horn, car horn, honking":
                                        handleSoundLabelNotification(this, SoundLabel.CAR_HORN);
                                        break;

                                    case "Knock":
                                    case "Door":
                                        handleSoundLabelNotification(this, SoundLabel.DOOR_KNOCK);
                                        break;

                                    case "Doorbell":
                                    case "Ding-dong":
                                    case "Bell":
                                    case "Jingle bell":
                                    case "Ding":
                                        handleSoundLabelNotification(this, SoundLabel.INTERCOM);
                                       break;

                                    case "Fire engine, fire truck (siren)":
                                    case "Fire alarm":
                                    case "Smoke detector, smoke alarm":
                                        handleSoundLabelNotification(this, SoundLabel.FIRE_ALARM);
                                        break;

                                    default:
                                        // do nothing
                                        break;
                                }
                            }

                        } catch (Exception e) {
                            Log.e("SERVICE", "Error during inference: " + e.getMessage());
                        }

                        fullBuffer.clear();
                    }
                }
            }
        }).start();
    }

    private void handleSoundLabelNotification(Context context, SoundLabel soundLabel) {
        String displayName = SoundLabel.getDisplayName(soundLabel);

        long now = System.currentTimeMillis();
        Long lastTime = notificationTimestamps.get(displayName);

        boolean shouldNotify = ((lastTime == null || (now - lastTime > NOTIFICATION_EXPIRY_DURATION)) &&
                SPManager.getInstance().isNotificationEnabled(displayName));

        if (shouldNotify) {
            notificationTimestamps.put(displayName, now);
            Log.d("SOUND_MATCH", displayName);
            sendMessageToWatch(context, BASE_SOUND_MESSAGE + displayName);
        } else {
            if (lastTime != null) {
                Log.d("SOUND_SKIP", "Skipped " + displayName + " – " + (now - lastTime) + "ms since last notification");
            } else {
                Log.d("SOUND_SKIP", "Skipped " + displayName + " – not enabled in SharedPreferences");
            }
        }
    }

    private void handleNameCallingNotification(Context context, String topLabel, String userName) {
        String displayName = SoundLabel.getDisplayName(SoundLabel.NAME_CALLING);

        long now = System.currentTimeMillis();
        Long lastTime = notificationTimestamps.get(displayName);

        boolean shouldNotify = (
                topLabel != null &&
                userName != null &&
                !userName.equals("Not Found") &&
                topLabel.toLowerCase().contains(userName.toLowerCase()) &&
                SPManager.getInstance().isNotificationEnabled(displayName));

        if (shouldNotify) {
            notificationTimestamps.put(displayName, now);
            Log.d("NAME_MATCH", userName);
            sendMessageToWatch(context, "Your name\n" + userName + "\nwas called");
        } else {
            if (lastTime != null) {
                Log.d("NAME_SKIP", "Skipped NAME_CALLING – " + (now - lastTime) + "ms since last");
            } else {
                Log.d("NAME_SKIP", "NAME_CALLING not triggered – either disabled or not matched");
            }
        }
    }

    private void sendMessageToWatch(Context context, String message) {
        long sendAt = System.currentTimeMillis();
        saveSendTimeToJson(sendAt);

        Wearable.getNodeClient(context).getConnectedNodes().addOnSuccessListener(nodes -> {
            if (nodes.isEmpty()) {
                Log.e("SEND_TO_WATCH", "❌ No connected nodes — cannot send message!");
            } else {
                for (com.google.android.gms.wearable.Node node : nodes) {
                    Wearable.getMessageClient(context).sendMessage(
                            node.getId(),
                            PATH_SOUND_NOTIFICATION,
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

    public void saveSendTimeToJson(long timestamp) {
        // Updates time stamp format
        String formattedSendTime = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date(timestamp));

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file  = new File(downloadsDir, "log_send.json");

            // Values map
            Map<String, Object> data = new HashMap<>();
            data.put("send_time", formattedSendTime);
            data.put("timestamp", timestamp);

            // Write to file
            FileWriter writer = new FileWriter(file, true);
            writer.write(new Gson().toJson(data) + "\n");
            writer.close();

            Log.d("LOG", "📄 Send time saved to file: " + formattedSendTime);
        } catch (Exception e) {
            Log.e("LOG", "❌ Send time Failed to save", e);
        }
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Extract the top label (highest percentage) from the Edge Impulse result string
    private String extractTopLabel(String resultText) {
        String[] lines = resultText.split("\n");
        String topLabel = null;
        float maxConfidence = -1f;

        for (String line : lines) {
            // Skip invalid or non-label lines
            if (line.trim().isEmpty() || !line.contains(":") || line.toLowerCase().startsWith("result")) {
                continue;
            }

            try {
                String[] parts = line.split(":");
                String label = parts[0].trim();
                String percentStr = parts[1].replace("%", "").trim();
                float confidence = Float.parseFloat(percentStr);

                if (confidence > maxConfidence) {
                    maxConfidence = confidence;
                    topLabel = label;
                }
            } catch (Exception e) {
                Log.e("PARSE_ERROR", "Error parsing line: " + line);
            }
        }

        if (topLabel != null) {
            NameLabel nameLabel = NameLabel.fromLabel(topLabel.toLowerCase());

            if (nameLabel != null) {
                float requiredConfidence = nameLabel.getMinConfidence();
                if (maxConfidence >= requiredConfidence) {
                    Log.d("EdgeImpulse", "Top label: " + topLabel + ". Confidence: " + maxConfidence);
                    return topLabel;
                } else {
                    Log.d("EdgeImpulse", "Confidence too low for " + topLabel + ": " + maxConfidence + " < " + requiredConfidence);
                }
            } else {
                Log.w("EdgeImpulse", "Unknown label: " + topLabel);
            }
        }

        return null;

//        if(maxConfidence > 50f) { //Send only if there is a high recognition rate (above 60%)
//            Log.d("EdgeImpulse", "Top label: " + topLabel +" Confidence: "+maxConfidence);
//            return topLabel;
//        }
//        return null;
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

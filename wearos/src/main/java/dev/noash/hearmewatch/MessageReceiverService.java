package dev.noash.hearmewatch;

import android.util.Log;
import android.os.Build;
import android.os.Vibrator;
import android.content.Intent;
import android.content.Context;
import android.app.PendingIntent;
import android.os.VibrationEffect;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class MessageReceiverService extends WearableListenerService {

    private static final String CHANNEL_ID = "sound_alert_channel";
    private static final String TAG = "MessageReceiverService";


    @Override
    public void onCreate() {
        super.onCreate();
        SPManager.init(this);
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d(TAG, "Message received in service with path: " + event.getPath());

        if ("/sound_alert".equals(event.getPath())) { //Detected
            String message = new String(event.getData());
            showNotification("Sound Detected", message);
        } else if ("/update_vibration".equals(event.getPath())) { //Update vibration type
            String type = new String(event.getData(), StandardCharsets.UTF_8);
            SPManager.getInstance().setVibrationType(type);
            Log.d("WATCH", "✅ vibration_type updated to: " + type);

        }
    }

    private void showNotification(String title, CharSequence text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String vib = SPManager.getInstance().getVibrationType();
        long[] vibrationPattern = VibrationPattern.getPatternByName(vib);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));
            } else {
                vibrator.vibrate(vibrationPattern, -1);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1002, builder.build());
        sendAckToPhone(text.toString());
    }

    private void sendAckToPhone(String originalMessage) {
        String ackMessage = "ACK: " + originalMessage;
        String path = "/acknowledge_notification";

        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
                for (Node node : nodes) {
                    Tasks.await(Wearable.getMessageClient(this).sendMessage(
                            node.getId(),
                            path,
                            ackMessage.getBytes(StandardCharsets.UTF_8)
                    ));
                    Log.d("WATCH_ACK", "✅ Ack sent to phone: " + ackMessage);
                }
            } catch (Exception e) {
                Log.e("WATCH_ACK", "❌ Failed to send ack", e);
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sound Alerts";
            String description = "Alerts for detected sounds";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

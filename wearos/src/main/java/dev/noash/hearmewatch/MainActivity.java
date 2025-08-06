package dev.noash.hearmewatch;

import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.MessageClient;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {

    private static final String CHANNEL_ID = "sound_alert_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Wearable.getMessageClient(this).addListener(this);
        createNotificationChannel();
        Log.d("WATCH", "Listener registered in MainActivity");
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent event) {
        Log.d("WATCH_RECEIVE", "Message received with path: " + event.getPath());

        if ("/sound_alert".equals(event.getPath())) {
            final String receivedMessage = new String(event.getData());

            runOnUiThread(() -> {
                TextView tv = findViewById(R.id.tvSoundDetected);
                tv.setText(receivedMessage);
                showNotification("Sound Detected", receivedMessage);
            });
        }
    }

    private void showNotification(String title, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.getMessageClient(this).removeListener(this);
    }
}

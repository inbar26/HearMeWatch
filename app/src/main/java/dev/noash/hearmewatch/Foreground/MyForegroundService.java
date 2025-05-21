package dev.noash.hearmewatch.Foreground;

import android.Manifest;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Handler handler;
    private Runnable runnable;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private int bufferSize;
    private byte[] audioBuffer;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
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
        startRecording();
        //startListening();

        return START_STICKY;
    }

    private void startRecording() {
        bufferSize = AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            audioBuffer = new byte[bufferSize];

            audioRecord.startRecording();
            isRecording = true;

            new Thread(() -> {
                while (isRecording) {
                    int read = audioRecord.read(audioBuffer, 0, bufferSize);
                    if (read > 0) {
                        handleAudioData(audioBuffer, read);
                    }
                }
            }).start();
        }

    }

        private void handleAudioData ( byte[] data, int length){
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(MyForegroundService.this, "נקלטו " + length + " בייטים של אודיו", Toast.LENGTH_SHORT).show();
            });
        }


        @Nullable
        @Override
        public IBinder onBind (Intent intent){
            return null;
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
        super.onDestroy();
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        handler.removeCallbacks(runnable);
    }

//        private void startListening () {
//            runnable = new Runnable() {
//                @Override
//                public void run() {
//                    // כאן תכתבי את הלוגיקה של קליטת שמע
//                    // בינתיים לצורך הדוגמה - נציג Toast
//                    Toast.makeText(MyForegroundService.this, "שמעתי משהו!", Toast.LENGTH_SHORT).show();
//
//                    // נקבע לקרוא לעצמנו שוב בעוד 5 שניות
//                    handler.postDelayed(this, 5000);
//                }
//            };
//
//            handler.post(runnable);
//        }
}

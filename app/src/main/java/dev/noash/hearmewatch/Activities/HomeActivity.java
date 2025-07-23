package dev.noash.hearmewatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.Utilities.DrawerManager;
import dev.noash.hearmewatch.Foreground.MyForegroundService;


public class HomeActivity extends AppCompatActivity {

    private MaterialButton startRecordingBtn;
    private MaterialButton stopRecordingBtn;
    private TextView title;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_drawer);
        LayoutInflater.from(this).inflate(R.layout.activity_home, findViewById(R.id.content_container), true); // 2. טוען את התוכן הספציפי

        findViews();
        initViews();;
    }

    private void findViews() {
        title = findViewById(R.id.TV_page_title);
        startRecordingBtn = findViewById(R.id.BTN_start_detection);
     //   stopRecordingBtn = findViewById(R.id.BTN_stop_recording);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initDrawer();
        initHeader();

        if(SPManager.getInstance().isServiceRunning()) {
            //   setButtonMode(startRecordingBtn, ContextCompat.getColor(this, R.color.buttons_disabled), false);
        }

        startRecordingBtn.setOnClickListener(v -> startMyService());
   //     stopRecordingBtn.setOnClickListener(v -> stopMyService());
    }

    private void initDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navView = findViewById(R.id.navigation_view);
        DrawerManager.setupDrawer(this, drawerLayout, toolbar, navView);

        View headerView = navView.getHeaderView(0);
        String userName = DBManager.getInstance().getUser().getName();
        String userEmail = DBManager.getInstance().getUser().getEmail();
        DrawerManager.updateUserCard(navView, headerView, userName, userEmail);
    }

    private void initHeader() {
        title = findViewById(R.id.TV_page_title);
        setHelloMessage();

        TextView subtitle = findViewById(R.id.TV_page_subtitle);
        subtitle.setText("Ready to detect sounds?");
    }


    private void setHelloMessage() {
        String fName = DBManager.getInstance().getUser().getfName();
        if(fName == null || fName.isEmpty())
            title.setText("Hello !");
        else {
            title.setText("Hello, " + fName + "!");
        }
    }

    private void setButtonMode(MaterialButton button, int color, boolean isEnabled) {
        button.setBackgroundColor(color);
        button.setClickable(isEnabled);
    }

    private void startMyService() {
        if (!checkMicrophonePermission()) {
            requestMicrophonePermission();
        } else {
            startMyForegroundService();
        }
    }
    private boolean checkMicrophonePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void requestMicrophonePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void startMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //checking version
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        SPManager.getInstance().setIsServiceRunning(true);
       // setButtonMode(startRecordingBtn, ContextCompat.getColor(this, R.color.buttons_disabled), false);
    }

    private void stopMyService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);

        if(!SPManager.getInstance().isServiceRunning()) {
            Toast.makeText(this, "No recording in progress.", Toast.LENGTH_SHORT).show();
        }
        else {
            SPManager.getInstance().setIsServiceRunning(false);
            setButtonMode(startRecordingBtn, ContextCompat.getColor(this, R.color.buttons),true);
            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMyForegroundService();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

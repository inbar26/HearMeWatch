package dev.noash.hearmewatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;


import dev.noash.hearmewatch.Foreground.MyForegroundService;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Utilities.DBManager;
//import dev.noash.hearmewatch.Utilities.MessageSender;
import dev.noash.hearmewatch.Utilities.SPManager;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView helloMessage;
    private MaterialButton startRecordingBtn;
    private MaterialButton stopRecordingBtn;
    private MaterialButton connectWatchBtn;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViews();
        initViews();
        SPManager.init(getApplicationContext());
        SPManager.getInstance().logAllPreferences();

//        Wearable.getNodeClient(getApplicationContext()).getConnectedNodes()
//                .addOnSuccessListener(nodes -> {
//                    for (Node node : nodes) {
//                        Log.d("WATCH111", "Connected node: " + node.getDisplayName());
//                    }
//                });
    }
    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        setSupportActionBar(toolbar);
        menuManagement();
        setHelloMessage();
        startRecordingBtn.setOnClickListener(v -> startMyService());
        stopRecordingBtn.setOnClickListener(v -> stopMyService());
        connectWatchBtn.setOnClickListener(v -> connectWatch());

    }

    private void setHelloMessage() {
        String firstName;
        firstName = DBManager.getUser().getName();
        if(firstName == null)
            helloMessage.setText("Hello");
        else {
            helloMessage.setText("Hello " + firstName.split(" ")[0]);
        }

    }

    private void menuManagement() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_preferences) {
                    moveToPreferencesPage();
                }
                if (id == R.id.nav_profile) {
                    moveToProfilePage();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void moveToProfilePage() {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
        finish();
    }

    private void findViews() {
        drawerLayout = findViewById(R.id.home_drawer_layout);
        navigationView = findViewById(R.id.home_navigation_view);
        toolbar = findViewById(R.id.home_toolbar);
        helloMessage = findViewById(R.id.TV_hello_message);
        startRecordingBtn = findViewById(R.id.BTN_start_recording);
        stopRecordingBtn = findViewById(R.id.BTN_stop_recording);
        connectWatchBtn = findViewById(R.id.BTN_connect_watch);
    }
    private void moveToPreferencesPage() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
        finish();
    }

    private void connectWatch() {

    }


    private void startMyService() {
        if (!checkMicrophonePermission()) {
            requestMicrophonePermission();
        } else {
            startRecordingBtn.setBackgroundColor(Color.parseColor("#77BABA"));
            startRecordingBtn.setClickable(false);
            startMyForegroundService();
        }
    }

    private void stopMyService() {
        startRecordingBtn.setBackgroundColor(Color.parseColor("#1F4F4F"));
        startRecordingBtn.setClickable(true);
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);
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

    private void startMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //checking version
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
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

}

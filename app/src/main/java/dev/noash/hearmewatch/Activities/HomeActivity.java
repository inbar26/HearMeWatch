package dev.noash.hearmewatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.Foreground.MyForegroundService;


public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView helloMessage;
    private MaterialButton startRecordingBtn;
    private MaterialButton stopRecordingBtn;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViews();
        initViews();;
    }

    private void findViews() {
        drawerLayout = findViewById(R.id.home_drawer_layout);
        navigationView = findViewById(R.id.home_navigation_view);
        toolbar = findViewById(R.id.home_toolbar);
        helloMessage = findViewById(R.id.TV_hello_message);
        startRecordingBtn = findViewById(R.id.BTN_start_recording);
        stopRecordingBtn = findViewById(R.id.BTN_stop_recording);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        setSupportActionBar(toolbar);
        setHelloMessage();
        menuManagement();

        if(SPManager.getInstance().isServiceRunning())
            setButtonMode(startRecordingBtn, ContextCompat.getColor(this, R.color.buttons_disabled), false);

        startRecordingBtn.setOnClickListener(v -> startMyService());
        stopRecordingBtn.setOnClickListener(v -> stopMyService());
    }

    private void setHelloMessage() {
        String fName = DBManager.getInstance().getUser().getfName();
        if(fName == null || fName.isEmpty())
            helloMessage.setText("Hello !");
        else {
            helloMessage.setText("Hello, " + fName + "!");
        }
    }

    private void setButtonMode(MaterialButton button, int color, boolean isEnabled) {
        button.setBackgroundColor(color);
        button.setClickable(isEnabled);
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
                    loadDataAndMoveToPreferencesPage();
                }

                if (id == R.id.nav_profile) {
                    moveToProfilePage();
                }

                if (id == R.id.nav_guide) {
                    moveToGuidePage();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void loadDataAndMoveToPreferencesPage() {
        if(DBManager.getVibrationsList() == null) {
            DBManager.getInstance().loadVibrationListFromDB(new DBManager.CallBack<Boolean>() {
                @Override
                public void res(Boolean res) {
                    if (res) { //data load successfully
                        moveToPreferencesPage();
                    }
                }
            });
        } else {
            moveToPreferencesPage();
        }
    }
    private void moveToPreferencesPage() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
        finish();
    }
    private void moveToProfilePage() {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
        finish();
    }
    private void moveToGuidePage() {
        Intent i = new Intent(this, GuideActivity.class);
        startActivity(i);
        finish();
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
        setButtonMode(startRecordingBtn, ContextCompat.getColor(this, R.color.buttons_disabled), false);
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

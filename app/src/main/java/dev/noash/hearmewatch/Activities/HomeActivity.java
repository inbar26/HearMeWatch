package dev.noash.hearmewatch.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;

import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.view.LayoutInflater;

import androidx.activity.OnBackPressedCallback;
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

    private MaterialButton startRecordingBtn, stopRecordingBtn;
    private TextView title, statusMessage, contactUsBtn;
    private View statusDot;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_drawer);
        LayoutInflater.from(this).inflate(R.layout.activity_home, findViewById(R.id.content_container), true);

        findViews();
        initViews();
    }

    private void findViews() {
        title = findViewById(R.id.TV_page_title);
        startRecordingBtn = findViewById(R.id.BTN_start_detection);
        stopRecordingBtn = findViewById(R.id.BTN_stop_detection);
        statusDot = findViewById(R.id.dotStatus);
        statusMessage = findViewById(R.id.tvStatus);
        contactUsBtn = findViewById(R.id.tvContactUs);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initDrawer();
        initHeader();
        updateUIAccordingToRecordingState();

        startRecordingBtn.setOnClickListener(v -> startMyService());
        stopRecordingBtn.setOnClickListener(v -> stopMyService());

        contactUsBtn.setOnClickListener(v -> shoeContactUsMessage());
    }

    private void shoeContactUsMessage() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_contact_info, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // PopupWindow positioning on screen
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        contactUsBtn.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int x = anchorX - popupWidth / 5; // Move left
        int y = anchorY - popupHeight - 54; // Move up

        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(20f);
        popupWindow.showAtLocation(contactUsBtn, Gravity.NO_GRAVITY, x, y);
    }

    private void updateUIAccordingToRecordingState() {
        boolean isRunning = SPManager.getInstance().isServiceRunning();

        if (isRunning) {
            startRecordingBtn.setVisibility(View.GONE);
            stopRecordingBtn.setVisibility(View.VISIBLE);
            statusDot.setBackgroundResource(R.drawable.dot_green);
            statusMessage.setText("Listening ...");
        } else {
            startRecordingBtn.setVisibility(View.VISIBLE);
            stopRecordingBtn.setVisibility(View.GONE);
            statusDot.setBackgroundResource(R.drawable.dot_gray);
            statusMessage.setText("No active recording");
        }
    }

    private void initDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navView = findViewById(R.id.navigation_view);
        DrawerManager.setCurrentId(R.id.nav_dashboard);
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
        subtitle.setText("Ready to detect sounds ?");
    }

    private void setHelloMessage() {
        String fName = DBManager.getInstance().getUser().getfName();
        if (fName == null || fName.isEmpty())
            title.setText("Hello !");
        else
            title.setText("Hello, " + fName + "!");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        SPManager.getInstance().setIsServiceRunning(true);
        updateUIAccordingToRecordingState();
    }

    private void stopMyService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        stopService(serviceIntent);

        SPManager.getInstance().setIsServiceRunning(false);
        updateUIAccordingToRecordingState();
        Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
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

    private void initReturnButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToHomePage();
            }
        });
    }

    private void returnToHomePage() {
    }
}

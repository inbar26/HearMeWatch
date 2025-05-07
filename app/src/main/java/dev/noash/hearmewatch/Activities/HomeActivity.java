package dev.noash.hearmewatch.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Models.User;


public class HomeActivity extends AppCompatActivity {
    private LinearLayout mainLayout;
    private DrawerLayout home_drawerLayout;
    private NavigationView home_navigationView;
    private Toolbar home_toolbar;
    MaterialButton BTN_start_recording;
    MaterialButton BTN_stop_recording;
    MaterialButton BTN_connect_watch;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        findViews();
        initViews();
    }

    private void initViews() {
        BTN_start_recording.setOnClickListener(v -> startRecording());
        BTN_stop_recording.setOnClickListener(v -> stopRecording());
        BTN_connect_watch.setOnClickListener(v -> connectWatch());
        setSupportActionBar(home_toolbar);
        menuManagement();
    }
    private void menuManagement() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, home_drawerLayout, home_toolbar, R.string.open, R.string.close);
        home_drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        home_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_preferences) {
                    moveToPreferencesPage();
                }
                home_drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    private void findViews() {
        home_drawerLayout = findViewById(R.id.home_drawer_layout);
        home_navigationView = findViewById(R.id.home_navigation_view);
        home_toolbar = findViewById(R.id.home_toolbar);
        mainLayout = findViewById(R.id.home_main_layout);
        BTN_start_recording = findViewById(R.id.BTN_start_recording);
        BTN_stop_recording = findViewById(R.id.BTN_stop_recording);
        BTN_connect_watch = findViewById(R.id.BTN_connect_watch);
    }
    private void moveToPreferencesPage() {
        Intent i = new Intent(this, PreferencesActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }

    private void connectWatch() {

    }

    private void stopRecording() {
    }

    private void startRecording() {
    }
}

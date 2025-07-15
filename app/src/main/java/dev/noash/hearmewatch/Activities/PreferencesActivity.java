package dev.noash.hearmewatch.Activities;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import dev.noash.hearmewatch.Fragments.VibrationListFragment;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Fragments.PreferenceListFragment;

public class PreferencesActivity extends AppCompatActivity {

    ImageButton backBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        findViews();
        initViews();
        setupFragment();
    }
    private void findViews() {
        backBtn = findViewById(R.id.preferences_BTN_back);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        backBtn.setOnClickListener(v -> returnToHomePage());
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
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    private void setupFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.FL_preferences, new PreferenceListFragment());
        transaction.replace(R.id.FL_vibrations, new VibrationListFragment());
        transaction.commit();
    }
}

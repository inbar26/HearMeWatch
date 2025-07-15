package dev.noash.hearmewatch.Activities;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;

public class GuideActivity extends AppCompatActivity {

    ImageButton backBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        findViews();
        initViews();;
    }

    private void findViews() {
        backBtn = findViewById(R.id.guide_BTN_back);
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
}

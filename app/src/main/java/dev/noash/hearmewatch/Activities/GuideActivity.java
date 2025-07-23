package dev.noash.hearmewatch.Activities;

import android.os.Bundle;
import android.view.View;
import android.text.Html;
import android.view.LayoutInflater;

import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.StepItemView;
import dev.noash.hearmewatch.Utilities.DrawerManager;

public class GuideActivity extends AppCompatActivity {

    private StepItemView guide1_step1, guide1_step2, guide1_step3, guide2_step1, guide2_step2, guide2_step3;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_drawer);
        LayoutInflater.from(this).inflate(R.layout.activity_guide, findViewById(R.id.content_container), true);

        findViews();
        initViews();
    }

    private void findViews() {
        guide1_step1 = findViewById(R.id.guide1_step1);
        guide1_step2 = findViewById(R.id.guide1_step2);
        guide1_step3 = findViewById(R.id.guide1_step3);
        guide2_step1 = findViewById(R.id.guide2_step1);
        guide2_step2 = findViewById(R.id.guide2_step2);
        guide2_step3 = findViewById(R.id.guide2_step3);
    }
    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initDrawer();
        initHeader();

        //Init steps info
        guide1_step1.setStep(
                1,
                "Go to Dashboard",
                "Open the main dashboard from the menu",
                R.drawable.bg_step_number_blue
        );

        guide1_step2.setStep(
                2,
                "Tap \"Start Sound Detection\"",
                "Press the large button to begin monitoring sounds",
                R.drawable.bg_step_number_blue
        );

        guide1_step3.setStep(
                3,
                "Allow Microphone Access",
                "Grant permission when prompted by the system",
                R.drawable.bg_step_number_blue
        );

        guide2_step1.setStep(
                1,
                "Open Preferences",
                "Navigate to Preferences from the side menu",
                R.drawable.bg_step_number_purple
        );

        guide2_step2.setStep(
                2,
                "Select Sound Categories",
                "Toggle on/off the sounds you want to be alerted about",
                R.drawable.bg_step_number_purple
        );

        guide2_step3.setStep(
                3,
                "Save Your Preferences",
                "Your choices will be remembered for future sessions",
                R.drawable.bg_step_number_purple
        );

        //Init info
        TextView categories = findViewById(R.id.TV_available_categories);
        categories.setText(Html.fromHtml(getString(R.string.available_categories_html)));

        TextView privacy = findViewById(R.id.TV_privacy_info);
        privacy.setText(Html.fromHtml(getString(R.string.privacy_notice)));

        //Init bullets info
        TextView bullet1 = findViewById(R.id.bullet1).findViewById(R.id.bullet_text);
        bullet1.setText("Vibration notification");

        TextView bullet2 = findViewById(R.id.bullet2).findViewById(R.id.bullet_text);
        bullet2.setText("Visual alert with sound type");

        TextView bullet3 = findViewById(R.id.bullet3).findViewById(R.id.bullet_text);
        bullet3.setText("Timestamp of detection");
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
        TextView title = findViewById(R.id.TV_page_title);
        TextView subtitle = findViewById(R.id.TV_page_subtitle);

        title.setText("User Guide");
        subtitle.setText("Learn how to use HearMeWatch effectively");
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

package dev.noash.hearmewatch.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import dev.noash.hearmewatch.Fragments.PreferenceListFragment;
import dev.noash.hearmewatch.R;

public class PreferencesActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perferences);
        findViews();
        initViews();
        setupFragment();
    }

    private void initViews() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToHomePage();
            }
        });
    }

    private void findViews() {

    }
    private void setupFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.FL_preferences, new PreferenceListFragment());
        transaction.commit();
    }
    private void returnToHomePage() {
        Intent i = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }
}

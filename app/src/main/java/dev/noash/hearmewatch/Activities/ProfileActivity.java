package dev.noash.hearmewatch.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.Utilities.DrawerManager;

public class ProfileActivity extends AppCompatActivity {

    private EditText fName, lName, email;
    private MaterialButton submitBTN;

    private TextView tvImageProfile ;

    private MaterialButton tvImageButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_drawer);
        LayoutInflater.from(this).inflate(R.layout.activity_profile, findViewById(R.id.content_container), true);

        findViews();
        initViews();
    }

    private void uploadPhoto() {
        Toast.makeText(this, "Upload photo", Toast.LENGTH_SHORT).show();
    }

    private void findViews() {
        fName = findViewById(R.id.ET_first_name);
        lName = findViewById(R.id.ET_last_name);
        email = findViewById(R.id.ET_email);
        submitBTN = findViewById(R.id.BTN_save_changes);
        tvImageProfile = findViewById(R.id.IV_profile_picture);
        tvImageButton = findViewById(R.id.BTN_choose_image);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initDrawer();
        initHeader();
        initFields();

        submitBTN.setOnClickListener(v -> updateUserDetails());

        String firstN = DBManager.getInstance().getUser().getfName();

        if (tvImageProfile != null && firstN != null && !firstN.isEmpty()) {
            String firstLetter = firstN.substring(0, 1).toUpperCase();
            tvImageProfile.setText(firstLetter);
        }

        tvImageButton.setOnClickListener(v -> uploadPhoto());
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

        title.setText("Your Profile");
        subtitle.setText("Manage your account information");
    }

    private void initFields() {
        email.setText(DBManager.getInstance().getUser().getEmail());

        String fName = DBManager.getInstance().getUser().getfName();
        String lName = DBManager.getInstance().getUser().getlName();

        if(fName != null && !fName.isEmpty())
            this.fName.setText(fName);

        if(lName != null && !lName.isEmpty())
            this.lName.setText(lName);
    }

    private void updateUserDetails() {
        String lName = this.lName.getText().toString().trim();
        String fName = this.fName.getText().toString().trim();

        if (fName.isEmpty() && lName.isEmpty()) {
            Toast.makeText(this, "No data entered to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        DBManager.getInstance().updateUserName(fName, lName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DBManager.getInstance().getUser().setName(fName, lName);
                        SPManager.getInstance().setUserName(fName);
                        Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show();
                        initDrawer();
                    } else {
                        Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                });
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

package dev.noash.hearmewatch.Activities;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText ET_fName;
    private TextInputEditText ET_lName;
    private TextInputEditText ET_email;
    private MaterialButton submitBTN;
    private ImageButton backBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViews();
        initViews();
    }

    private void findViews() {
        ET_fName = findViewById(R.id.ET_first_name);
        ET_lName = findViewById(R.id.ET_last_name);
        ET_email = findViewById(R.id.ET_email);
        submitBTN = findViewById(R.id.BTN_submit);
        backBtn = findViewById(R.id.profile_BTN_back);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initFields();

        submitBTN.setOnClickListener(v -> updateUserDetails());
        backBtn.setOnClickListener(v -> returnToHomePage());
    }

    private void initFields() {
        ET_email.setText(DBManager.getInstance().getUser().getEmail());

        String fName = DBManager.getInstance().getUser().getfName();
        String lName = DBManager.getInstance().getUser().getlName();

        if(fName != null && !fName.isEmpty())
            ET_fName.setText(fName);

        if(lName != null && !lName.isEmpty())
            ET_lName.setText(lName);
    }

    private void updateUserDetails() {
        String lName = ET_lName.getText().toString().trim();
        String fName = ET_fName.getText().toString().trim();

        if (fName.isEmpty() && lName.isEmpty()) {
            Toast.makeText(this, "No data entered to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        DBManager.getInstance().updateUserName(fName, lName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DBManager.getInstance().getUser().setName(fName, lName);
                        SPManager.getInstance().setName(fName);
                        Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show();
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
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}

package dev.noash.hearmewatch.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Utilities.DBManager;

public class ProfileActivity extends AppCompatActivity {

    private TextInputLayout ET_fName;

    private TextInputEditText ET_fName1;
    private TextInputEditText ET_lName;
    private TextInputEditText ET_email;
    private MaterialButton BTN_submit;
    ImageButton backBtn;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViews();
        initViews();
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        BTN_submit.setOnClickListener(v -> updateUserDetails());
        backBtn.setOnClickListener(v -> returnToHomePage());
        String name = DBManager.getUser().getName();
        if(name != null && !name.equals("")) {
            //ET_fName.setText(name.split(" ")[0]);
            ET_fName1.setText(name.split(" ")[0]);
            ET_lName.setText(name.split(" ")[1]);
            // setText(name.split(" ")[1]);
        }
        ET_email.setText(DBManager.getUser().getEmail());

    }

    private void updateUserDetails() {
        String name;

        // String fName = ET_fName.getEditText().toString().trim();
        String lName = ET_lName.getText().toString().trim();
        String fName = ET_fName1.getText().toString().trim();

        if (fName.isEmpty() || lName.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        name=fName.trim()+" " +lName.trim();

        DBManager.updateUserName(name).addOnCompleteListener(task -> Toast.makeText(this, "User was updated successfully", Toast.LENGTH_SHORT).show());
    }

    private void findViews() {
        ET_fName = findViewById(R.id.ET_first_name);
        ET_fName1 = findViewById(R.id.ET_first_name1);
        ET_lName = findViewById(R.id.ET_last_name);
        ET_email = findViewById(R.id.ET_email);
        BTN_submit = findViewById(R.id.BTN_submit);
        backBtn = findViewById(R.id.design_BTN_back);
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

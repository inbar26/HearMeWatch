package dev.noash.hearmewatch.Activities;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;

import dev.noash.hearmewatch.Utilities.DataBaseManager;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Models.User;

public class LoginActivity extends AppCompatActivity {
    MaterialButton BTN_google;
    int x;
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    saveNewUser();
                    moveToHomePage();
                } else {
                    Toast.makeText(this, "התחברות נכשלה או בוטלה", LENGTH_SHORT).show();
                }
            }
    );

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBaseManager.checkIfUserExist(new DataBaseManager.CallBack<Boolean>() {
            @Override
            public void res(Boolean res) {
                if(res) {
                    moveToHomePage();
                } else {
                    setContentView(R.layout.activity_login);
                    findViews();
                    initViews();
                }
            }
        });


//        setContentView(R.layout.activity_login);
//        findViews();
//        initViews();
//        saveNewUser();
    }
    private void findViews() {
        BTN_google = findViewById(R.id.BTN_google);
    }
    private void initViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }
        BTN_google.setOnClickListener(v -> login());
    }
    private void login() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build();

        signInLauncher.launch(signInIntent);
    }
    private void moveToHomePage() {
        Intent i = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }
    private void saveNewUser() {
        User user = new User();
//        user.setName("noa");
//        user.setEmail("");
        DataBaseManager.addUserToDB(user);
    }
}
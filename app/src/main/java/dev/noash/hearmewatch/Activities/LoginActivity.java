package dev.noash.hearmewatch.Activities;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;

import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Models.User;
import dev.noash.hearmewatch.Utilities.DataBaseManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> signInLauncher;
    private MaterialButton googleBtn;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.setStatusBar(getWindow(), this);

        FirebaseUser user = DataBaseManager.isUserLoggedIn();
        if(user == null) {
            showLoginScreen();
        } else {
            checkUserInDB(user);
        }
    }
    private void showLoginScreen() {
        setContentView(R.layout.activity_login);
        initSignInLauncher();
        findViews();
        initViews();
    }
    private void initSignInLauncher() {
        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        DataBaseManager.saveNewUserToDB(user).addOnCompleteListener(task -> moveToHomePage());
                    } else {
                        Toast.makeText(this, "ההתחברות נכשלה או בוטלה", LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void findViews() {
        imageView = findViewById(R.id.TV_title);
        googleBtn = findViewById(R.id.BTN_google);
    }
    private void initViews() {
        googleBtn.setOnClickListener(v -> signUp());
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_logo)
                .into(imageView);
    }
    private void signUp() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build();

        signInLauncher.launch(signInIntent);
    }
    private void checkUserInDB(FirebaseUser user) {
        DataBaseManager.addUserToDB_ifNecessary(user, new DataBaseManager.CallBack<Boolean>() {
            @Override
            public void res(Boolean res) {
                if(res) {
                    loadDataAndMoveToHomePage();
                    //moveToHomePage();
                } else {
                    showLoginScreen();
                }
            }
        });
    }

    private void loadDataAndMoveToHomePage() {
        DataBaseManager.loadUserDataFromDB(new DataBaseManager.CallBack<Boolean>() {
            @Override
            public void res(Boolean res) {
                if(res)
                    moveToHomePage();
            }
        });
    }
    private void moveToHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}


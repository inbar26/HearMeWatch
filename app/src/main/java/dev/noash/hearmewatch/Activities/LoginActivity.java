package dev.noash.hearmewatch.Activities;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;

import dev.noash.hearmewatch.Utilities.DataBaseManager;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Models.User;

public class LoginActivity extends AppCompatActivity {
    private MaterialButton BTN_google;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }
        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        saveNewUserToDB(user);
                    } else {
                        Toast.makeText(this, "ההתחברות נכשלה או בוטלה", LENGTH_SHORT).show();
                    }
                }
        );

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            checkIfUserExistsInDB();
        } else {
            showLoginScreen();
        }
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login);
        ImageView imageView = findViewById(R.id.TV_title);
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_logo)  // בלי הסיומת
                .into(imageView);

        for(int i=0; i<100000000; i++) {

        }
        BTN_google = findViewById(R.id.BTN_google);
        BTN_google.setOnClickListener(v -> login());
    }

    private void login() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void moveToHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void checkIfUserExistsInDB() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            showLoginScreen();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    moveToHomePage();
                } else {
                    showLoginScreen();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoginScreen();
            }
        });
    }
    private void saveNewUserToDB(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());

        if (firebaseUser.getDisplayName() != null)
            user.setfName(firebaseUser.getDisplayName());
        else if (firebaseUser.getEmail() != null)
            user.setfName(firebaseUser.getEmail());
        else
            user.setfName("משתמש ללא שם");

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(task -> moveToHomePage());
    }
}


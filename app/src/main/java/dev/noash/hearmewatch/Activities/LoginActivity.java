package dev.noash.hearmewatch.Activities;

import static android.widget.Toast.LENGTH_SHORT;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;

import java.util.Collections;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> signInLauncher;
    private Button googleBtn;
    private ImageView imageView;
    private View loginContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.setStatusBar(getWindow(), this);

        DBManager.init(getApplicationContext());
        SPManager.init(getApplicationContext());

        initSignInLauncher();

        FirebaseUser user = DBManager.getInstance().fetchCurrentUser();
        if(user == null) {
            showLoginScreen(); //user not found
        } else {
            handleUserEntry(user); //user exists
        }

//        List<Vibration> vibrations = Arrays.asList(
//                new Vibration("Quick Tap", Arrays.asList(0L, 150L)),
//                new Vibration("Double Pulse", Arrays.asList(0L, 150L, 100L, 150L)),
//                new Vibration("Steady Alarm", Arrays.asList(0L, 300L, 200L, 300L)),
//                new Vibration("Bold Buzz", Arrays.asList(0L, 600L))
//        );
//        DBManager.getInstance().saveVibrationsToDatabase(vibrations);
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login);
        findViews();
        initViews();
    }

    private void initSignInLauncher() {
        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        DBManager.getInstance().addUserToDB_ifNecessary(user, new DBManager.CallBack<Boolean>() {
                            @Override
                            public void res(Boolean res) {
                                if (res != null && res) {
                                    loadDataAndMoveToHomePage();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(this, "Login failed or canceled", LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void findViews() {
        imageView = findViewById(R.id.IV_image);
        googleBtn = findViewById(R.id.BTN_google);
        loginContainer = findViewById(R.id.login_container);
    }
    private void initViews() {
        googleBtn.setOnClickListener(v -> signUp());

        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_logo)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        loginContainer.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull GifDrawable resource, @NonNull Object model, Target<GifDrawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        loginContainer.setVisibility(View.VISIBLE);
                        return false;
                    }


                })
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

    private void handleUserEntry(FirebaseUser user) {
        DBManager.getInstance().addUserToDB_ifNecessary(user, new DBManager.CallBack<Boolean>() {
            @Override
            public void res(Boolean res) {
                if(res) { //user exists in DB
                    loadDataAndMoveToHomePage();
                } else { //something went wrong
                    showLoginScreen();
                }
            }
        });
    }

    private void loadDataAndMoveToHomePage() {
        DBManager.getInstance().loadUserDataFromDB(new DBManager.CallBack<Boolean>() {
            @Override
            public void res(Boolean res) {
                if(res != null && res) { //data load successfully
                        DBManager.getInstance().loadVibrationListFromDB(new DBManager.CallBack<Boolean>() {
                            @Override
                            public void res(Boolean res) {
                                if (res != null && res) { //data load successfully
                                    moveToHomePage();
                                } else {
                                    showLoginScreen();
                                }
                            }
                        });
                    } else {
                    showLoginScreen();
                }
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
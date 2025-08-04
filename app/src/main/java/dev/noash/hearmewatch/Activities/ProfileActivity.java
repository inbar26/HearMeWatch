package dev.noash.hearmewatch.Activities;

import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.content.ContentValues;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.Utilities.DrawerManager;


public class ProfileActivity extends AppCompatActivity {

    private EditText fName, lName, email;
    private MaterialButton saveChangesBtn, editImageBtn;
    private TextView profileImageTV;
    private ImageView profileImageIV;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private Uri cameraImageUri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_drawer);
        LayoutInflater.from(this).inflate(R.layout.activity_profile, findViewById(R.id.content_container), true);

        findViews();
        initViews();
        updateProfileImageView();
    }
    private void findViews() {
        fName = findViewById(R.id.ET_first_name);
        lName = findViewById(R.id.ET_last_name);
        email = findViewById(R.id.ET_email);
        saveChangesBtn = findViewById(R.id.BTN_save_changes);
        editImageBtn = findViewById(R.id.BTN_choose_image);
        profileImageTV = findViewById(R.id.IV_profile_picture);
        profileImageIV = findViewById(R.id.IV_profile_photo);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initDrawer();
        initHeader();
        initFields();

        saveChangesBtn.setOnClickListener(v -> updateUserDetails());
        editImageBtn.setOnClickListener(v -> showUploadPhotoOptions());
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

        if (profileImageTV != null) {
            String firstLetter = (fName != null && !fName.isEmpty()) ? fName.substring(0, 1).toUpperCase() : "";
            profileImageTV.setText(firstLetter);
            profileImageTV.setBackgroundResource(R.drawable.bg_user_avatar);
        }
    }

    private void updateUserDetails() {
        String lName = this.lName.getText().toString().trim();
        String fName = this.fName.getText().toString().trim();

        if (fName.isEmpty() && lName.isEmpty()) {
            Toast.makeText(this, "No data entered to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalLName = capitalizeFirstLetter(lName);
        String finalFLName = capitalizeFirstLetter(fName);

        DBManager.getInstance().updateUserName(finalFLName, finalLName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DBManager.getInstance().getUser().setName(finalFLName, finalLName);
                        SPManager.getInstance().setUserName(finalFLName);
                        Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show();
                        initDrawer();
                        initFields();
                    } else {
                        Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void showUploadPhotoOptions() { // Shows an options dialog for editing the profile photo: take photo, choose from gallery, or delete photo
        String[] options = {"Take Photo", "Choose from Gallery","Delete photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit profile picture");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkAndRequestCameraPermission();
            } else if (which == 1) {
                checkAndRequestGalleryPermission();
            } else if (which == 2) {
                removeProfilePhoto();
            }
        });
        builder.show();
    }

    private void updateProfileImageView() {
        String imageUrl = DBManager.getInstance().getUser().getProfileImageUrl();
        String firstName = DBManager.getInstance().getUser().getfName();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            profileImageIV.setVisibility(View.VISIBLE);
            profileImageTV.setVisibility(View.GONE);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_user_avatar)
                    .into(profileImageIV);

        } else {
            profileImageIV.setVisibility(View.GONE);
            profileImageTV.setVisibility(View.VISIBLE);

            if (firstName != null && !firstName.isEmpty()) {
                profileImageTV.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
            } else {
                profileImageTV.setText("");
            }

            profileImageTV.setBackgroundResource(R.drawable.bg_user_avatar);
        }
    }

    private void openGallery() { // Opens the device's gallery to allow the user to select a photo
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() { // Launches the camera app to take a new photo and save it
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void checkAndRequestGalleryPermission() { // Checks for gallery access permission and requests it if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2001);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2001);
            } else {
                openGallery();
            }
        }
    }

    private void checkAndRequestCameraPermission() { // Checks for camera access permission and requests it if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2002);
        } else {
            openCamera();
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        DBManager.getInstance().uploadProfileImageToFirebase(imageUri, res -> {
            if (res) {
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                updateProfileImageView();
                initDrawer();
            } else {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // Handles the result from gallery or camera, and triggers image upload to Firebase
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = null;

            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                selectedImageUri = data.getData();
                Log.d("ImageUpload", "Gallery image URI: " + selectedImageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                selectedImageUri = cameraImageUri;
                Log.d("ImageUpload", "Camera image URI: " + selectedImageUri);
            }

            if (selectedImageUri != null) {
                uploadImageToFirebase(selectedImageUri);
            } else {
            Log.e("ImageUpload", "selectedImageUri is NULL ");
            }
        }
    }

    private void removeProfilePhoto() {
        DBManager.getInstance().removeProfilePhotoFromFirebase(res -> {
            if (res) {
                updateProfileImageView();
                initDrawer();
                Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove photo", Toast.LENGTH_SHORT).show();
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
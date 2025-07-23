package dev.noash.hearmewatch.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.MyApp;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;
import dev.noash.hearmewatch.Utilities.DrawerManager;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private EditText fName, lName, email;
    private MaterialButton submitBTN;
    private TextView tvImageProfile ;
    private ImageView imProfilePhoto;

    private MaterialButton tvImageButton;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private Uri cameraImageUri;
    private Uri imageUri;

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
        submitBTN = findViewById(R.id.BTN_save_changes);
        tvImageProfile = findViewById(R.id.IV_profile_picture);
        imProfilePhoto = findViewById(R.id.IV_profile_photo);
        tvImageButton = findViewById(R.id.BTN_choose_image);
    }

    private void initViews() {
        MyApp.setStatusBar(getWindow(), this);
        initReturnButton();
        initDrawer();
        initHeader();
        initFields();

        submitBTN.setOnClickListener(v -> updateUserDetails());

        //String firstN = DBManager.getInstance().getUser().getfName();

//        if (tvImageProfile != null && firstN != null && !firstN.isEmpty()) {
//            String firstLetter = firstN.substring(0, 1).toUpperCase();
//            tvImageProfile.setText(firstLetter);
//        }

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

    // Below are functions for saving and managing the user's profile image URL.
    // Displays the user's profile image if available; otherwise, shows the first initial inside a circular view.
    private void updateProfileImageView() {
        String imageUrl = DBManager.getInstance().getUser().getProfileImageUrl();
        String firstName = DBManager.getInstance().getUser().getfName();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imProfilePhoto.setVisibility(View.VISIBLE);
            tvImageProfile.setVisibility(View.GONE);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_user_avatar)
                    .into(imProfilePhoto);

        } else {
            imProfilePhoto.setVisibility(View.GONE);
            tvImageProfile.setVisibility(View.VISIBLE);

            if (firstName != null && !firstName.isEmpty()) {
                tvImageProfile.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
            } else {
                tvImageProfile.setText("?");
            }

            tvImageProfile.setBackgroundResource(R.drawable.bg_user_avatar);
        }
    }

    // Opens the device's gallery to allow the user to select a photo.
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Launches the camera app to take a new photo and save it.
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    // Checks for gallery access permission and requests it if not already granted.
    private void checkAndRequestGalleryPermission() {
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

    // Checks for camera access permission and requests it if not already granted.
    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2002);
        } else {
            openCamera();
        }
    }

    // Uploads the selected image to Firebase Storage and saves its download URL in the Realtime Database.
    private void uploadImageToFirebase(Uri imageUri) {
        String uid = DBManager.getInstance().fetchCurrentUser().getUid();

        // create a reference to the user's profile image in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + uid + ".jpg");

        // upload the image to Firebase Storage
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // get the download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Save image URL to user object and database
                        DBManager.getInstance().getUser().setProfileImageUrl(downloadUrl);

                        DBManager.getInstance().updateUserProfileImageUrl(downloadUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                                    // Update the UI with the new image
                                    updateProfileImageView();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to save image URL to database", Toast.LENGTH_SHORT).show()
                                );
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                );
    }

    // Handles the result from gallery or camera, and triggers image upload to Firebase.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    // Deletes the user's profile photo from Firebase Storage and removes the URL from the database.
    private void removeProfilePhoto() {
        String uid = DBManager.getInstance().fetchCurrentUser().getUid();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + uid + ".jpg");

        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(uid)
                            .child("profileImageUrl")
                            .removeValue();

                    DBManager.getInstance().getUser().setProfileImageUrl(null);

                    String firstName = DBManager.getInstance().getUser().getfName();
                    if (firstName != null && !firstName.isEmpty()) {
                        String initial = firstName.substring(0, 1).toUpperCase();
                        tvImageProfile.setText(initial);
                        tvImageProfile.setVisibility(View.VISIBLE);
                    }

                    imProfilePhoto.setVisibility(View.GONE);

                    Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove photo", Toast.LENGTH_SHORT).show();
                });
    }

    // Shows an options dialog for editing the profile photo: take photo, choose from gallery, or delete photo.
    private void uploadPhoto() {
        Toast.makeText(this, "Upload photo", Toast.LENGTH_SHORT).show();
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
    // Finished all profile picture related operations
}

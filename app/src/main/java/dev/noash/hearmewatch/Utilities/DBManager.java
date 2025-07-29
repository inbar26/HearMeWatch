package dev.noash.hearmewatch.Utilities;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import dev.noash.hearmewatch.Objects.User;
import dev.noash.hearmewatch.Objects.Vibration;
import dev.noash.hearmewatch.Objects.Preference;
import dev.noash.hearmewatch.Objects.VibrationList;

public class DBManager {
    private static DBManager dbManager;
    private static Context context;
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    public static final String USERS_REF = "users";
    public static final String MY_PREFERENCES_REF = "myPreferences";
    private static final DatabaseReference usersRef = database.getReference(USERS_REF);
    public static final String VIBRATIONS_REF = "vibrations";
    public static final String MY_VIBRATION_REF = "chosenVibration";
    private static final DatabaseReference vibrationsRef = database.getReference(VIBRATIONS_REF);
    private static User currentUser = new User();
    private static VibrationList vibrationsList;
    public static final String IMAGE_STORAGE_REF_START = "profile_images/";
    public static final String IMAGE_STORAGE_REF_END = ".jpg";
    public static final String IMAGE_STORAGE_REF = "profileImageUrl";


    public interface CallBack<T> { void res(T res); }

    public static void init(Context context) {
        if (dbManager == null) {
            synchronized (DBManager.class) {
                if (dbManager == null) {
                    dbManager = new DBManager(context);
                }
            }
        }
    }
    private DBManager(Context context) {
        this.context = context;
    }

    public static DBManager getInstance() {
        if (dbManager == null) {
            throw new IllegalStateException("DBManagaer must be initialized by calling init(context) before use.");
        }
        return dbManager;
    }

    public User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = new User(user);
    }
    public static VibrationList getVibrationsList() {
        return vibrationsList;
    }
    public static void setVibrationsList(VibrationList vibrationsList) {
        DBManager.vibrationsList = vibrationsList;
    }

    public FirebaseUser fetchCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser(); //null -> user not logged
    }
    public void addUserToDB_ifNecessary(FirebaseUser user, CallBack<Boolean> callBack) {
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callBack.res(true);
                } else {
                    //adding user to DB
                    saveNewUserToDB(user).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SPManager.getInstance().clearAllPreferences();
                            callBack.res(true);
                        } else {
                            callBack.res(false);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.res(null);
            }
        });
    }

    public Task<Void> saveNewUserToDB(FirebaseUser firebaseUser) {
        User user = new User();

        user.setId(firebaseUser.getUid());
        user.initMyPreferences();
        user.setChosenVibration("Default");

        if (firebaseUser.getEmail() != null)
            user.setEmail(firebaseUser.getEmail());

        return usersRef.child(firebaseUser.getUid()).setValue(user);
    }

    public void loadUserDataFromDB(CallBack<Boolean> callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setUser(snapshot.getValue(User.class));

                    SPManager.getInstance().savePreferencesFromList(getUser().getMyPreferences());
                    if(getUser().getfName() != null && !getUser().getfName().isEmpty())
                        SPManager.getInstance().setUserName(getUser().getfName());

                    callBack.res(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.res(null);
            }
        });
    }

    public Task<Void> updateUserName(String fName, String lName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getUser().setfName(fName);
        getUser().setlName(lName);

        Task<Void> updateFNameTask = usersRef.child(user.getUid()).child("fName").setValue(fName);
        Task<Void> updateLNameTask = usersRef.child(user.getUid()).child("lName").setValue(lName);

        //return task after 2 tasks are done
        return Tasks.whenAll(updateFNameTask, updateLNameTask);
    }

    public Task<Void> updateUserPreference(Preference preference) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return usersRef
                .child(user.getUid())
                .child(MY_PREFERENCES_REF)
                .child("list")
                .child(preference.getName())
                .setValue(preference);
    }

    public void loadVibrationListFromDB(CallBack<Boolean> callBack) {
        vibrationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Vibration> vibrationMap = (Map<String, Vibration>) snapshot.getValue(new GenericTypeIndicator<Map<String, Vibration>>() {});
                if (vibrationMap != null) {
                    HashMap<String, Vibration> hashMap = new HashMap<>(vibrationMap);
                    DBManager.getInstance().setVibrationsList(new VibrationList(new ArrayList<>(hashMap.values())));
                }
                callBack.res(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.res(false);
            }
        });

    }

    public Task<Void> updateUserVibration(Vibration option) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return usersRef
                .child(user.getUid())
                .child(MY_VIBRATION_REF)
                .setValue(option.getName());
    }

    public static void saveVibrationsToDatabase(List<Vibration> vibrations) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("vibrations");

        HashMap<String, Vibration> vibrationMap = new HashMap<>();
        for (Vibration v : vibrations) {
            vibrationMap.put(v.getName(), v);
        }

        ref.setValue(vibrationMap)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Vibrations saved successfully!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to save vibrations: " + e.getMessage());
                });
    }

    public Task<Void> updateUserProfileImageUrl(String url) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return usersRef.child(user.getUid()).child(IMAGE_STORAGE_REF).setValue(url);
    }

    public void removeProfilePhotoFromFirebase(CallBack<Boolean> callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference storageRef = storage
                .getReference(IMAGE_STORAGE_REF_START + user.getUid() + IMAGE_STORAGE_REF_END);

        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(user.getUid())
                            .child(IMAGE_STORAGE_REF)
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    getUser().setProfileImageUrl(null);
                                    callBack.res(true);
                                } else {
                                    callBack.res(false);
                                }
                            });
                })
                .addOnFailureListener(e -> callBack.res(false));
    }

    public void uploadProfileImageToFirebase(Uri imageUri, CallBack<Boolean> callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference storageRef = storage
                .getReference(IMAGE_STORAGE_REF_START + user.getUid() + IMAGE_STORAGE_REF_END);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateUserProfileImageUrl(downloadUrl)
                                .addOnSuccessListener(aVoid -> {

                                    getUser().setProfileImageUrl(downloadUrl);
                                    callBack.res(true);
                                })
                                .addOnFailureListener(e -> callBack.res(false));

                    }).addOnFailureListener(e -> callBack.res(false));
                })
                .addOnFailureListener(e -> callBack.res(false));
    }
}





package dev.noash.hearmewatch.Utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import dev.noash.hearmewatch.Models.User;
import dev.noash.hearmewatch.Models.MyPreference;

public class DBManager {
    private static DBManager dbManager;
    private static Context context;
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String USERS_REF = "users";
    public static final String MY_PREFERENCES_REF = "myPreferences";
    private static final DatabaseReference usersRef = database.getReference(USERS_REF);
    private static User currentUser = new User();

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
                        SPManager.getInstance().setName(getUser().getfName());

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

    public Task<Void> updateUserPreference(MyPreference preference) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return usersRef
                .child(user.getUid())
                .child(MY_PREFERENCES_REF)
                .child("list")
                .child(preference.getName())
                .setValue(preference);
    }
}





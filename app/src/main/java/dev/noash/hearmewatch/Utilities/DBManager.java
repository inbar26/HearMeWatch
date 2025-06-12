package dev.noash.hearmewatch.Utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import dev.noash.hearmewatch.Models.MyPreference;
import dev.noash.hearmewatch.Models.User;

public class DBManager {
    private static DBManager DBManager;
    private static Context context;
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String USERS_REF = "users";
    private static final DatabaseReference usersRef = database.getReference(USERS_REF);
    private static User currentUser = new User();

    public interface CallBack<T> { void res(T res); }

    public static void init(Context context) {
        if (DBManager == null) {
            synchronized (DBManager.class) {
                if (DBManager == null) {
                    DBManager = new DBManager(context);
                }
            }
        }
    }
    private DBManager(Context context) {
        this.context = context;
    }

    public static FirebaseUser isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser(); //null -> user not logged
    }
    public static void addUserToDB_ifNecessary(FirebaseUser user, CallBack<Boolean> callBack) {
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callBack.res(true);
                } else {
                    saveNewUserToDB(user).addOnCompleteListener(task -> callBack.res(true));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.res(null);
            }
        });
    }
    public static Task<Void> saveNewUserToDB(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.initMyPreferences();

        if (firebaseUser.getDisplayName() != null)
            user.setName(firebaseUser.getDisplayName());
        if (firebaseUser.getEmail() != null)
            user.setEmail(firebaseUser.getEmail());


        return usersRef.child(firebaseUser.getUid()).setValue(user);
    }

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = new User(user);
    }
    public static Task<Void> updateUserName(String name) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getUser().setName(name);
        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("name").setValue(name);

    }

    public static void loadUserDataFromDB(CallBack<Boolean> callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setUser(snapshot.getValue(User.class));
                    callBack.res(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.res(null);
            }
        });
    }

    public static void updateUserPreferences(ArrayList<MyPreference> preferenceList) {
        for(MyPreference p: preferenceList ) {
            getUser().getMyPreferences().update(p);
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(getUser().getId())
                    .child("myPreferences").child("list").child(p.getName()).setValue(p);

            SPManager.getInstance().setNotificationPreference(p.getName(), p.getActive());
        }
    }
}





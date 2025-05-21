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
import dev.noash.hearmewatch.Models.PreferenceList;
import dev.noash.hearmewatch.Models.User;
import dev.noash.hearmewatch.MyApp;

public class DataBaseManager {
    private static DataBaseManager dataManager;
    private static Context context;
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String USERS_LIST = "users";
    private static final DatabaseReference usersRef = database.getReference(USERS_LIST);

    private static User currentUser = new User();

    private static PreferenceList defultPre = new PreferenceList();



    public interface CallBack<T> { void res(T res); }

    public static void init(Context context) {
        if (dataManager == null) {
            synchronized (DataBaseManager.class) {
                if (dataManager == null) {
                    dataManager = new DataBaseManager(context.getApplicationContext());
                }
            }
        }
    }
    private DataBaseManager(Context context) {
        this.context = context;
    }

    public static FirebaseUser isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser(); //null -> user not logged
    }
    public static void addUserToDB_ifNecessary(FirebaseUser user, CallBack<Boolean> callBack) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callBack.res(true);
                } else {
                    saveNewUserToDB(user).addOnCompleteListener(task -> callBack.res(true));;
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

        if (firebaseUser.getDisplayName() != null)
            user.setName(firebaseUser.getDisplayName());
        if (firebaseUser.getEmail() != null)
            user.setEmail(firebaseUser.getEmail());
        user.setMyPreferences(defultPre);

//        else if (firebaseUser.getEmail() != null)
//            user.setName(firebaseUser.getEmail());
//        else
//            user.setName("משתמש ללא שם");

        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.getUid())
                .setValue(user);

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
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        //getUser().setMyPreferences(new PreferenceList(preferenceList));

        for(MyPreference p: preferenceList ) {
            getUser().getMyPreferences().update(p);
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(getUser().getId())
                    .child("PreferenceList").child(p.getName()).setValue(p);
        }

    }

//        public static void checkIfUserExist(CallBack<Boolean> callBack) {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser == null) {
//            // אין יוזר מחובר בכלל - מחזירים false
//            callBack.res(false);
//            return;
//        }
//
//        String userId = firebaseUser.getUid();
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
//
//        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                callBack.res(snapshot.exists());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // במקרה של שגיאה בגישה לדאטאבייס, מחזירים false או אפשר גם להעביר שגיאה אחרת
//                callBack.res(false);
//            }
//        });
//    }
//    public static void addUserToDB(User newUser) {
//        if (newUser != null) {
//            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            newUser.setId(userUid);
//            usersRef.child(userUid).setValue(newUser);
//        }
//    }
//public static User addUserToDB() {
//    User newUser = new User();
//    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//    newUser.setId(userUid);
//    newUser.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
//    usersRef.child(userUid).setValue(newUser);
//    return  newUser;
//}
}





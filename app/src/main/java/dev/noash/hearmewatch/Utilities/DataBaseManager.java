package dev.noash.hearmewatch.Utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.noash.hearmewatch.Models.User;

public class DataBaseManager {
    private static DataBaseManager dataManager;
    private static Context context;
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String USERS_LIST = "users";
    private static final DatabaseReference usersRef = database.getReference(USERS_LIST);
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
    public static void addUserToDB(User newUser) {
        if (newUser != null) {
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            newUser.setId(userUid);
            usersRef.child(userUid).setValue(newUser);
        }
    }
    public static void checkIfUserExist(CallBack<Boolean> callBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // אין יוזר מחובר בכלל - מחזירים false
            callBack.res(false);
            return;
        }

        String userId = firebaseUser.getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callBack.res(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // במקרה של שגיאה בגישה לדאטאבייס, מחזירים false או אפשר גם להעביר שגיאה אחרת
                callBack.res(false);
            }
        });
    }


}





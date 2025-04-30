package dev.noash.hearmewatch.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Models.User;


public class HomeActivity extends AppCompatActivity {
    private LinearLayout mainLayout;
    private DrawerLayout home_drawerLayout;
    private NavigationView home_navigationView;
    private Toolbar home_toolbar;
    private final int BLACK = Color.parseColor("#000000");
    private final int WHITE = Color.parseColor("#FFFFFF");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        findViews();
        initViews();
        FirebaseDatabase.getInstance().getReference("test_node")
                .setValue("Hello from Noa!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "✔ שמירה ל-Firebase הצליחה!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ שמירה נכשלה: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });

        User user = new User();
//        user.setName("noa");
//        user.setEmail("");
       // DataBaseManager.addUserToDB(user);
    }



    private void initViews() {
        setSupportActionBar(home_toolbar);
       // home_TV_info_box.setText(Html.fromHtml(getString(R.string.home_TV_info), Html.FROM_HTML_MODE_LEGACY));
        menuManagement();
    }
    private void menuManagement() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, home_drawerLayout, home_toolbar, R.string.open, R.string.close);
        home_drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        home_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
//                if (id == R.id.nav_design) {
//                    //moveToDesignPage();
//                }
                home_drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    private void findViews() {
        home_drawerLayout = findViewById(R.id.home_drawer_layout);
        home_navigationView = findViewById(R.id.home_navigation_view);
        home_toolbar = findViewById(R.id.home_toolbar);
        mainLayout = findViewById(R.id.home_main_layout);
    }
//    private void moveToDesignPage() {
//        Intent i = new Intent(this, DesignActivity.class);
//        Bundle bundle = new Bundle();
//        i.putExtras(bundle);
//        startActivity(i);
//        finish();
//    }
}

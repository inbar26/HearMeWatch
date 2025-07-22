package dev.noash.hearmewatch.Utilities;

import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Activities.HomeActivity;
import dev.noash.hearmewatch.Activities.GuideActivity;
import dev.noash.hearmewatch.Activities.ProfileActivity;
import dev.noash.hearmewatch.Activities.PreferencesActivity;

public class DrawerManager {

    public static void setupDrawer(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, NavigationView navView) {
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);

        toolbar.post(() -> {
            try {
                toolbar.setOnClickListener(v -> {
                    drawerLayout.openDrawer(GravityCompat.START);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_dashboard) {
                intent = new Intent(activity, HomeActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            } else if (id == R.id.nav_preferences) {
                intent = new Intent(activity, PreferencesActivity.class);
            } else if (id == R.id.nav_guide) {
                intent = new Intent(activity, GuideActivity.class);
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public static void updateUserCard(View headerView, String name, String email) {
        TextView tvAvatar = headerView.findViewById(R.id.TV_header_user_avatar);
        TextView tvName = headerView.findViewById(R.id.TV_header_user_name);
        TextView tvEmail = headerView.findViewById(R.id.TV_header_user_email);


        if (tvName != null) {
            tvName.setText(name);
        }

        if (tvEmail != null) {
            tvEmail.setText(email);
        }

        if (tvAvatar != null && name != null && !name.isEmpty()) {
            String firstLetter = name.substring(0, 1).toUpperCase();
            tvAvatar.setText(firstLetter);
        }
    }
}

package dev.noash.hearmewatch.Utilities;

import android.view.View;
import android.app.Activity;
import android.view.Gravity;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Activities.HomeActivity;
import dev.noash.hearmewatch.Activities.GuideActivity;
import dev.noash.hearmewatch.Activities.LoginActivity;
import dev.noash.hearmewatch.Activities.ProfileActivity;
import dev.noash.hearmewatch.Activities.PreferencesActivity;

public class DrawerManager {
    private static Integer current_id = R.id.nav_dashboard;

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

        ImageView logoutBtn = activity.findViewById(R.id.IB_logout_icon);
        View signOutLayout = logoutBtn != null ? (View) logoutBtn.getParent() : null;

        if (signOutLayout != null) {
            signOutLayout.setOnClickListener(v -> {
                if(!SPManager.getInstance().isServiceRunning()) {
                    FirebaseAuth.getInstance().signOut();

                    DrawerManager.setCurrentId(R.id.nav_dashboard);

                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    Snackbar snackbar = Snackbar.make(drawerLayout, "To sign out, please stop the detection service first.", Snackbar.LENGTH_INDEFINITE);

                    snackbar.setActionTextColor(ContextCompat.getColor(activity, R.color.snackbar_action));
                    View snackbarView = snackbar.getView();
                    TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setLineSpacing(5f, 1.2f);

                    snackbar.setAction("OK", v2 -> {
                        snackbar.dismiss();
                    });

                    snackbar.show();
                }
            });
        }

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_dashboard) {
                current_id = R.id.nav_dashboard;
                intent = new Intent(activity, HomeActivity.class);
            } else if (id == R.id.nav_profile) {
                current_id = R.id.nav_profile;
                intent = new Intent(activity, ProfileActivity.class);
            } else if (id == R.id.nav_preferences) {
                current_id = R.id.nav_preferences;
                intent = new Intent(activity, PreferencesActivity.class);
            } else if (id == R.id.nav_guide) {
                current_id = R.id.nav_guide;
                intent = new Intent(activity, GuideActivity.class);
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (current_id != -1) { // Set navigation items background
            navView.setCheckedItem(current_id);

            navView.post(() -> {
                for (int i = 0; i < navView.getMenu().size(); i++) {
                    MenuItem menuItem = navView.getMenu().getItem(i);
                    View itemView = navView.findViewById(menuItem.getItemId());

                    if (itemView != null) {
                        if (menuItem.getItemId() == current_id) {
                            itemView.setBackgroundResource(R.drawable.bg_menu_item_blue); // Current page - blue background
                        } else {
                            itemView.setBackground(null);
                        }
                    }
                }
            });
        }
    }

    public static void updateUserCard(NavigationView navView, View headerView, String name, String email) {
        MenuItem item = navView.getMenu().findItem(R.id.nav_profile);
        View itemView = navView.findViewById(item.getItemId());

        if (itemView != null) {
            itemView.setBackgroundResource(R.drawable.bg_menu_item_blue);
        }

        TextView tvAvatar = headerView.findViewById(R.id.TV_header_user_avatar);
        ImageView ivAvatar = headerView.findViewById(R.id.IV_header_user_avatar);
        TextView tvName = headerView.findViewById(R.id.TV_header_user_name);
        TextView tvEmail = headerView.findViewById(R.id.TV_header_user_email);

        if (tvName != null) {
            tvName.setText(name);
        }

        if (tvEmail != null) {
            tvEmail.setText(email);
        }

        String imageUrl = DBManager.getInstance().getUser().getProfileImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Show image view, hide letter avatar
            if (ivAvatar != null) {
                ivAvatar.setVisibility(View.VISIBLE);
                Glide.with(headerView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_user_avatar)
                        .into(ivAvatar);
            }

            if (tvAvatar != null) {
                tvAvatar.setVisibility(View.GONE);
            }
        } else {
            // Show letter avatar, hide image view
            if (tvAvatar != null) {
                String firstLetter = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "";
                tvAvatar.setText(firstLetter);
                tvAvatar.setBackgroundResource(R.drawable.bg_user_avatar);
                tvAvatar.setVisibility(View.VISIBLE);
            }

            if (ivAvatar != null) {
                ivAvatar.setVisibility(View.GONE);
            }
        }
    }

    public static void setCurrentId(int id) {
        current_id = id;
    }
}

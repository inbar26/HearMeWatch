package dev.noash.hearmewatch.Fragments;

import android.view.View;
import android.os.Bundle;

import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Objects.Preference;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;

public class PreferenceListFragment extends Fragment {
    private ListView LV_items;
    private final ArrayList<Preference> preferences = new ArrayList<>();
    private ArrayAdapter<Preference> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        LV_items = view.findViewById(R.id.LV_items);

        //Init list title + description
        TextView tvTitle = view.findViewById(R.id.TV_title_list);
        TextView tvSubtitle = view.findViewById(R.id.TV_subtitle);
        ImageView ivIcon = view.findViewById(R.id.IV_title_icon_list);

        tvTitle.setText("Sound Categories");
        tvSubtitle.setText("Choose which sounds you want to receive alerts for");
        ivIcon.setImageResource(R.drawable.ic_bell);

        HashMap<String, Preference> temp = DBManager.getInstance().getUser().getMyPreferences().getList();
        for (Map.Entry<String, Preference> entry : temp.entrySet()) {
            preferences.add(entry.getValue());
        }
        setupListView();
        return view;
    }

    private void setupListView() {
        adapter = new ArrayAdapter<Preference>(requireContext(), R.layout.item_preference, preferences) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_preference, parent, false);
                }

                Preference pref = getItem(position);
                TextView name = convertView.findViewById(R.id.TV_pName);
                SwitchCompat toggle = convertView.findViewById(R.id.SC_active);
                ImageView icon = convertView.findViewById(R.id.IV_pIcon);

                if (pref != null) { //init
                    name.setText(pref.getName());
                    setIcon(pref.getName(), icon);
                    toggle.setOnCheckedChangeListener(null);
                    toggle.setChecked(pref.getActive());

                    toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        pref.setActive(isChecked);
                        DBManager.getInstance().updateUserPreference(pref)
                                .addOnCompleteListener(task ->
                                        SPManager.getInstance().setNotificationPreference(pref.getName(), isChecked)
                                );
                    });
                }

                return convertView;
            }

            private void setIcon(String name, ImageView icon) {
                int resId = 0;
                switch (name) {
                    case "Dog Barking":
                        resId = R.drawable.ic_dog;
                        break;
                    case "Baby Crying":
                        resId = R.drawable.ic_baby_crying;
                        break;
                    case "Emergency Vehicle":
                        resId = R.drawable.ic_emergency_vehicle;
                        break;
                    case "Car Horn":
                        resId = R.drawable.ic_car_horn;
                        break;
                    case "Civil Defense":
                        resId = R.drawable.ic_emergency_vehicle;
                        break;
                    case "Door Knock":
                        resId = R.drawable.ic_door_knock;
                        break;
                    case "Intercom":
                        resId = R.drawable.ic_intercom;
                        break;
                    case "Fire Alarm":
                        resId = R.drawable.ic_fire_alarm;
                        break;
                    case "Name Calling":
                        resId = R.drawable.ic_name_calling;
                        break;
                    default:

                        break;
                }
                icon.setImageResource(resId);
            }

        };

        LV_items.setAdapter(adapter);
    }
}
package dev.noash.hearmewatch.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dev.noash.hearmewatch.Models.MyPreference;
import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Utilities.DBManager;

public class PreferenceListFragment extends Fragment {
    private ListView LV_items;
    private final ArrayList<MyPreference> preferences = new ArrayList<>();
    private ArrayAdapter<MyPreference> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        LV_items = view.findViewById(R.id.LV_items);
        HashMap<String, MyPreference> p = DBManager.getUser().getMyPreferences().getList();
        for (Map.Entry<String, MyPreference> entry : p.entrySet()) {
            preferences.add(entry.getValue());
        }
        setupListView();
        return view;
    }

//    private ArrayList<MyPreference> loadPreferences() {
//        ArrayList<MyPreference> list = new ArrayList<>();
//        list.add(new MyPreference("Name Calling", true));
//        list.add(new MyPreference("Dog Barking", true));
//        list.add(new MyPreference("Baby Crying", false));
//        list.add(new MyPreference("Ambulance Siren", true));
//        list.add(new MyPreference("Fire Alarm", false));
//        list.add(new MyPreference("Door Knock", true));
//        return list;
//    }

    private void setupListView() {
        adapter = new ArrayAdapter<MyPreference>(requireContext(), R.layout.preference_layout, preferences) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.preference_layout, parent, false);
                }

                MyPreference pref = getItem(position);
                TextView name = convertView.findViewById(R.id.TV_pName);
                SwitchCompat toggle = convertView.findViewById(R.id.SC_active);

                if (pref != null) {
                    name.setText(pref.getName());
                    toggle.setChecked(pref.getActive());
                    toggle.setOnCheckedChangeListener((buttonView, isChecked) -> pref.setActive(isChecked));
                }

                toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    pref.setActive(isChecked);
                    DBManager.updateUserPreferences(preferences);
                });

                return convertView;
            }
        };

        LV_items.setAdapter(adapter);
    }
}

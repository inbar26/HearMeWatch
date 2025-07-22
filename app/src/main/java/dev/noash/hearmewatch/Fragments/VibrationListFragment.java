package dev.noash.hearmewatch.Fragments;

import android.content.Context;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.os.Vibrator;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.os.VibrationEffect;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.wearable.Wearable;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import dev.noash.hearmewatch.R;
import dev.noash.hearmewatch.Objects.Vibration;
import dev.noash.hearmewatch.Utilities.DBManager;
import dev.noash.hearmewatch.Utilities.SPManager;

public class VibrationListFragment extends Fragment {

    private ListView LV_items;
    private final ArrayList<Vibration> vibrations = new ArrayList<>();
    private ArrayAdapter<Vibration> adapter;
    private int selectedPosition = -1;
    public static final String PATH_UPDATE_VIBRATION = "/update_vibration";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        LV_items = view.findViewById(R.id.LV_items);

        //Init list title + description
        TextView tvTitle = view.findViewById(R.id.TV_title_list);
        TextView tvSubtitle = view.findViewById(R.id.TV_subtitle);
        ImageView ivIcon = view.findViewById(R.id.IV_title_icon_list);

        tvTitle.setText("Vibration Type");
        tvSubtitle.setText("Select your preferred vibration pattern for alerts");
        ivIcon.setImageResource(R.drawable.ic_vibration);

        HashMap<String, Vibration> temp = DBManager.getInstance().getVibrationsList().getList();
        for (Map.Entry<String, Vibration> entry : temp.entrySet()) {
            vibrations.add(entry.getValue());
        }

        //Load selected vibration option
        String savedVibration = SPManager.getInstance().getPreferredVibration();
        if (savedVibration != null) {
            for (int i = 0; i < vibrations.size(); i++) {
                if (vibrations.get(i).getName().equals(savedVibration)) {
                    selectedPosition = i; //Mark selected vibration option
                    break;
                }
            }
        }

        setupListView();
        return view;
    }

    private void setupListView() {
        adapter = new ArrayAdapter<Vibration>(requireContext(), R.layout.item_vibration, vibrations) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_vibration, parent, false);
                }

                Vibration vib = getItem(position);

                TextView name = convertView.findViewById(R.id.VI_name);
                RadioButton radio = convertView.findViewById(R.id.VI_radio);

                name.setText(vib.getName());
                radio.setChecked(position == selectedPosition);

                radio.setOnClickListener(v -> {
                    selectedPosition = position;
                    DBManager.getInstance().updateUserVibration(vib)
                            .addOnCompleteListener(task -> {
                                    SPManager.getInstance().setPreferredVibration(vib.getName());
                                    sendVibrationUpdateToWatch(getContext(), vib.getName());

                });
                    notifyDataSetChanged();
                });

                //Vibration demonstration
                convertView.setOnClickListener(v -> {
                    Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        VibrationEffect effect = VibrationEffect.createWaveform(Vibration.toLongArray(vib.getPattern()), -1);
                        vibrator.vibrate(effect);
                    }
                });

                return convertView;
            }
        };

        LV_items.setAdapter(adapter);
    }

    private void sendVibrationUpdateToWatch(Context context, String message) {
        Wearable.getNodeClient(context).getConnectedNodes().addOnSuccessListener(nodes -> {
            if (nodes.isEmpty()) {
                Log.e("SEND_TO_WATCH", "❌ No connected nodes — cannot send message!");
            } else {
                for (com.google.android.gms.wearable.Node node : nodes) {
                    Wearable.getMessageClient(context).sendMessage(
                            node.getId(),
                            PATH_UPDATE_VIBRATION,
                            message.getBytes()
                    ).addOnSuccessListener(aVoid -> {
                        Log.d("SEND_TO_WATCH", "✅ Message sent to " + node.getDisplayName());
                    }).addOnFailureListener(e -> {
                        Log.e("SEND_TO_WATCH", "❌ Failed to send message: " + e.getMessage());
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("SEND_TO_WATCH", "❌ Failed to get connected nodes: " + e.getMessage());
        });
    }
}
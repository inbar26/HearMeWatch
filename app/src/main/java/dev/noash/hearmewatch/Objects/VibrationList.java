package dev.noash.hearmewatch.Objects;

import java.util.HashMap;
import java.util.ArrayList;

public class VibrationList {

    private HashMap<String, Vibration> list = new HashMap<>();

    public VibrationList() {
    }

    public VibrationList(ArrayList<Vibration> vibrations) {
        list.clear();
        for (Vibration v : vibrations) {
            list.put(v.getName(), v);
        }
    }

    public HashMap<String, Vibration> getList() {
        return list;
    }
    public void setList(HashMap<String, Vibration> list) {
        this.list = list;
    }
}

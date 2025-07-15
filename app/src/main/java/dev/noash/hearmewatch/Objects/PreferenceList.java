package dev.noash.hearmewatch.Objects;

import java.util.HashMap;
import java.util.ArrayList;

public class PreferenceList {

    private HashMap<String, Preference> list = new HashMap<>();

    public PreferenceList() {
    }

    public PreferenceList(ArrayList<Preference> preferences) {
        list.clear();
        for(Preference p : preferences) {
            list.put(p.getName(), p);
        }
    }

    public HashMap<String, Preference> getList() {
        return list;
    }
    public void setList(HashMap<String, Preference> list) {
        this.list = list;
    }
}

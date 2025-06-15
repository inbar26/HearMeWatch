package dev.noash.hearmewatch.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class PreferenceList {

    private HashMap<String, MyPreference> list = new HashMap<>();

    public PreferenceList() {
    }

    public PreferenceList(ArrayList<MyPreference> myP) {
        list.clear();
        for(MyPreference p : myP) {
            list.put(p.getName(), p);
        }
    }

    public HashMap<String, MyPreference> getList() {
        return list;
    }
    public void setList(HashMap<String, MyPreference> list) {
        this.list = list;
    }
}

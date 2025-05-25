package dev.noash.hearmewatch.Models;


import java.util.ArrayList;
import java.util.HashMap;

public class PreferenceList {

    private HashMap<String, MyPreference> list = new HashMap<>();

    public PreferenceList() {
        list.put("Name Calling", new MyPreference("Name Calling", true));
        list.put("Dog Barking", new MyPreference("Dog Barking", false));
        list.put("Baby Crying", new MyPreference("Baby Crying", false));
        list.put("Ambulance Siren", new MyPreference("Ambulance Siren", true));
        list.put("Fire Alarm", new MyPreference("Fire Alarm", false));
        list.put("Door Knock", new MyPreference("Door Knock", true));
    }

    public PreferenceList(ArrayList<MyPreference> myP) {
        list.clear();
        for(MyPreference p : myP) {
            list.put(p.getName(), p);
        }
    }

    public void update(MyPreference mpP) {
        list.put(mpP.getName(), mpP);
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i=0;i<list.size();i++) {
            s.append(list.get(i).toString());
        }
        return s.toString();
    }

    public HashMap<String, MyPreference> getList() {
        return list;
    }
}

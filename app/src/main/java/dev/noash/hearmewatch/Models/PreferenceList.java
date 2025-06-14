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

    public void setList(HashMap<String, MyPreference> list) {
        this.list = list;
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

    public void initList() {
        list.put("Name Calling", new MyPreference("Name Calling", true));
        list.put("Dog", new MyPreference("Dog", false));
        list.put("Baby cry", new MyPreference("Baby cry", false));
        list.put("Ambulance (siren)", new MyPreference("Ambulance (siren)", true));
        list.put("Fire alarm", new MyPreference("Fire alarm", false));
        list.put("Doorbell", new MyPreference("Doorbell", true));
    }
}

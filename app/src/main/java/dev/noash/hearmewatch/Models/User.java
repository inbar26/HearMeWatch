package dev.noash.hearmewatch.Models;

import dev.noash.hearmewatch.Models.PreferenceList;
;
import java.util.ArrayList;

public class User {
    private String id;
    private String Name;
    private String email;
    private PreferenceList myPreferences = new PreferenceList();
    public User() {}

    public User(User user) {
        this.id = user.getId();
        this.Name = user.getName();
        this.email = user.getEmail();
        this.myPreferences = user.getMyPreferences();
    }

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public PreferenceList getMyPreferences() {
        return myPreferences;
    }

    public void setMyPreferences(PreferenceList myPreferences) {
        this.myPreferences = myPreferences;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void initMyPreferences() {
        ArrayList<MyPreference> defPreList = new ArrayList<>();
        defPreList.add(new MyPreference("Name Calling", true));
        defPreList.add(new MyPreference("Dog", false));
        defPreList.add(new MyPreference("Baby cry", false));
        defPreList.add(new MyPreference("Ambulance (siren)", true));
        defPreList.add(new MyPreference("Fire alarm", false));
        defPreList.add(new MyPreference("Doorbell", true));
        this.myPreferences = new PreferenceList(defPreList);
    }
}

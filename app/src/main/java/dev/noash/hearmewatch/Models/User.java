package dev.noash.hearmewatch.Models;

import java.util.ArrayList;

public class User {
    private String id;
    private String fName;
    private String lName;
    private String email;
    private PreferenceList myPreferences = new PreferenceList();
    public User() {}

    public User(User user) {
        this.id = user.getId();
        this.fName = user.getfName();
        this.lName = user.getlName();
        this.email = user.getEmail();
        this.myPreferences = user.getMyPreferences();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getfName() {
        return fName;
    }
    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }
    public void setlName(String lName) {
        this.lName = lName;
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

    public void setName(String fName, String lName) {
        this.fName = fName;
        this.lName = lName;
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

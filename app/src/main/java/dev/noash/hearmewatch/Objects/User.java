package dev.noash.hearmewatch.Objects;

import java.util.ArrayList;

public class User {
    private String id;
    private String fName;
    private String lName;
    private String email;

    private String profileImageUrl;
    private PreferenceList myPreferences = new PreferenceList();
    private String chosenVibration;
    public User() {}

    public User(User user) {
        this.id = user.getId();
        this.fName = user.getfName();
        this.lName = user.getlName();
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfileImageUrl();
        this.myPreferences = user.getMyPreferences();
        this.chosenVibration = user.getChosenVibration();
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public PreferenceList getMyPreferences() {
        return myPreferences;
    }
    public void setMyPreferences(PreferenceList myPreferences) {
        this.myPreferences = myPreferences;
    }

    public String getChosenVibration() {
        return chosenVibration;
    }
    public void setChosenVibration(String chosenVibration) {
        this.chosenVibration = chosenVibration;
    }
    public void setName(String fName, String lName) {
        this.fName = fName;
        this.lName = lName;
    }
    public String getName() {
        StringBuilder fullName = new StringBuilder();

        if (fName != null && !fName.trim().isEmpty()) {
            fullName.append(fName.trim());
        }

        if (lName != null && !lName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lName.trim());
        }

        return fullName.toString();
    }

    public void initMyPreferences() {
        ArrayList<Preference> defPreList = new ArrayList<>();
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.NAME_CALLING), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.CIVIL_DEFENSE), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.AMBULANCE_SIREN), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.FIRE_ALARM), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.POLICE_SIREN), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.CAR_HORN), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.INTERCOM), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.DOOR_KNOCK), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.BABY_CRYING), true));
        defPreList.add(new Preference(SoundLabel.getDisplayName(SoundLabel.DOG_BARKING), true));

        this.myPreferences = new PreferenceList(defPreList);
    }
}

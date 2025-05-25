package dev.noash.hearmewatch.Models;

import dev.noash.hearmewatch.Models.PreferenceList;
;
import java.util.ArrayList;

public class User {
        private String id;
        private String Name;
        private String email;
        private PreferenceList myPreferences;
        public User() {}
        public User(User user) {
            this.id = user.getId();
            this.Name = user.getName();
            this.email = user.getEmail();
            this.myPreferences = new PreferenceList();
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

}

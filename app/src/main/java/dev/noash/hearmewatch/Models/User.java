package dev.noash.hearmewatch.Models;


import java.util.ArrayList;

public class User {
        private String id;
        private String fName;
        private String lName;
        private String email;
        private ArrayList<MyPreference> myMyPreferences = new ArrayList<>();
        public User() {}

        public String getId() {
            return id;
        }

        public User setId(String id) {
            this.id = id;
            return this;
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

    public ArrayList<MyPreference> getMyMyPreferences() {
        return myMyPreferences;
    }

    public void setMyMyPreferences(ArrayList<MyPreference> myMyPreferences) {
        this.myMyPreferences = myMyPreferences;
    }
}

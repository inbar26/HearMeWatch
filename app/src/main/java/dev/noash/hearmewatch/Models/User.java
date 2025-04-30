package dev.noash.hearmewatch.Models;


import java.util.ArrayList;

public class User {
        private String id;
        private String fName;
        private String lName;
        private String email;
        private ArrayList<preference> myPreferences = new ArrayList<>();
        public User() {}

        public String getId() {
            return id;
        }

        public User setId(String id) {
            this.id = id;
            return this;
        }



    }

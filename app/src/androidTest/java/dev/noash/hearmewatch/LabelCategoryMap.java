package dev.noash.hearmewatch;

import java.util.HashMap;
import java.util.Map;

public class LabelCategoryMap {

    public static final Map<String, String> LABEL_TO_CATEGORY_MAP = new HashMap<>();

    static {
        // DOG_BARKING
        LABEL_TO_CATEGORY_MAP.put("Dog", "dog");
        LABEL_TO_CATEGORY_MAP.put("Bark", "dog");
        LABEL_TO_CATEGORY_MAP.put("Whimper (dog)", "dog");

        // BABY_CRY
        LABEL_TO_CATEGORY_MAP.put("Whimper", "Baby cry, infant cry");
        LABEL_TO_CATEGORY_MAP.put("Crying, sobbing", "Baby cry, infant cry");

        // AMBULANCE_SIREN
        LABEL_TO_CATEGORY_MAP.put("Ambulance (siren)", "ambulance (siren)");
        LABEL_TO_CATEGORY_MAP.put("Emergency vehicle", "ambulance (siren)");

        // POLICE_SIREN
        LABEL_TO_CATEGORY_MAP.put("Police car (siren)", "police car (siren)");

        // CAR_HORN
        LABEL_TO_CATEGORY_MAP.put("Car alarm", "car alarm");
        LABEL_TO_CATEGORY_MAP.put("Toot", "car alarm");
        LABEL_TO_CATEGORY_MAP.put("Vehicle horn, car horn, honking", "car alarm");


        // CIVIL_DEFENSE - we need to add
        LABEL_TO_CATEGORY_MAP.put("Civil defense siren", "civil_defense");
        LABEL_TO_CATEGORY_MAP.put("Alarm", "civil_defense");

        // DOOR_KNOCK
        LABEL_TO_CATEGORY_MAP.put("Knock", "door");
        LABEL_TO_CATEGORY_MAP.put("Door", "door");

        // INTERCOM
        LABEL_TO_CATEGORY_MAP.put("Doorbell", "doorbell");
        LABEL_TO_CATEGORY_MAP.put("Ding-dong", "doorbell");
        LABEL_TO_CATEGORY_MAP.put("Bell", "doorbell");
        LABEL_TO_CATEGORY_MAP.put("Jingle bell", "doorbell");
        LABEL_TO_CATEGORY_MAP.put("Ding", "doorbell");

        // FIRE_ALARM
        LABEL_TO_CATEGORY_MAP.put("Fire alarm", "fire alarm");
        LABEL_TO_CATEGORY_MAP.put("Fire engine, fire truck (siren)", "fire alarm");
        LABEL_TO_CATEGORY_MAP.put("Smoke detector, smoke alarm", "fire alarm");
    }

    public static String getCategory(String label) {
        return LABEL_TO_CATEGORY_MAP.getOrDefault(label, null);
    }


}

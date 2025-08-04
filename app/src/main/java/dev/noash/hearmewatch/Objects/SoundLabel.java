package dev.noash.hearmewatch.Objects;

public enum SoundLabel {

    NAME_CALLING("Name Calling"),
    AMBULANCE_SIREN("Ambulance Siren"),
    FIRE_ALARM("Fire Alarm"),
    POLICE_SIREN("Police Siren"),
    CAR_HORN("Car Horn"),
    INTERCOM("Intercom"),
    DOOR_KNOCK("Door Knock"),
    BABY_CRYING("Baby Crying"),
    DOG_BARKING("Dog Barking");

    private final String displayName;

    SoundLabel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String getDisplayName(SoundLabel label) {
        return label.getDisplayName();
    }
}

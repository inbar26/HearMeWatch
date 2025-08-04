package dev.noash.hearmewatch.Objects;

public enum SoundLabel {

    NAME_CALLING("Name Calling"),
    EMERGENCY_VEHICLE("Emergency Vehicle"),
    FIRE_ALARM("Fire Alarm"),
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

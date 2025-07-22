package dev.noash.hearmewatch;

public enum VibrationPattern {

    DOUBLE_TAP("Quick Tap", new long[]{0L, 150L}),
    LONG_VIBRATION("Double Pulse", new long[]{0L, 150L, 100L, 150L}),
    SHORT_LONG("Steady Alarm", new long[]{0L, 300L, 200L, 300L}),
    HEARTBEAT("Bold Buzz", new long[]{0L, 600L}),
    DEFAULT("Default", new long[]{0L, 400L});

    private final String displayName;
    private final long[] pattern;

    VibrationPattern(String displayName, long[] pattern) {
        this.displayName = displayName;
        this.pattern = pattern;
    }

    public long[] getPattern() {
        return pattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VibrationPattern fromString(String input) {
        if (input == null) return DEFAULT;

        for (VibrationPattern vp : values()) {
            if (vp.name().equalsIgnoreCase(input) || vp.displayName.equalsIgnoreCase(input)) {
                return vp;
            }
        }

        return DEFAULT;
    }

    public static long[] getPatternByName(String name) {
        return fromString(name).getPattern();
    }
    public static String getDisplayName(VibrationPattern label) {
        return label.getDisplayName();
    }
}

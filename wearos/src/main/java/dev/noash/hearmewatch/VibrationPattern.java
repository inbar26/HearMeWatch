package dev.noash.hearmewatch;

public enum VibrationPattern {

    DOUBLE_TAP("Double Tap", new long[]{0, 100, 100, 100}),
    LONG_VIBRATION("Long Vibration", new long[]{0, 600}),
    SHORT_LONG("Short-Long", new long[]{0, 100, 100, 400}),
    HEARTBEAT("Heartbeat", new long[]{0, 150, 100, 150}),

    DRUMROLL("Drumroll", new long[]{0, 80, 80, 80, 80, 80, 80}),
    STEADY_PULSE("Steady Pulse", new long[]{0, 200, 200, 200}),
    ALARM_PULSE("Alarm Pulse", new long[]{0, 300, 200, 300, 200, 300}),

    BREATHE("Breathe", new long[]{0, 400, 400, 600}),

    EMERGENCY("Emergency", new long[]{0, 500, 100, 500, 100, 500}),
    RAPID_FIRE("Rapid Fire", new long[]{0, 100, 50, 100, 50, 100, 50, 100}),
    PANIC_PULSE("Panic Pulse", new long[]{0, 200, 50, 200, 50, 200, 50, 200}),

    SMS_STYLE("SMS Style", new long[]{0, 150, 100, 150}),

    DEFAULT("Default", new long[]{0, 400});

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
}

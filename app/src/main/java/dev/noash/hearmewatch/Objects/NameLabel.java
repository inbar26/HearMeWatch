package dev.noash.hearmewatch.Objects;

public enum NameLabel {

    NOA("noa", 65f),
    INBAR("inbar", 55f),
    YOSSI("yossi", 45f),
    DANIEL("daniel", 35f),
    YONATAN("yonatan", 35f),
    AVIGAIL("avigail", 30f);

    private final String displayName;
    private final float minConfidence;

    NameLabel(String displayName, float minConfidence) {
        this.displayName = displayName;
        this.minConfidence = minConfidence;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMinConfidence() {
        return minConfidence;
    }

    public static NameLabel fromLabel(String label) {
        for (NameLabel name : values()) {
            if (name.displayName.equalsIgnoreCase(label)) {
                return name;
            }
        }
        return null;
    }
}

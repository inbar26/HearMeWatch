package dev.noash.hearmewatch.Objects;

import java.util.List;

public class Vibration {

    private String name;
    private List<Long> pattern;

    public Vibration() {
    }

    public Vibration(String name, List<Long> pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getPattern() {
        return pattern;
    }
    public void setPattern(List<Long> pattern) {
        this.pattern = pattern;
    }

    public static long[] toLongArray(List<Long> list) {
        long[] arr = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}

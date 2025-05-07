package dev.noash.hearmewatch.Models;

public class MyPreference {
    private String name;
    private Boolean isActive;
    public MyPreference() {
    }

    public MyPreference(String name, Boolean isActive) {
        this.name = name;
        this.isActive = isActive;

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean getActive() {
        return isActive;
    }
    public void setActive(Boolean active) {
        isActive = active;
    }
}

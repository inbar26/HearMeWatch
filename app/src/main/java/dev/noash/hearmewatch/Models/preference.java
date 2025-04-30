package dev.noash.hearmewatch.Models;

public class preference {
    private String type;
    private Boolean isActive;
    public preference() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}

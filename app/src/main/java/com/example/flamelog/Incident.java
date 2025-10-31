package com.example.flamelog;

public class Incident {
    private String title;
    private String description;
    private String level;
    private int iconResId;

    public Incident(String title, String description, String level, int iconResId) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.iconResId = iconResId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLevel() { return level; }
    public int getIconResId() { return iconResId; }
}

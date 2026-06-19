package com.example.apptvxemphim;

public class Movie {
    private String title;
    private long duration;
    private String description;
    private String poster;

    public Movie(){}

    public Movie(String title, long duration, String description, String poster) {
        this.title = title;
        this.duration = duration;
        this.description = description;
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
}

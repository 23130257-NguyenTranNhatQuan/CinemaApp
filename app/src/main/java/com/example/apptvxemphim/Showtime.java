package com.example.apptvxemphim;
import com.google.firebase.firestore.DocumentId;

public class Showtime {
    @DocumentId private String showtimeId;
    private String movieId;
    private String cinemaId;
    private String hallId;
    private String date;
    private String time;
    private String language;
    private long price;

    public Showtime() {}

    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String v) { showtimeId = v; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String v) { movieId = v; }

    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String v) { cinemaId = v; }
    public String getHallId() { return hallId; }
    public void setHallId(String v) { hallId = v; }
    public String getDate() { return date; }
    public void setDate(String v) { date = v; }
    public String getTime() { return time; }
    public void setTime(String v) { time = v; }
    public String getLanguage() { return language; }
    public void setLanguage(String v) { language = v; }
    public long getPrice() { return price; }
    public void setPrice(long v) { price = v; }
}
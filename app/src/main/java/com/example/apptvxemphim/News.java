package com.example.apptvxemphim;

import com.google.firebase.firestore.DocumentId;

public class News {
    @DocumentId
    private String id;
    private String title;
    private String imageUrl;
    private String content;

    public News() {}

    public News(String title, String imageUrl, String content) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

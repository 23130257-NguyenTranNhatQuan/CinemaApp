package com.example.apptvxemphim;

public class Banner {
    private String banner;
    private int order;

    public Banner() {}

    public Banner(String banner, int order) {
        this.banner = banner;
        this.order = order;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

package com.example.apptvxemphim;
import com.google.firebase.firestore.DocumentId;

public class Cinema {
    @DocumentId private String cinemaId;
    private String name;
    private String address;
    private String logo;
    private String brand;
    private String phone;
    private String photo;
    private String ggmap;

    public Cinema() {}

    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String v) { cinemaId = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }

    public String getLogo() { return logo; }
    public void setLogo(String v) { logo = v; }
    public String getBrand() { return brand; }
    public void setBrand(String v) { brand = v; }

    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getPhoto() { return photo; }
    public void setPhoto(String v) { photo = v; }
    public String getGgmap() { return ggmap; }
    public void setGgmap(String v) { ggmap = v; }

}

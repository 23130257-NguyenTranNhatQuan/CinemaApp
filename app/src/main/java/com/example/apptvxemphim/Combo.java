package com.example.apptvxemphim;
import com.google.firebase.firestore.DocumentId;
import android.os.Parcel;
import android.os.Parcelable;

public class Combo implements Parcelable {
    @DocumentId private String comboId;
    private String category;
    private String desc;
    private String imageUrl;
    private String name;
    private long price;
    private int quantity;
    private boolean isHeader;

    public Combo() {}

    protected Combo(Parcel in) {
        comboId = in.readString();
        category = in.readString();
        desc = in.readString();
        imageUrl = in.readString();
        name = in.readString();
        price = in.readLong();
        quantity = in.readInt();
    }

    public static final Creator<Combo> CREATOR = new Creator<Combo>() {
        @Override
        public Combo createFromParcel(Parcel in) {
            return new Combo(in);
        }

        @Override
        public Combo[] newArray(int size) {
            return new Combo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comboId);
        dest.writeString(category);
        dest.writeString(desc);
        dest.writeString(imageUrl);
        dest.writeString(name);
        dest.writeLong(price);
        dest.writeInt(quantity);
    }

    public String getComboId() { return comboId; }
    public void setComboId(String v) { comboId = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category = v; }
    public String getDesc() { return desc; }
    public void setDesc(String v) { desc = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public long getPrice() { return price; }
    public void setPrice(long v) { price = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { quantity = v; }
    public boolean isHeader() { return isHeader; }
    public void setHeader(boolean v) { isHeader = v; }
    
    public static Combo createHeader(String category) {
        Combo header = new Combo();
        header.setHeader(true);
        header.setCategory(category);
        return header;
    }
}

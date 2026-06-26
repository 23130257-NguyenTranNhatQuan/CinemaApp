    package com.example.apptvxemphim;

    import android.os.Parcel;
    import android.os.Parcelable;

    public class Combo implements Parcelable {
        public String name;
        public String category;
        public long price;
        public int quantity;
        public String desc;
        public String imageUrl;
        public boolean isHeader;

        // Constructor mặc định
        public Combo() {}

        // Constructor cho Header
        public static Combo createHeader(String title) {
            Combo c = new Combo();
            c.name = title;
            c.isHeader = true;
            return c;
        }

        // Constructor cho món ăn (Item)
        public Combo(String name, long price, String desc, String imageUrl) {
            this.name = name;
            this.price = price;
            this.desc = desc;
            this.imageUrl = imageUrl;
            this.quantity = 0;
            this.isHeader = false;
        }

        protected Combo(Parcel in) {
            name = in.readString();
            price = in.readLong();
            quantity = in.readInt();
            desc = in.readString();
            imageUrl = in.readString();
            isHeader = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeLong(price);
            dest.writeInt(quantity);
            dest.writeString(desc);
            dest.writeString(imageUrl);
            dest.writeByte((byte) (isHeader ? 1 : 0));
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<Combo> CREATOR = new Creator<Combo>() {
            @Override
            public Combo createFromParcel(Parcel in) { return new Combo(in); }
            @Override
            public Combo[] newArray(int size) { return new Combo[size]; }
        };
    }
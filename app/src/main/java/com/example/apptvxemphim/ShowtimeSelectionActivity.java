package com.example.apptvxemphim;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ShowtimeSelectionActivity extends AppCompatActivity {

    LinearLayout btnDate17, btnDate18, btnDate19;
    TextView tvNum17, tvTxt17, tvNum18, tvTxt18, tvNum19, tvTxt19;

    LinearLayout btnBrandAll, btnBrandCGV, btnBrandBeta;
    TextView imgAll, imgCGV, imgBeta;

    LinearLayout layoutCinemaList;
    LinearLayout itemBranch1, itemBranch2;
    TextView tvBranchName1, tvBranchAddress1, tvBranchName2, tvBranchAddress2;
    LinearLayout layoutShowtimes1, layoutShowtimes2;

    LinearLayout btnLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime_selection);

        // Đặt ID cho ô Hà Nội trong XML nếu chưa có (ví dụ android:id="@+id/btnLocation")
        btnLocation = findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, btnLocation);
            popup.getMenu().add("Hà Nội");
            popup.getMenu().add("Hồ Chí Minh");
            popup.getMenu().add("Đà Nẵng");
            popup.setOnMenuItemClickListener(item -> {
                // Bấm vào tỉnh nào thì đổi chữ hiển thị thành tỉnh đó
                ((TextView)((LinearLayout)btnLocation).getChildAt(1)).setText(item.getTitle());
                return true;
            });
            popup.show();
        });

        // Ánh xạ thành phần chọn ngày
        btnDate17 = findViewById(R.id.btnDate17);
        btnDate18 = findViewById(R.id.btnDate18);
        btnDate19 = findViewById(R.id.btnDate19);
        tvNum17 = findViewById(R.id.tvNum17);
        tvTxt17 = findViewById(R.id.tvTxt17);
        tvNum18 = findViewById(R.id.tvNum18);
        tvTxt18 = findViewById(R.id.tvTxt18);
        tvNum19 = findViewById(R.id.tvNum19);
        tvTxt19 = findViewById(R.id.tvTxt19);

        // Ánh xạ thành phần hãng rạp
        btnBrandAll = findViewById(R.id.btnBrandAll);
        btnBrandCGV = findViewById(R.id.btnBrandCGV);
        btnBrandBeta = findViewById(R.id.btnBrandBeta);
        imgAll = findViewById(R.id.imgAll);
        imgCGV = findViewById(R.id.imgCGV);
        imgBeta = findViewById(R.id.imgBeta);

        // Ánh xạ danh sách rạp và suất chiếu
        layoutCinemaList = findViewById(R.id.layoutCinemaList);

        itemBranch1 = findViewById(R.id.itemBranch1);
        itemBranch2 = findViewById(R.id.itemBranch2);
        tvBranchName1 = findViewById(R.id.tvBranchName1);
        tvBranchAddress1 = findViewById(R.id.tvBranchAddress1);
        tvBranchName2 = findViewById(R.id.tvBranchName2);
        tvBranchAddress2 = findViewById(R.id.tvBranchAddress2);
        layoutShowtimes1 = findViewById(R.id.layoutShowtimes1);
        layoutShowtimes2 = findViewById(R.id.layoutShowtimes2);

        // --- 1. XỬ LÝ BẤM CHỌN NGÀY ---
        btnDate17.setOnClickListener(v -> selectDate(17));
        btnDate18.setOnClickListener(v -> selectDate(18));
        btnDate19.setOnClickListener(v -> selectDate(19));

        // --- 2. XỬ LÝ BẤM CHỌN HÃNG RẠP ---
        btnBrandAll.setOnClickListener(v -> filterCinema("ALL"));
        btnBrandCGV.setOnClickListener(v -> filterCinema("CGV"));
        btnBrandBeta.setOnClickListener(v -> filterCinema("BETA"));

        // --- 3. XỬ LÝ BẤM CHI NHÁNH HIỆN GIỜ CHIẾU ---
        // --- 3. XỬ LÝ BẤM CHI NHÁNH HIỆN GIỜ CHIẾU (ĐÓNG/MỞ TẠI CHỖ) ---
        itemBranch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu đang hiện thì ẩn đi, nếu đang ẩn thì hiện lên
                if (layoutShowtimes1.getVisibility() == View.VISIBLE) {
                    layoutShowtimes1.setVisibility(View.GONE);
                } else {
                    layoutShowtimes1.setVisibility(View.VISIBLE);
                    layoutShowtimes2.setVisibility(View.GONE); // Bấm rạp 1 thì tự động đóng rạp 2 lại
                }
            }
        });

        itemBranch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutShowtimes2.getVisibility() == View.VISIBLE) {
                    layoutShowtimes2.setVisibility(View.GONE);
                } else {
                    layoutShowtimes2.setVisibility(View.VISIBLE);
                    layoutShowtimes1.setVisibility(View.GONE); // Bấm rạp 2 thì tự động đóng rạp 1 lại
                }
            }
        });
    }

    private void selectDate(int date) {
        // Reset toàn bộ màu nền ngày về trắng
        btnDate17.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        tvNum17.setTextColor(Color.parseColor("#333333")); tvTxt17.setTextColor(Color.parseColor("#888888"));

        btnDate18.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        tvNum18.setTextColor(Color.parseColor("#333333")); tvTxt18.setTextColor(Color.parseColor("#888888"));

        btnDate19.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        tvNum19.setTextColor(Color.parseColor("#333333")); tvTxt19.setTextColor(Color.parseColor("#888888"));

        // Đổi màu ngày được chọn thành Hồng Active
        if (date == 17) {
            btnDate17.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63")));
            tvNum17.setTextColor(Color.WHITE); tvTxt17.setTextColor(Color.WHITE);
        } else if (date == 18) {
            btnDate18.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63")));
            tvNum18.setTextColor(Color.WHITE); tvTxt18.setTextColor(Color.WHITE);
        } else if (date == 19) {
            btnDate19.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63")));
            tvNum19.setTextColor(Color.WHITE); tvTxt19.setTextColor(Color.WHITE);
        }
    }

    private void filterCinema(String brand) {
        // Quay lại hiển thị danh sách rạp và ẩn chi tiết giờ chiếu cũ đi
        layoutCinemaList.setVisibility(View.VISIBLE);
        layoutShowtimes1.setVisibility(View.GONE);
        layoutShowtimes2.setVisibility(View.GONE);

        // Reset màu nền các nút Hãng rạp
        imgAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE)); imgAll.setTextColor(Color.parseColor("#E91E63"));
        imgCGV.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE)); imgCGV.setTextColor(Color.parseColor("#E51937"));
        imgBeta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE)); imgBeta.setTextColor(Color.parseColor("#00BCD4"));

        if (brand.equals("ALL")) {
            imgAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63"))); imgAll.setTextColor(Color.WHITE);
            itemBranch1.setVisibility(View.VISIBLE); tvBranchName1.setText("Beta Giải Phóng"); tvBranchAddress1.setText("Tầng 3, tòa IP2, Chung cư Imperial, số 360 Giải Phóng, Hà Nội");
            itemBranch2.setVisibility(View.VISIBLE); tvBranchName2.setText("CGV Aeon Mall Hà Đông"); tvBranchAddress2.setText("Tầng 3, TTTM Aeon Mall Hà Đông, Hà Nội");
        } else if (brand.equals("CGV")) {
            imgCGV.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63"))); imgCGV.setTextColor(Color.WHITE);
            itemBranch1.setVisibility(View.VISIBLE); tvBranchName1.setText("CGV Aeon Mall Hà Đông"); tvBranchAddress1.setText("Tầng 3, TTTM Aeon Mall Hà Đông, Hà Nội");
            itemBranch2.setVisibility(View.GONE); // Chỉ hiện 1 chi nhánh CGV mẫu
        } else if (brand.equals("BETA")) {
            imgBeta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63"))); imgBeta.setTextColor(Color.WHITE);
            itemBranch1.setVisibility(View.VISIBLE); tvBranchName1.setText("Beta Giải Phóng"); tvBranchAddress1.setText("Tầng 3, tòa IP2, Chung cư Imperial, số 360 Giải Phóng, Hà Nội");
            itemBranch2.setVisibility(View.GONE); // Chỉ hiện 1 chi nhánh Beta mẫu
        }
    }


}
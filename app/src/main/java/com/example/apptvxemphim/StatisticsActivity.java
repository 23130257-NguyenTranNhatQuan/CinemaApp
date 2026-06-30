package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalOrders;
    private LinearLayout layoutMovieRevenue;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        layoutMovieRevenue = findViewById(R.id.layoutMovieRevenue);
        db = FirebaseFirestore.getInstance();

        calculateStatistics();
    }

    private void calculateStatistics() {
        // Lấy toàn bộ đơn hàng từ bảng booking
        db.collection("Booking").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                long totalRevenue = 0;
                int totalOrders = 0;

                // Dùng HashMap để gom nhóm doanh thu theo từng tên phim
                HashMap<String, Long> movieRevenueMap = new HashMap<>();

                for (DocumentSnapshot doc : task.getResult()) {
                    String status = doc.getString("status");

                    // Chỉ tính tiền các đơn đã thanh toán hoặc đã check-in
                    if ("Đã thanh toán".equals(status) || "Đã sử dụng".equals(status)) {
                        long price = doc.getLong("totalPrice") != null ? doc.getLong("totalPrice") : 0;
                        String movieTitle = doc.getString("movieTitle");
                        if (movieTitle == null || movieTitle.isEmpty()) movieTitle = "Khác";

                        totalRevenue += price;
                        totalOrders++;

                        // Cộng dồn doanh thu cho phim này
                        long currentMovieTotal = movieRevenueMap.getOrDefault(movieTitle, 0L);
                        movieRevenueMap.put(movieTitle, currentMovieTotal + price);
                    }
                }

                // Cập nhật lên UI (Tổng quan)
                tvTotalRevenue.setText(String.format("%,d đ", totalRevenue));
                tvTotalOrders.setText(totalOrders + " đơn");

                // Cập nhật lên UI (Danh sách từng phim)
                renderMovieRevenueList(movieRevenueMap);

            } else {
                Toast.makeText(this, "Lỗi khi lấy dữ liệu thống kê!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tạo giao diện động cho danh sách doanh thu từng phim
    private void renderMovieRevenueList(HashMap<String, Long> map) {
        layoutMovieRevenue.removeAllViews();

        if (map.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có dữ liệu doanh thu.");
            layoutMovieRevenue.addView(tvEmpty);
            return;
        }

        for (Map.Entry<String, Long> entry : map.entrySet()) {
            // Tạo một khung CardView nhỏ cho mỗi phim
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setRadius(12f);
            card.setCardElevation(4f);

            // Giao diện bên trong CardView
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(30, 30, 30, 30);

            TextView tvName = new TextView(this);
            tvName.setText("Phim: " + entry.getKey());
            tvName.setTextSize(16f);
            tvName.setTextColor(getResources().getColor(android.R.color.black));

            TextView tvPrice = new TextView(this);
            tvPrice.setText("Doanh thu: " + String.format("%,d đ", entry.getValue()));
            tvPrice.setTextSize(16f);
            tvPrice.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvPrice.setPadding(0, 8, 0, 0);

            innerLayout.addView(tvName);
            innerLayout.addView(tvPrice);
            card.addView(innerLayout);

            // Đẩy vào màn hình
            layoutMovieRevenue.addView(card);
        }
    }
}
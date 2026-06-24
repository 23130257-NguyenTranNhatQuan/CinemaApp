package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardMovies, cardCinemas, cardTickets, cardUsers, cardStats;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // File XML đã tạo ở bước trước

        // 1. Ánh xạ giao diện
        cardMovies = findViewById(R.id.card_manage_movies);
        cardCinemas = findViewById(R.id.card_manage_cinemas);
        cardTickets = findViewById(R.id.card_manage_tickets);
        cardUsers = findViewById(R.id.card_manage_users);
        cardStats = findViewById(R.id.card_statistics);
        btnLogout = findViewById(R.id.btn_admin_logout);

        // 2. Bắt sự kiện Click cho từng khối chức năng
        cardMovies.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageMoviesActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Đang mở Quản lý Phim...", Toast.LENGTH_SHORT).show();
        });

        cardCinemas.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở Quản lý Rạp...", Toast.LENGTH_SHORT).show();
        });

        cardTickets.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở Quản lý Vé...", Toast.LENGTH_SHORT).show();
        });

        cardUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở Quản lý Người dùng...", Toast.LENGTH_SHORT).show();
        });

        cardStats.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở Thống kê Doanh thu...", Toast.LENGTH_SHORT).show();
        });

        // 3. Xử lý Đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Đã đăng xuất khỏi tài khoản Admin", Toast.LENGTH_SHORT).show();

            // Quay về trang Login và xóa các trang admin khỏi lịch sử để không ấn Back lại được
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
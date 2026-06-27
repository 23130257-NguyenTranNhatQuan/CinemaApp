package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName;
    private Button btnViewHistory;
    private Button btnLogout;
    private BottomNavigationView bottomNavigationView; // Thêm biến cho thanh điều hướng

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ ID từ file XML
        tvName = findViewById(R.id.tvName);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Ánh xạ thanh điều hướng

        // Gọi hàm tải dữ liệu ngay khi vừa mở màn hình
        loadUserProfile();

        // ---------------- XỬ LÝ BOTTOM NAVIGATION ---------------- //
        // Bật sáng icon "Tài khoản" vì người dùng đang ở trang Profile
        bottomNavigationView.setSelectedItemId(R.id.nav_account);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Chuyển về trang chủ và đóng trang Profile lại
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển trang để có cảm giác liền mạch
                finish();
                return true;
            } else if (id == R.id.nav_ticket) {
                startActivity(new Intent(ProfileActivity.this, CinemaListActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                Toast.makeText(this, "Chuyển sang Tin tức", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_account) {
                // Đang ở sẵn trang tài khoản rồi thì không làm gì cả
                return true;
            }
            return false;
        });
        // --------------------------------------------------------- //

        // Xử lý nút xem lịch sử
        btnViewHistory.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Chức năng Lịch sử đang phát triển...", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Đăng xuất an toàn
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Xóa bộ nhớ đệm màn hình và văng ra Login
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        // Kiểm tra xem đã có người đăng nhập chưa
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Rút dữ liệu từ Firestore
            db.collection("User").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("full_name");

                            if (fullName != null) {
                                tvName.setText(fullName);
                            } else {
                                tvName.setText("Khách hàng");
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show();
                            tvName.setText("Lỗi dữ liệu");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvName.setText("Lỗi kết nối");
                    });
        } else {
            tvName.setText("Chưa đăng nhập");
            // Ép văng ra Login nếu chưa đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
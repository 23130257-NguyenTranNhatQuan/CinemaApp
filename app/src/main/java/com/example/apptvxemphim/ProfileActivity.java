package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName;
    private Button btnViewHistory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Đảm bảo tên file giao diện XML của bạn đúng ở đây

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ ID từ file XML
        tvName = findViewById(R.id.tvName);
        btnViewHistory = findViewById(R.id.btnViewHistory);

        // Gọi hàm tải dữ liệu ngay khi vừa mở màn hình
        loadUserProfile();

        // (Tùy chọn) Xử lý nút xem lịch sử đặt vé sau này nhóm bạn làm
        btnViewHistory.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Chức năng Lịch sử đang phát triển...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        // Kiểm tra xem đã có người đăng nhập chưa
        if (mAuth.getCurrentUser() != null) {
            // Lấy ID của user hiện tại
            String userId = mAuth.getCurrentUser().getUid();

            // Chọc vào Database -> Collection "User" -> Tìm Document có tên là ID của user
            db.collection("User").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // Nếu tìm thấy dữ liệu trên Database
                        if (documentSnapshot.exists()) {

                            // Lấy chính xác trường "full_name" như cấu trúc bạn đã chụp
                            String fullName = documentSnapshot.getString("full_name");

                            // Gắn dữ liệu lên màn hình (thay cho chữ "Đang tải...")
                            if (fullName != null) {
                                tvName.setText(fullName);
                            } else {
                                tvName.setText("Khách hàng");
                            }

                        } else {
                            Toast.makeText(ProfileActivity.this, "Không tìm thấy dữ liệu trên Database!", Toast.LENGTH_SHORT).show();
                            tvName.setText("Lỗi dữ liệu");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvName.setText("Lỗi kết nối");
                    });
        } else {
            // Nếu vô tình mở màn hình này mà chưa đăng nhập
            tvName.setText("Chưa đăng nhập");
        }
    }
}
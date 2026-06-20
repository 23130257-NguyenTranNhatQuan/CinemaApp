package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ ID
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Xử lý nút ĐĂNG KÝ
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // 1. Kiểm tra dữ liệu đầu vào
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng điền đủ tất cả thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Tạo tài khoản trên Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                // 3. LƯU DỮ LIỆU LÊN FIRESTORE (Khớp 100% cấu trúc Database của bạn)
                                String userId = mAuth.getCurrentUser().getUid();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // Tạo Tên ngắn gọn (Ví dụ: Tạm thời dùng fullName, sau này nhóm có thể tách chuỗi sau)
                                String shortName = fullName;

                                // Đóng gói dữ liệu đúng tên trường
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("full_name", fullName);
                                userData.put("password", password);
                                userData.put("user", shortName);

                                // Đẩy lên Collection "User"
                                db.collection("User").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                            finish(); // Đóng trang đăng ký, về lại trang đăng nhập
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });

                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi";
                                Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Xử lý nút TRỞ VỀ ĐĂNG NHẬP
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
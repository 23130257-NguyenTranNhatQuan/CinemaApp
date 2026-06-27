package com.example.apptvxemphim;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

                // 1. Kiểm tra rỗng
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Kiểm tra định dạng Email hợp lệ
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Định dạng Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                    etEmail.requestFocus();
                    return;
                }

                // 3. Kiểm tra độ dài mật khẩu
                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
                    etPassword.requestFocus();
                    return;
                }

                // 4. Kiểm tra xác nhận mật khẩu
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                    etConfirmPassword.requestFocus();
                    return;
                }

                // 5. Tiến hành tạo tài khoản trên Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();
                                    String shortName = fullName.substring(0, 1).toUpperCase();

                                    // Tạo dữ liệu để lưu lên Firestore
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("full_name", fullName);
                                    userData.put("email", email);
                                    userData.put("password", password);
                                    userData.put("user", shortName);

                                    // LƯU LÊN FIRESTORE TRƯỚC
                                    db.collection("User").document(userId)
                                            .set(userData)
                                            .addOnSuccessListener(aVoid -> {

                                                // LƯU THÀNH CÔNG -> GỬI EMAIL XÁC THỰC
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(verifyTask -> {
                                                            if (verifyTask.isSuccessful()) {
                                                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng kiểm tra hộp thư (hoặc Thư rác) để xác thực.", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                String errorMsg = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Lỗi không xác định";
                                                                Toast.makeText(RegisterActivity.this, "Không thể gửi email xác thực: " + errorMsg, Toast.LENGTH_LONG).show();
                                                            }

                                                            // Bất kể gửi mail lỗi hay thành công, đều phải đăng xuất và về trang Đăng nhập
                                                            mAuth.signOut();
                                                            finish();
                                                        });

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                            });
                                }
                            } else {
                                // Báo lỗi nếu trùng email hoặc lỗi tạo tài khoản
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
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
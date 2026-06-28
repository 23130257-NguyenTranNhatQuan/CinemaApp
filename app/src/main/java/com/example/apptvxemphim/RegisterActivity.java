package com.example.apptvxemphim;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private TextView tvEmailError, tvPasswordError, tvConfirmPasswordError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isConfirmPasswordValid = false;

    // Regex patterns
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$");

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
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        // Setup real-time validation
        setupEmailValidation();
        setupPasswordValidation();
        setupConfirmPasswordValidation();

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

                // 2. Kiểm tra email phải là @gmail.com
                if (!GMAIL_PATTERN.matcher(email).matches()) {
                    tvEmailError.setVisibility(View.VISIBLE);
                    tvEmailError.setText("Email phải có đuôi @gmail.com");
                    etEmail.requestFocus();
                    return;
                }

                // 3. Kiểm tra mật khẩu: từ 6 ký tự, có chữ thường, chữ hoa và số
                if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    tvPasswordError.setVisibility(View.VISIBLE);
                    tvPasswordError.setText("Mật khẩu phải từ 6 ký tự, có chữ thường, chữ hoa và số");
                    etPassword.requestFocus();
                    return;
                }

                // 4. Kiểm tra xác nhận mật khẩu
                if (!password.equals(confirmPassword)) {
                    tvConfirmPasswordError.setVisibility(View.VISIBLE);
                    tvConfirmPasswordError.setText("Mật khẩu xác nhận không khớp");
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
                                    userData.put("role", "user");

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

    private void setupEmailValidation() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    tvEmailError.setVisibility(View.GONE);
                    isEmailValid = false;
                } else if (!GMAIL_PATTERN.matcher(email).matches()) {
                    tvEmailError.setVisibility(View.VISIBLE);
                    tvEmailError.setText("Email phải có đuôi @gmail.com");
                    isEmailValid = false;
                } else {
                    tvEmailError.setVisibility(View.GONE);
                    isEmailValid = true;
                }
            }
        });
    }

    private void setupPasswordValidation() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString().trim();
                if (password.isEmpty()) {
                    tvPasswordError.setVisibility(View.GONE);
                    isPasswordValid = false;
                } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    tvPasswordError.setVisibility(View.VISIBLE);
                    tvPasswordError.setText("Mật khẩu phải từ 6 ký tự, có chữ thường, chữ hoa và số");
                    isPasswordValid = false;
                } else {
                    tvPasswordError.setVisibility(View.GONE);
                    isPasswordValid = true;
                }
                // Re-check confirm password when password changes
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                if (!confirmPassword.isEmpty()) {
                    validateConfirmPassword(confirmPassword);
                }
            }
        });
    }

    private void setupConfirmPasswordValidation() {
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String confirmPassword = s.toString().trim();
                validateConfirmPassword(confirmPassword);
            }
        });
    }

    private void validateConfirmPassword(String confirmPassword) {
        String password = etPassword.getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            tvConfirmPasswordError.setVisibility(View.GONE);
            isConfirmPasswordValid = false;
        } else if (!confirmPassword.equals(password)) {
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            tvConfirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            isConfirmPasswordValid = false;
        } else {
            tvConfirmPasswordError.setVisibility(View.GONE);
            isConfirmPasswordValid = true;
        }
    }
}

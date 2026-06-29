package com.example.apptvxemphim;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
    
    // Email configuration
    private static final String EMAIL_SENDER = "nxtien207@gmail.com";
    private static final String EMAIL_PASSWORD = "qcyw xlxl hgwg vyji";

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

                                                // GỬI EMAIL XÁC THỰC
                                                sendVerificationEmail(email, fullName);

                                                // LƯU THÀNH CÔNG -> CHUYỂN ĐẾN TRANG CHỦ
                                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                finish();
                                            });
                                }
                            } else {
                                // Báo lỗi nếu trùng email hoặc lỗi tạo tài khoản
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                                
                                // Nếu lỗi là "email đã được sử dụng", kiểm tra xem có phải user đã bị xóa không
                                if (error != null && error.contains("email address is already in use")) {
                                    checkAndHandleDeletedUser(email, fullName, password);
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + error, Toast.LENGTH_LONG).show();
                                }
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
                    // Kiểm tra email đã được sử dụng chưa
                    checkEmailExists(email);
                }
            }
        });
    }

    private void checkEmailExists(String email) {
        // Kiểm tra Firestore TRƯỚC - xem email có bị xóa/disabled không
        db.collection("User")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasActiveUser = false;
                        for (var doc : task.getResult()) {
                            Boolean disabled = doc.getBoolean("disabled");
                            String docEmail = doc.getString("email");
                            
                            // Nếu email trong doc khác với email đang kiểm tra → đã bị đổi (xóa)
                            if (docEmail != null && !docEmail.equals(email)) {
                                hasActiveUser = false;
                                break;
                            }
                            // Nếu user bị disabled
                            else if (disabled != null && disabled) {
                                hasActiveUser = false;
                                break;
                            }
                            // User active
                            else {
                                hasActiveUser = true;
                            }
                        }
                        
                        // Nếu Firestore có user active với email này → không cho đăng ký
                        if (hasActiveUser) {
                            tvEmailError.setVisibility(View.VISIBLE);
                            tvEmailError.setText("Email này đã được sử dụng");
                            isEmailValid = false;
                            return;
                        }
                    }
                    
                    // Firestore không có user active → kiểm tra Firebase Auth
                    checkEmailInAuth(email);
                });
    }

    private void checkEmailInAuth(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        
                        // Nếu email tồn tại trong Auth nhưng Firestore đã xóa → cho phép đăng ký lại
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // Email tồn tại trong Auth nhưng đã bị xóa khỏi Firestore
                            // Cho phép đăng ký lại (Firebase Auth sẽ tự xử lý)
                            tvEmailError.setVisibility(View.GONE);
                            isEmailValid = true;
                        } else {
                            // Email không tồn tại ở đâu
                            tvEmailError.setVisibility(View.GONE);
                            isEmailValid = true;
                        }
                    } else {
                        // Nếu lỗi khi kiểm tra, vẫn cho phép đăng ký
                        tvEmailError.setVisibility(View.GONE);
                        isEmailValid = true;
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

    private void checkAndHandleDeletedUser(String email, String fullName, String password) {
        // Kiểm tra xem email có thuộc về user đã bị xóa/disabled không
        db.collection("User")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Có document với email này
                        for (var doc : task.getResult()) {
                            Boolean disabled = doc.getBoolean("disabled");
                            String docEmail = doc.getString("email");
                            
                            // Nếu user bị disabled hoặc email đã bị đổi
                            if ((disabled != null && disabled) || (docEmail != null && !docEmail.equals(email))) {
                                // Đây là user đã bị xóa
                                // Firebase Auth vẫn giữ email, không thể đăng ký lại từ client
                                Toast.makeText(this, 
                                    "Email này đã được sử dụng bởi tài khoản đã bị xóa. " +
                                    "Do giới hạn của Firebase, email này không thể đăng ký lại. " +
                                    "Vui lòng sử dụng email khác.", 
                                    Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                    
                    // Không tìm thấy user bị xóa → email thực sự đang được sử dụng
                    Toast.makeText(this, "Email này đã được sử dụng", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendVerificationEmail(String email, String fullName) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_SENDER));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Chào mừng bạn đến với Cinema App!");

                String emailContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                        + "<h2 style='color: #4A2B73; text-align: center;'>Chào mừng " + fullName + "!</h2>"
                        + "<p>Cảm ơn bạn đã đăng ký tài khoản Cinema App.</p>"
                        + "<p>Tài khoản của bạn đã được tạo thành công.</p>"
                        + "<p>Bạn có thể đăng nhập và đặt vé xem phim ngay bây giờ.</p>"
                        + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                        + "<p style='color: #999; font-size: 12px; text-align: center;'>Trân trọng,<br>Cinema App</p></div>";
                message.setContent(emailContent, "text/html; charset=utf-8");

                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

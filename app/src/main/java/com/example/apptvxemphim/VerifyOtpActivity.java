package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private TextView tvOtpError, tvResendOtp, tvOtpInfo;

    private FirebaseFirestore db;
    private String userId, email, fullName;
    private String generatedOtp;
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;

    // 2 phút = 120000 milliseconds
    private static final long OTP_EXPIRY_MS = 120000;

    // Sử dụng EmailJS API (miễn phí, đáng tin cậy)
    private static final String EMAILJS_SERVICE_ID = "service_cinema";
    private static final String EMAILJS_TEMPLATE_ID = "template_otp";
    private static final String EMAILJS_USER_ID = "user_xxx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ RegisterActivity
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        fullName = getIntent().getStringExtra("fullName");

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpError = findViewById(R.id.tvOtpError);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvOtpInfo = findViewById(R.id.tvOtpInfo);

        // Tạo OTP và gửi email
        generateAndSendOtp();
        startResendTimer();

        btnVerifyOtp.setOnClickListener(v -> {
            String otpInput = etOtp.getText().toString().trim();
            if (otpInput.isEmpty()) {
                tvOtpError.setVisibility(View.VISIBLE);
                tvOtpError.setText("Vui lòng nhập mã OTP");
                return;
            }
            if (otpInput.length() != 6) {
                tvOtpError.setVisibility(View.VISIBLE);
                tvOtpError.setText("Mã OTP phải gồm 6 số");
                return;
            }
            verifyOtp(otpInput);
        });

        tvResendOtp.setOnClickListener(v -> {
            if (isResendEnabled) {
                generateAndSendOtp();
                startResendTimer();
                Toast.makeText(this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendTimer() {
        isResendEnabled = false;
        tvResendOtp.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvResendOtp.setEnabled(false);
        tvResendOtp.setClickable(false);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(OTP_EXPIRY_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvResendOtp.setText("Gửi lại mã OTP (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                isResendEnabled = true;
                tvResendOtp.setText("Gửi lại mã OTP");
                tvResendOtp.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                tvResendOtp.setEnabled(true);
                tvResendOtp.setClickable(true);
            }
        }.start();
    }

    private void generateAndSendOtp() {
        // Tạo OTP 6 số
        Random random = new Random();
        generatedOtp = String.format("%06d", random.nextInt(999999));

        // Hiển thị OTP trên màn hình ngay lập tức
        tvOtpInfo.setText("Mã OTP của bạn: " + generatedOtp + "\n(Đang gửi email đến: " + email + ")");

        // Lưu OTP và thời gian hết hạn lên Firestore
        long expiryTime = System.currentTimeMillis() + OTP_EXPIRY_MS;
        db.collection("User").document(userId)
                .update("otp", generatedOtp, "otpExpiry", expiryTime)
                .addOnSuccessListener(aVoid -> {
                    // Gửi email OTP
                    sendOtpEmail();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOtpEmail() {
        new Thread(() -> {
            try {
                // Sử dụng EmailJS API để gửi email
                String url = "https://api.emailjs.com/api/v1.0/email/send";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Origin", "http://localhost");
                con.setDoOutput(true);

                // Tạo JSON payload
                String jsonPayload = "{"
                        + "\"service_id\":\"" + EMAILJS_SERVICE_ID + "\","
                        + "\"template_id\":\"" + EMAILJS_TEMPLATE_ID + "\","
                        + "\"user_id\":\"" + EMAILJS_USER_ID + "\","
                        + "\"template_params\":{"
                        + "\"to_email\":\"" + email + "\","
                        + "\"to_name\":\"" + fullName + "\","
                        + "\"otp_code\":\"" + generatedOtp + "\","
                        + "\"expiry_time\":\"2 phút\""
                        + "}"
                        + "}";

                OutputStream os = con.getOutputStream();
                os.write(jsonPayload.getBytes());
                os.flush();
                os.close();

                int responseCode = con.getResponseCode();
                Log.d("EmailJS", "Response Code: " + responseCode);

                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        tvOtpInfo.setText("Mã OTP đã được gửi đến email: " + email);
                        Toast.makeText(this, "Mã OTP 6 số đã được gửi đến email của bạn", Toast.LENGTH_LONG).show();
                    } else {
                        tvOtpInfo.setText("Mã OTP của bạn: " + generatedOtp + "\n(Email chưa gửi được, vui lòng nhập OTP trên màn hình)");
                        Toast.makeText(this, "Không thể gửi email. Vui lòng nhập OTP hiển thị trên màn hình.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("EmailJS", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    tvOtpInfo.setText("Mã OTP của bạn: " + generatedOtp + "\n(Email lỗi, nhập OTP trên màn hình)");
                    Toast.makeText(this, "Lỗi gửi email. Mã OTP hiển thị trên màn hình.", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void verifyOtp(String otpInput) {
        db.collection("User").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            String storedOtp = doc.getString("otp");
                            Long expiryTimestamp = doc.getLong("otpExpiry");

                            // Kiểm tra OTP hết hạn
                            if (expiryTimestamp != null && System.currentTimeMillis() > expiryTimestamp) {
                                tvOtpError.setVisibility(View.VISIBLE);
                                tvOtpError.setText("Mã OTP đã hết hạn, vui lòng nhấn 'Gửi lại mã OTP'");
                                return;
                            }

                            if (storedOtp != null && storedOtp.equals(otpInput)) {
                                // OTP chính xác -> cập nhật trạng thái verified
                                db.collection("User").document(userId)
                                        .update("otpVerified", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                            // Chuyển đến trang chủ
                                            Intent intent = new Intent(VerifyOtpActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Lỗi xác thực: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                tvOtpError.setVisibility(View.VISIBLE);
                                tvOtpError.setText("Mã OTP không chính xác, vui lòng thử lại");
                            }
                        } else {
                            tvOtpError.setVisibility(View.VISIBLE);
                            tvOtpError.setText("Không tìm thấy thông tin tài khoản");
                        }
                    } else {
                        tvOtpError.setVisibility(View.VISIBLE);
                        tvOtpError.setText("Lỗi kiểm tra OTP: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
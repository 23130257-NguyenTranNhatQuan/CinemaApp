package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        TextView tvStatus = findViewById(R.id.tvPaymentStatus);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        boolean isSuccess = getIntent().getBooleanExtra("IS_SUCCESS", false);
        if (isSuccess) {
            tvStatus.setText("THANH TOÁN THÀNH CÔNG!\nVé đã được gửi vào Email.");
        } else {
            tvStatus.setText("THANH TOÁN THẤT BẠI!\nVui lòng thử lại.");
        }

        btnBackHome.setOnClickListener(v -> {
            // Quay về màn hình chính (ví dụ: MainActivity)
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
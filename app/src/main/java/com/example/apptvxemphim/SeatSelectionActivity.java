package com.example.apptvxemphim;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SeatSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nạp giao diện sơ đồ chọn ghế của bạn
        setContentView(R.layout.activity_seat_selection);
    }
}
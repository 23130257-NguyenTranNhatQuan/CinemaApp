package com.example.apptvxemphim;

import com.google.firebase.firestore.*;
import java.util.HashSet;
import java.util.Set;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import android.view.View;

public class SeatSelectionActivity extends AppCompatActivity {

    private SeatMapView seatMapView;
    private TextView tvSelectedSeatNames, tvTotalPrice;

    private ImageButton btnClearSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar); // Ẩn Actionbar mặc định
        setContentView(R.layout.activity_seat_selection);

        seatMapView = findViewById(R.id.seatMapView);
        tvSelectedSeatNames = findViewById(R.id.tvSelectedSeatNames);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnClearSeats = findViewById(R.id.btnClearSeats);
        btnClearSeats.setOnClickListener(v -> {
            seatMapView.clearAllSelected();
            tvSelectedSeatNames.setText("Chỗ ngồi");
            tvTotalPrice.setText("0đ");
            btnClearSeats.setVisibility(View.GONE);
        });

        // Khởi tạo dữ liệu map ghế y hệt trong ảnh
        String showtimeId = getIntent().getStringExtra("SHOWTIME_ID");
        if (showtimeId == null) { finish(); return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Showtime").document(showtimeId).get()
                .addOnSuccessListener(stDoc -> {
                    String hallId = stDoc.getString("hallId");
                    if (hallId == null) {
                        // Chưa có hallId thì dùng sơ đồ mặc định 8x10
                        buildSeatMap(8, 10, new HashSet<>());
                        return;
                    }
                    db.collection("Hall").document(hallId).get()
                            .addOnSuccessListener(hallDoc -> {
                                int rows = hallDoc.getLong("rows") != null ? hallDoc.getLong("rows").intValue() : 8;
                                int cols = hallDoc.getLong("cols") != null ? hallDoc.getLong("cols").intValue() : 10;

                                db.collection("bookedSeats").document(showtimeId).get()
                                        .addOnSuccessListener(bookedDoc -> {
                                            Set<String> booked = new HashSet<>();
                                            if (bookedDoc.exists() && bookedDoc.getData() != null)
                                                booked.addAll(bookedDoc.getData().keySet());
                                            buildSeatMap(rows, cols, booked);
                                        });
                            });
                });

        // Bắt sự kiện chọn ghế tính tiền
        seatMapView.setOnSeatSelectedListener(new SeatMapView.OnSeatSelectedListener() {
            @Override
            public void onSeatSelectionChanged(List<SeatMapView.Seat> selectedSeats) {
                if (selectedSeats.isEmpty()) {
                    tvSelectedSeatNames.setText("Chỗ ngồi");
                    tvTotalPrice.setText("0đ");
                    btnClearSeats.setVisibility(View.GONE);
                    return;
                }

                StringBuilder names = new StringBuilder("Chỗ ngồi: ");
                long totalPrice = 0;

                for (int i = 0; i < selectedSeats.size(); i++) {
                    SeatMapView.Seat seat = selectedSeats.get(i);
                    names.append(seat.name);
                    if (i < selectedSeats.size() - 1) names.append(", ");

                    // Tính tiền tạm tính theo loại ghế ví dụ
                    if (seat.type == 1) totalPrice += 80000;      // Thường
                    else if (seat.type == 2) totalPrice += 110000; // VIP
                    else if (seat.type == 3) totalPrice += 220000; // Đôi
                }

                tvSelectedSeatNames.setText(names.toString());
                tvTotalPrice.setText(String.format("%,dđ", totalPrice));
                btnClearSeats.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void buildSeatMap(int rows, int cols, Set<String> booked) {
        String[] labels = {"A","B","C","D","E","F","G","H","I","J"};
        List<SeatMapView.Seat> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String name = labels[r] + (c + 1);
                int type = (r == rows - 1) ? 3 : (r >= rows / 2) ? 2 : 1;
                list.add(new SeatMapView.Seat(name, type, booked.contains(name), r, c));
            }
        }
        seatMapView.setSeats(list);
    }
}
package com.example.apptvxemphim;

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
        List<SeatMapView.Seat> list = new ArrayList<>();

        // Hàng A (Chỉ có 12 ghế, từ col 0 đến 11 - loại Thường = 1)
        for (int i = 0; i < 12; i++) {
            list.add(new SeatMapView.Seat("A" + (12 - i), 1, false, 0, i));
        }

        // Hàng B (Có 10 ghế, loại Thường = 1)
        for (int i = 0; i < 10; i++) {
            list.add(new SeatMapView.Seat("B" + (10 - i), 1, false, 1, i));
        }

        // Hàng C (Có 13 ghế, loại Thường = 1)
        for (int i = 0; i < 13; i++) {
            list.add(new SeatMapView.Seat("C" + (13 - i), 1, false, 2, i));
        }

        // Hàng D (Hỗn hợp VIP đỏ và Đã đặt xám)
        // Ghế D8, D7, D6, D5 đã đặt (isBooked = true)
        for (int i = 0; i < 12; i++) {
            int seatNum = 12 - i;
            boolean isBooked = (seatNum >= 5 && seatNum <= 8);
            list.add(new SeatMapView.Seat("D" + seatNum, 2, isBooked, 3, i));
        }

        // Hàng E (VIP)
        for (int i = 0; i < 9; i++) {
            int seatNum = 9 - i;
            boolean isBooked = (seatNum >= 5 && seatNum <= 7);
            list.add(new SeatMapView.Seat("E" + seatNum, 2, isBooked, 4, i));
        }

        // Hàng F (VIP)
        for (int i = 0; i < 8; i++) {
            int seatNum = 8 - i;
            boolean isBooked = (seatNum == 5 || seatNum == 6);
            list.add(new SeatMapView.Seat("F" + seatNum, 2, isBooked, 5, i));
        }

        // Hàng G (10 ghế VIP đỏ rực rỡ không ai đặt)
        for (int i = 0; i < 10; i++) {
            list.add(new SeatMapView.Seat("G" + (10 - i), 2, false, 6, i));
        }

        // Hàng H (10 ghế VIP)
        for (int i = 0; i < 10; i++) {
            list.add(new SeatMapView.Seat("H" + (10 - i), 2, false, 7, i));
        }

        // Hàng I (Ghế đôi màu hồng = Loại 3, gồm 4 block đôi)
        list.add(new SeatMapView.Seat("I8", 3, false, 8, 0));
        list.add(new SeatMapView.Seat("I7", 3, false, 8, 1));
        list.add(new SeatMapView.Seat("I6", 3, false, 8, 3)); // tạo khoảng hở lối đi giữa các block ghế đôi bằng cách nhảy cột cách quãng
        list.add(new SeatMapView.Seat("I5", 3, false, 8, 4));
        list.add(new SeatMapView.Seat("I4", 3, false, 8, 6));
        list.add(new SeatMapView.Seat("I3", 3, false, 8, 7));
        list.add(new SeatMapView.Seat("I2", 3, false, 8, 9));
        list.add(new SeatMapView.Seat("I1", 3, false, 8, 10));

        seatMapView.setSeats(list);

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
}
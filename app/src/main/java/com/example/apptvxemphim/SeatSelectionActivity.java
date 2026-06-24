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
        TextView tvMovieName = findViewById(R.id.tvMovieName);
        TextView tvShowtimeInfo = findViewById(R.id.tvShowtimeInfo);

        String title       = getIntent().getStringExtra("MOVIE_TITLE");
        String time        = getIntent().getStringExtra("SHOWTIME_TIME");
        String date        = getIntent().getStringExtra("SHOWTIME_DATE");
        String lang        = getIntent().getStringExtra("SHOWTIME_LANG");
        String hallIdExtra = getIntent().getStringExtra("HALL_ID");

        if (tvMovieName != null)
            tvMovieName.setText(title != null ? title : "");

// Tính thứ
        String thuStr = "";
        try {
            java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date d = inFmt.parse(date);
            java.text.SimpleDateFormat thuFmt = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("vi", "VN"));
            String raw = thuFmt.format(d);
            thuStr = raw.substring(0, 1).toUpperCase() + raw.substring(1);
        } catch (Exception e) { thuStr = ""; }

        String shortDate = date != null && date.length() >= 5 ? date.substring(0, 5) : "";
        final String thuFinal = thuStr;
        final String shortDateFinal = shortDate;

        FirebaseFirestore dbInfo = FirebaseFirestore.getInstance();
        dbInfo.collection("Hall").document(hallIdExtra != null ? hallIdExtra : "").get()
                .addOnSuccessListener(hallDoc -> {
                    String hallName = hallDoc.getString("name") != null ? hallDoc.getString("name") : "Phòng chiếu";
                    dbInfo.collection("Movie").whereEqualTo("title", title).limit(1).get()
                            .addOnSuccessListener(movieSnap -> {
                                String endTime = "";
                                if (!movieSnap.isEmpty()) {
                                    Long duration = movieSnap.getDocuments().get(0).getLong("duration");
                                    if (duration != null && time != null) {
                                        try {
                                            String[] parts = time.split(":");
                                            int totalMin = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]) + duration.intValue();
                                            endTime = String.format("%02d:%02d", totalMin / 60 % 24, totalMin % 60);
                                        } catch (Exception ignored) {}
                                    }
                                }
                                String format = movieSnap.isEmpty() ? "2D" :
                                        (movieSnap.getDocuments().get(0).getString("format") != null ?
                                                movieSnap.getDocuments().get(0).getString("format") : "2D");

                                String info = time + " ~ " + endTime
                                        + " · " + thuFinal + ", " + shortDateFinal
                                        + " · " + hallName
                                        + " · " + format + " " + lang;
                                if (tvShowtimeInfo != null) tvShowtimeInfo.setText(info);
                            });
                });
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
                    String hallIdFromDoc = stDoc.getString("hallId");
                    if (hallIdFromDoc  == null) {
                        // Chưa có hallId thì dùng sơ đồ mặc định 8x10
                        buildSeatMap(8, 10, new HashSet<>());
                        return;
                    }
                    db.collection("Hall").document(hallIdFromDoc).get()
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
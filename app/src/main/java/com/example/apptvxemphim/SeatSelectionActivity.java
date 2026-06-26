package com.example.apptvxemphim;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class SeatSelectionActivity extends AppCompatActivity {

    private SeatMapView seatMapView;
    private TextView tvSelectedSeatNames, tvTotalPrice;
    private ImageButton btnClearSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        setContentView(R.layout.activity_seat_selection);

        seatMapView = findViewById(R.id.seatMapView);
        TextView tvMovieName = findViewById(R.id.tvMovieName);
        TextView tvShowtimeInfo = findViewById(R.id.tvShowtimeInfo);

        String title       = getIntent().getStringExtra("MOVIE_TITLE");
        String time        = getIntent().getStringExtra("SHOWTIME_TIME");
        String date        = getIntent().getStringExtra("SHOWTIME_DATE");
        String lang        = getIntent().getStringExtra("SHOWTIME_LANG");
        String hallIdExtra = getIntent().getStringExtra("HALL_ID");

        if (tvMovieName != null) tvMovieName.setText(title != null ? title : "");

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

        String showtimeId = getIntent().getStringExtra("SHOWTIME_ID");
        if (showtimeId == null) { finish(); return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Showtime").document(showtimeId).get()
                .addOnSuccessListener(stDoc -> {
                    String hallIdFromDoc = stDoc.getString("hallId");
                    String hallId = hallIdFromDoc != null ? hallIdFromDoc : hallIdExtra;
                    if (hallId == null) { finish(); return; }
                    loadHallAndRender(db, hallId, showtimeId);
                });

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

                    // Giá vé hardcode theo loại ghế (không lấy từ DB)
                    if (seat.type == 1) totalPrice += 80000;      // Thường
                    else if (seat.type == 2) totalPrice += 110000; // VIP
                    else if (seat.type == 3) totalPrice += 220000; // Đôi
                }

                tvSelectedSeatNames.setText(names.toString());
                tvTotalPrice.setText(String.format("%,dđ", totalPrice));
                btnClearSeats.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.btnBuyTicket).setOnClickListener(v -> {
            List<SeatMapView.Seat> selected = new ArrayList<>();
            for (SeatMapView.Seat s : seatMapView.getSeatList()) {
                if (s.isSelected) selected.add(s);
            }
            if (selected.isEmpty()) return;

            long seatTotal = 0;
            StringBuilder seatNames = new StringBuilder();
            for (int i = 0; i < selected.size(); i++) {
                SeatMapView.Seat s = selected.get(i);
                if (i > 0) seatNames.append(", ");
                seatNames.append(s.name);
                if (s.type == 1) seatTotal += 80000;
                else if (s.type == 2) seatTotal += 110000;
                else if (s.type == 3) seatTotal += 220000;
            }

            android.content.Intent intent = new android.content.Intent(this, ComboSelectionActivity.class);
            intent.putExtra("MOVIE_TITLE",   getIntent().getStringExtra("MOVIE_TITLE"));
            intent.putExtra("SHOWTIME_ID",   getIntent().getStringExtra("SHOWTIME_ID"));
            intent.putExtra("SHOWTIME_TIME", getIntent().getStringExtra("SHOWTIME_TIME"));
            intent.putExtra("SHOWTIME_DATE", getIntent().getStringExtra("SHOWTIME_DATE"));
            intent.putExtra("SHOWTIME_LANG", getIntent().getStringExtra("SHOWTIME_LANG"));
            intent.putExtra("HALL_ID",       getIntent().getStringExtra("HALL_ID"));
            intent.putExtra("SEAT_NAMES",    seatNames.toString());
            intent.putExtra("SEAT_TOTAL",    seatTotal);
            intent.putExtra("SEAT_COUNT",    selected.size());
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    private void loadHallAndRender(FirebaseFirestore db, String hallId, String showtimeId) {
        db.collection("Hall").document(hallId).get()
                .addOnSuccessListener(hallDoc -> {
                    if (!hallDoc.exists()) { finish(); return; }

                    Hall hall = new Hall();
                    hall.hallId = hallId;
                    hall.cinemaId = hallDoc.getString("cinemaId");
                    hall.name = hallDoc.getString("name");
                    hall.rows = hallDoc.getLong("rows") != null ? hallDoc.getLong("rows").intValue() : 8;
                    hall.cols = hallDoc.getLong("cols") != null ? hallDoc.getLong("cols").intValue() : 10;
                    hall.vipRows = hallDoc.getLong("vipRows") != null ? hallDoc.getLong("vipRows").intValue() : 0;
                    hall.coupleRows = hallDoc.getLong("coupleRows") != null ? hallDoc.getLong("coupleRows").intValue() : 0;

                    Map<String, Object> cz = (Map<String, Object>) hallDoc.get("centerZone");
                    if (cz != null) {
                        hall.centerStartRow = ((Long) cz.get("startRow")).intValue();
                        hall.centerEndRow = ((Long) cz.get("endRow")).intValue();
                        hall.centerStartCol = ((Long) cz.get("startCol")).intValue();
                        hall.centerEndCol = ((Long) cz.get("endCol")).intValue();
                    }

                    // Lấy override (ngoại lệ từng ghế) của phòng này
                    db.collection("HallOverrides").document(hallId).get()
                            .addOnSuccessListener(layoutDoc -> {
                                Map<String, Integer> overrides = new HashMap<>();
                                if (layoutDoc.exists() && layoutDoc.getData() != null) {
                                    for (Map.Entry<String, Object> e : layoutDoc.getData().entrySet()) {
                                        if (e.getValue() instanceof Long) {
                                            overrides.put(e.getKey(), ((Long) e.getValue()).intValue());
                                        }
                                    }
                                }

                                // Lấy ghế đã đặt theo showtime này
                                db.collection("BookedSeats").document(showtimeId).get()
                                        .addOnSuccessListener(bookedDoc -> {
                                            Set<String> booked = new HashSet<>();
                                            if (bookedDoc.exists() && bookedDoc.getData() != null) {
                                                booked.addAll(bookedDoc.getData().keySet());
                                            }
                                            seatMapView.setEditMode(false);
                                            seatMapView.generate(hall, overrides, booked);
                                        });
                            });
                });
    }
}
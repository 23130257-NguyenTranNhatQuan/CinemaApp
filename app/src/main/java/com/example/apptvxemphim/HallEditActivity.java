package com.example.apptvxemphim;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class HallEditActivity extends AppCompatActivity {

    private SeatMapView seatMapView;
    private Button btnToggleZoneSelect, btnSave;
    private TextView tvHint;
    private FirebaseFirestore db;
    private Hall hall;
    private boolean zoneSelectOn = false;

    private Button btnPaintNormal, btnPaintVip, btnPaintCouple, btnPaintEmpty;
    private TextView tvHallLabel;
    private int currentPaintType = 1; // mặc định: Thường

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_edit);

        db = FirebaseFirestore.getInstance();

        seatMapView = findViewById(R.id.seatMapViewEdit);
        btnToggleZoneSelect = findViewById(R.id.btn_toggle_zone_select);
        btnSave = findViewById(R.id.btn_save_hall);
        tvHint = findViewById(R.id.tv_edit_hint);

        tvHallLabel = findViewById(R.id.tv_hall_label);
        btnPaintNormal = findViewById(R.id.btn_paint_normal);
        btnPaintVip = findViewById(R.id.btn_paint_vip);
        btnPaintCouple = findViewById(R.id.btn_paint_couple);
        btnPaintEmpty = findViewById(R.id.btn_paint_empty);

        btnPaintNormal.setOnClickListener(v -> selectPaintType(1));
        btnPaintVip.setOnClickListener(v -> selectPaintType(2));
        btnPaintCouple.setOnClickListener(v -> selectPaintType(3));
        btnPaintEmpty.setOnClickListener(v -> selectPaintType(0));
        selectPaintType(1); // mặc định chọn Thường lúc mở màn

        findViewById(R.id.btn_back_hall_edit).setOnClickListener(v -> finish());

        seatMapView.setEditMode(true);

        String hallId = getIntent().getStringExtra("HALL_ID");
        if (hallId == null) { finish(); return; }

        loadHall(hallId);

        // Tap 1 ghế -> hiện dialog chọn loại
        seatMapView.setOnSeatEditListener(seat -> seatMapView.updateSeatType(seat, currentPaintType));

        // Kéo chọn vùng trung tâm xong -> lưu tạm vào hall (chưa ghi Firestore, đợi bấm Save)
        seatMapView.setOnCenterZoneChangeListener((startRow, endRow, startCol, endCol) -> {
            hall.centerStartRow = startRow;
            hall.centerEndRow = endRow;
            hall.centerStartCol = startCol;
            hall.centerEndCol = endCol;
            Toast.makeText(this, "Đã chọn vùng trung tâm, nhớ bấm Save để lưu", Toast.LENGTH_SHORT).show();
        });

        btnToggleZoneSelect.setOnClickListener(v -> {
            zoneSelectOn = !zoneSelectOn;
            seatMapView.setZoneSelectMode(zoneSelectOn);
            btnToggleZoneSelect.setText(zoneSelectOn ? "Đang chọn vùng trung tâm (bấm để xong)" : "Chọn vùng trung tâm");

                    tvHint.setText(zoneSelectOn
                            ? "Chạm vào ghế ở góc 1, rồi chạm vào ghế ở góc đối diện để chọn vùng"
                            : "Chọn loại ghế ở thanh dưới, rồi chạm/kéo qua nhiều ghế để đổi loại");
        });

        btnSave.setOnClickListener(v -> saveOverrides(hallId));
    }

    private void loadHall(String hallId) {
        db.collection("Hall").document(hallId).get()
                .addOnSuccessListener(hallDoc -> {
                    if (!hallDoc.exists()) { finish(); return; }
                    hall = Hall.fromDocument(hallDoc);

                    db.collection("Cinema").document(hall.cinemaId != null ? hall.cinemaId : "").get()
                            .addOnSuccessListener(cinemaDoc -> {
                                String cinemaName = cinemaDoc.getString("name") != null ? cinemaDoc.getString("name") : "Rạp";
                                tvHallLabel.setText(hall.name + " - " + cinemaName);
                            });

                    db.collection("HallOverrides").document(hallId).get()
                            .addOnSuccessListener(overrideDoc -> {
                                Map<String, Integer> overrides = new HashMap<>();
                                if (overrideDoc.exists() && overrideDoc.getData() != null) {
                                    for (Map.Entry<String, Object> e : overrideDoc.getData().entrySet()) {
                                        if (e.getValue() instanceof Long) {
                                            overrides.put(e.getKey(), ((Long) e.getValue()).intValue());
                                        }
                                    }
                                }
                                // Edit mode -> không cần check booked, truyền null
                                seatMapView.generate(hall, overrides, null);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    private void saveOverrides(String hallId) {
        Map<String, Integer> overrides = seatMapView.computeOverridesToSave(hall);
        Map<String, Object> overridesToSave = new HashMap<>(overrides); // Firestore set() cần Map<String,Object>

        // Ghi đè toàn bộ document HallOverrides bằng map mới (set thay vì update để xoá luôn
        // các override cũ đã không còn khác biệt với mặc định)
        db.collection("HallOverrides").document(hallId).set(overridesToSave)
                .addOnSuccessListener(v -> {
                    // Lưu centerZone (nếu có thay đổi) vào Hall
                    Map<String, Object> updates = new HashMap<>();
                    if (hall.centerStartRow != null) {
                        Map<String, Object> cz = new HashMap<>();
                        cz.put("startRow", hall.centerStartRow);
                        cz.put("endRow", hall.centerEndRow);
                        cz.put("startCol", hall.centerStartCol);
                        cz.put("endCol", hall.centerEndCol);
                        updates.put("centerZone", cz);
                    }
                    if (!updates.isEmpty()) {
                        db.collection("Hall").document(hallId).update(updates);
                    }
                    Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void selectPaintType(int type) {
        currentPaintType = type;
        btnPaintNormal.setAlpha(type == 1 ? 1f : 0.4f);
        btnPaintVip.setAlpha(type == 2 ? 1f : 0.4f);
        btnPaintCouple.setAlpha(type == 3 ? 1f : 0.4f);
        btnPaintEmpty.setAlpha(type == 0 ? 1f : 0.4f);
    }

}
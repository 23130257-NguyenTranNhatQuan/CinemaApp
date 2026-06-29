package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HallCreateActivity extends AppCompatActivity {

    private Spinner spinnerCinema;
    private EditText etName, etRows, etCols, etVipRows, etCoupleRows;
    private FirebaseFirestore db;
    private List<Cinema> cinemaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_create);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_hall_create).setOnClickListener(v -> finish());

        spinnerCinema = findViewById(R.id.spinner_cinema);
        etName = findViewById(R.id.et_hall_name);
        etRows = findViewById(R.id.et_hall_rows);
        etCols = findViewById(R.id.et_hall_cols);
        etVipRows = findViewById(R.id.et_hall_vip_rows);
        etCoupleRows = findViewById(R.id.et_hall_couple_rows);

        loadCinemas();

        Button btnGenerate = findViewById(R.id.btn_generate_hall);
        btnGenerate.setOnClickListener(v -> generateHall());
    }

    private void loadCinemas() {
        db.collection("Cinema").get().addOnSuccessListener(snapshot -> {
            cinemaList.clear();
            List<String> names = new ArrayList<>();

            cinemaList.add(null);          // null = placeholder "Chọn rạp"
            names.add("Chọn rạp");

            for (QueryDocumentSnapshot doc : snapshot) {
                Cinema c = doc.toObject(Cinema.class);
                cinemaList.add(c);
                names.add(c.getName() != null ? c.getName() : c.getCinemaId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, names) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((android.widget.TextView) v).setTextColor(getResources().getColor(R.color.white));
                    return v;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCinema.setAdapter(adapter);

            android.graphics.drawable.GradientDrawable spinnerBg = new android.graphics.drawable.GradientDrawable();
            spinnerBg.setColor(android.graphics.Color.parseColor("#222222"));
            spinnerBg.setCornerRadius(24f);
            spinnerBg.setStroke(2, android.graphics.Color.parseColor("#C9227A"));
            findViewById(R.id.container_spinner_cinema_create).setBackground(spinnerBg);

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải danh sách rạp: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void generateHall() {
        if (cinemaList.isEmpty() || cinemaList.size() <= 1) {
            Toast.makeText(this, "Chưa có rạp nào, vui lòng tạo rạp trước", Toast.LENGTH_SHORT).show();
            return;
        }
        int cinemaPos = spinnerCinema.getSelectedItemPosition();
        if (cinemaPos <= 0) {
            Toast.makeText(this, "Vui lòng chọn rạp", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        Integer rows = parseIntOrNull(etRows.getText().toString());
        Integer cols = parseIntOrNull(etCols.getText().toString());
        Integer vipRows = parseIntOrNull(etVipRows.getText().toString());
        Integer coupleRows = parseIntOrNull(etCoupleRows.getText().toString());

        if (name.isEmpty() || rows == null || cols == null) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên phòng, Số hàng, Số cột", Toast.LENGTH_SHORT).show();
            return;
        }
        if (vipRows == null) vipRows = 0;
        if (coupleRows == null) coupleRows = 0;
        if (vipRows + coupleRows > rows) {
            Toast.makeText(this, "Số hàng VIP + hàng đôi không được vượt quá tổng số hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rows > 14) {
            Toast.makeText(this, "Số hàng tối đa hỗ trợ là 14 (A-N)", Toast.LENGTH_SHORT).show();
            return;
        }

        Cinema selectedCinema = cinemaList.get(cinemaPos);

        Hall hall = new Hall(name, rows, cols, vipRows, coupleRows);
        hall.cinemaId = selectedCinema.getCinemaId();

        db.collection("Hall").add(hall.toFirestoreMap())
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Tạo phòng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tạo phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Integer parseIntOrNull(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}
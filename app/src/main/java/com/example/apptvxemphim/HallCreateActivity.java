package com.example.apptvxemphim;

import android.os.Bundle;
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
            for (QueryDocumentSnapshot doc : snapshot) {
                Cinema c = doc.toObject(Cinema.class);
                cinemaList.add(c);
                names.add(c.getName() != null ? c.getName() : c.getCinemaId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
            spinnerCinema.setAdapter(adapter);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải danh sách rạp: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void generateHall() {
        if (cinemaList.isEmpty()) {
            Toast.makeText(this, "Chưa có rạp nào, vui lòng tạo rạp trước", Toast.LENGTH_SHORT).show();
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

        Cinema selectedCinema = cinemaList.get(spinnerCinema.getSelectedItemPosition());

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
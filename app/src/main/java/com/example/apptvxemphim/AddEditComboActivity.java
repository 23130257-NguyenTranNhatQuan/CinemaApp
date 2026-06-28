package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddEditComboActivity extends AppCompatActivity {

    private EditText etName, etCategory, etDesc, etPrice, etImage;
    private Button btnSave;
    private TextView tvHeader;
    private String comboId = null;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_combo);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        tvHeader = findViewById(R.id.tv_add_edit_combo_header);
        etName = findViewById(R.id.et_combo_name);
        etCategory = findViewById(R.id.et_combo_category);
        etDesc = findViewById(R.id.et_combo_desc);
        etPrice = findViewById(R.id.et_combo_price);
        etImage = findViewById(R.id.et_combo_image);
        btnSave = findViewById(R.id.btn_save_combo);

        // Kiểm tra xem có ID truyền sang không (Chế độ Sửa)
        comboId = getIntent().getStringExtra("COMBO_ID");
        if (comboId != null) {
            tvHeader.setText("Chỉnh sửa Combo");
            loadComboData(comboId);
        } else {
            tvHeader.setText("Thêm Combo Mới");
        }

        btnSave.setOnClickListener(v -> saveComboToFirebase());
    }

    private void loadComboData(String id) {
        db.collection("combos").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Combo combo = doc.toObject(Combo.class);
                if (combo != null) {
                    etName.setText(combo.getName());
                    etCategory.setText(combo.getCategory());
                    etDesc.setText(combo.getDesc());
                    etPrice.setText(String.valueOf(combo.getPrice()));
                    etImage.setText(combo.getImageUrl());
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveComboToFirebase() {
        String name = etName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên, Danh mục và Giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long price = Long.parseLong(priceStr);

            Map<String, Object> comboData = new HashMap<>();
            comboData.put("name", name);
            comboData.put("category", category);
            comboData.put("desc", desc);
            comboData.put("price", price);
            comboData.put("imageUrl", etImage.getText().toString().trim());

            if (comboId == null) {
                // Chế độ Thêm Mới
                db.collection("combos").add(comboData)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this, "Thêm combo thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi thêm combo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Chế độ Chỉnh Sửa
                db.collection("combos").document(comboId).set(comboData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Cập nhật combo thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }
}
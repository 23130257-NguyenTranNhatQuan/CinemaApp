package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManageCombosActivity extends AppCompatActivity implements AdminComboAdapter.OnComboActionListener {

    private RecyclerView rcvCombos;
    private AdminComboAdapter adapter;
    private List<Combo> comboList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddCombo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_combos);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin_combo).setOnClickListener(v -> finish());

        fabAddCombo = findViewById(R.id.fab_add_combo);
        fabAddCombo.setOnClickListener(v -> {
            Intent intent = new Intent(ManageCombosActivity.this, AddEditComboActivity.class);
            startActivity(intent);
        });

        rcvCombos = findViewById(R.id.rcv_admin_combos);
        comboList = new ArrayList<>();
        adapter = new AdminComboAdapter(comboList, this);
        rcvCombos.setLayoutManager(new LinearLayoutManager(this));
        rcvCombos.setAdapter(adapter);

        // Load danh sách combo từ Firebase và phân loại
        db.collection("combos").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            comboList.clear();
            if (value != null) {
                // Định nghĩa thứ tự các danh mục
                String[] categoryOrder = {"THỨC UỐNG", "ĂN VẶT", "BẮP RANG VỊ", "COMBO GẤU"};
                Map<String, List<Combo>> groupedMap = new LinkedHashMap<>();
                
                // Khởi tạo map với thứ tự cố định
                for (String cat : categoryOrder) {
                    groupedMap.put(cat, new ArrayList<>());
                }
                
                for (QueryDocumentSnapshot doc : value) {
                    Combo combo = doc.toObject(Combo.class);
                    combo.setComboId(doc.getId());
                    
                    // Phân loại combo vào danh mục
                    String category = (combo.getCategory() != null) ? combo.getCategory().toUpperCase() : "KHÁC";
                    boolean found = false;
                    for (String cat : categoryOrder) {
                        if (category.contains(cat) || cat.contains(category)) {
                            groupedMap.get(cat).add(combo);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        groupedMap.get("COMBO GẤU").add(combo);
                    }
                }
                
                // Thêm vào danh sách theo thứ tự với header
                for (Map.Entry<String, List<Combo>> entry : groupedMap.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        // Thêm header
                        Combo header = Combo.createHeader(entry.getKey());
                        comboList.add(header);
                        // Thêm các combo trong danh mục
                        comboList.addAll(entry.getValue());
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEditClick(Combo combo) {
        Intent intent = new Intent(ManageCombosActivity.this, AddEditComboActivity.class);
        intent.putExtra("COMBO_ID", combo.getComboId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Combo combo) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa combo \"" + combo.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCombo(combo))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCombo(Combo combo) {
        db.collection("combos").document(combo.getComboId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa combo thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi xóa combo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
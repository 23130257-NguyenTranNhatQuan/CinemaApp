package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComboSelectionActivity extends AppCompatActivity implements ComboAdapter.OnQuantityChangeListener {
    private RecyclerView rvCombos;
    private ComboAdapter adapter;
    private List<Combo> allItems = new ArrayList<>();
    private TextView tvTotalPrice;
    private Button btnNext;

    // Khai báo công cụ kết nối Firebase Realtime Database
    private FirebaseDatabase database;
    private DatabaseReference combosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_selection);

        rvCombos = findViewById(R.id.rvCombo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnNext = findViewById(R.id.btnNextToCheckout);

        // Thiết lập Adapter và LayoutManager ban đầu
        adapter = new ComboAdapter(allItems, this);
        rvCombos.setLayoutManager(new LinearLayoutManager(this));
        rvCombos.setAdapter(adapter);

        // Khởi tạo kết nối Firebase và lấy dữ liệu online
        initFirebaseConnection();

        btnNext.setOnClickListener(v -> {
            ArrayList<Combo> selectedCombos = new ArrayList<>();
            long comboPriceTotal = 0;
            for (Combo c : allItems) {
                if (!c.isHeader && c.quantity > 0) {
                    selectedCombos.add(c);
                    comboPriceTotal += ((long) c.price * c.quantity);
                }
            }

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putParcelableArrayListExtra("selected_combos", selectedCombos);

            // 1. Nhận các thông tin cơ bản
            intent.putExtra("MOVIE_TITLE", getIntent().getStringExtra("MOVIE_TITLE"));
            intent.putStringArrayListExtra("SELECTED_SEATS", getIntent().getStringArrayListExtra("SELECTED_SEATS"));

            // 2. CƠ CHẾ TỰ BẮT KEY: Kiểm tra xem khóa nào chứa giá trị tiền từ các màn hình trước
            long seatPrice = getIntent().getLongExtra("TOTAL_SEAT_PRICE", 0);
            if (seatPrice == 0) seatPrice = getIntent().getLongExtra("SEAT_PRICE", 0);

            if (seatPrice == 0) {
                String priceStr = getIntent().getStringExtra("TOTAL_PRICE");
                if (priceStr != null) {
                    try {
                        priceStr = priceStr.replaceAll("[^0-9]", "");
                        seatPrice = Long.parseLong(priceStr);
                    } catch (Exception ignored) {}
                }
            }

            intent.putExtra("SEAT_PRICE", seatPrice);
            intent.putExtra("COMBO_PRICE", comboPriceTotal);

            startActivity(intent);
        });
    }

    private void initFirebaseConnection() {
        database = FirebaseDatabase.getInstance();
        // Trỏ thẳng vào node "combos" đã tạo trên console
        combosRef = database.getReference("combos");

        // Lắng nghe sự thay đổi dữ liệu thời gian thực
        combosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ComboSelectionActivity.this, "Không tìm thấy dữ liệu bắp nước!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Dùng Map để nhóm các item theo danh mục (Category) tự động
                Map<String, List<Combo>> groupedMap = new LinkedHashMap<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Combo combo = dataSnapshot.getValue(Combo.class);
                    if (combo != null) {
                        // Lấy tên danh mục, nếu trống thì xếp vào mục "KHÁC"
                        String category = combo.category != null ? combo.category.toUpperCase() : "KHÁC";

                        if (!groupedMap.containsKey(category)) {
                            groupedMap.put(category, new ArrayList<>());
                        }
                        groupedMap.get(category).add(combo);
                    }
                }

                // Làm sạch danh sách cũ để đổ dữ liệu mới
                allItems.clear();

                // Đổ dữ liệu đã gom nhóm vào cấu trúc hiển thị kèm Header
                for (Map.Entry<String, List<Combo>> entry : groupedMap.entrySet()) {
                    // Thêm phần tiêu đề phân loại (Header)
                    allItems.add(Combo.createHeader(entry.getKey()));
                    // Thêm danh sách các gói bắp nước thuộc phân loại đó
                    allItems.addAll(entry.getValue());
                }

                // Cập nhật lại giao diện hiển thị
                adapter.notifyDataSetChanged();
                updateTotalPrice();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ComboSelectionActivity.this, "Lỗi kết nối Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        long total = 0;
        for (Combo c : allItems) {
            if (!c.isHeader) {
                total += ((long) c.price * c.quantity);
            }
        }
        tvTotalPrice.setText(String.format("%,d đ", total));
    }
}
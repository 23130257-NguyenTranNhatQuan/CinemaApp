package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

    // ĐÃ SỬA: Thay thế DatabaseReference bằng Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_selection);

        rvCombos = findViewById(R.id.rvCombo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnNext = findViewById(R.id.btnNextToCheckout);

        adapter = new ComboAdapter(allItems, this);
        rvCombos.setLayoutManager(new LinearLayoutManager(this));
        rvCombos.setAdapter(adapter);

        // Giữ nguyên lời gọi hàm, chỉ sửa nội dung bên trong hàm này
        initFirebaseConnection();

        btnNext.setOnClickListener(v -> {
            ArrayList<Combo> selectedCombos = new ArrayList<>();
            long comboPriceTotal = 0;
            for (Combo c : allItems) {
                if (c.getQuantity() > 0) {
                    selectedCombos.add(c);
                    comboPriceTotal += (c.getPrice() * c.getQuantity());
                }
            }

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putParcelableArrayListExtra("selected_combos", selectedCombos);
            intent.putExtra("COMBO_PRICE", comboPriceTotal);
            intent.putExtra("MOVIE_TITLE", getIntent().getStringExtra("MOVIE_TITLE"));
            intent.putExtra("SHOWTIME_ID", getIntent().getStringExtra("SHOWTIME_ID"));
            intent.putExtra("SHOWTIME_TIME", getIntent().getStringExtra("SHOWTIME_TIME"));
            intent.putExtra("SHOWTIME_DATE", getIntent().getStringExtra("SHOWTIME_DATE"));
            intent.putExtra("SHOWTIME_LANG", getIntent().getStringExtra("SHOWTIME_LANG"));
            intent.putExtra("HALL_ID", getIntent().getStringExtra("HALL_ID"));

            ArrayList<String> seats = getIntent().getStringArrayListExtra("SELECTED_SEATS");
            intent.putStringArrayListExtra("SELECTED_SEATS", seats);

            long seatPrice = getIntent().getLongExtra("SEAT_TOTAL", 0);
            if (seatPrice == 0) seatPrice = getIntent().getLongExtra("SEAT_PRICE", 0);
            if (seatPrice == 0) {
                String priceStr = getIntent().getStringExtra("TOTAL_PRICE");
                if (priceStr != null) {
                    try {
                        priceStr = priceStr.replaceAll("[^0-9]", "");
                        seatPrice = Long.parseLong(priceStr);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            intent.putExtra("SEAT_PRICE", seatPrice);
            startActivity(intent);
        });
    }

    // ĐÃ SỬA: Phương thức kết nối Firestore
    private void initFirebaseConnection() {
        db = FirebaseFirestore.getInstance();

        // Lấy toàn bộ document trong collection "combos"
        db.collection("combos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Định nghĩa thứ tự các danh mục
                String[] categoryOrder = {"THỨC UỐNG", "ĂN VẶT", "BẮP RANG", "COMBO"};
                Map<String, List<Combo>> groupedMap = new LinkedHashMap<>();

                // Khởi tạo map với thứ tự cố định
                for (String cat : categoryOrder) {
                    groupedMap.put(cat, new ArrayList<>());
                }

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Combo combo = document.toObject(Combo.class);
                    if (combo != null) {
                        String category = (combo.getCategory() != null) ? combo.getCategory().toUpperCase() : "KHÁC";
                        // Nếu category không nằm trong 4 mục chính, thêm vào COMBO
                        boolean found = false;
                        for (String cat : categoryOrder) {
                            if (category.contains(cat) || cat.contains(category)) {
                                groupedMap.get(cat).add(combo);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            groupedMap.get("COMBO").add(combo);
                        }
                    }
                }

                allItems.clear();
                for (Map.Entry<String, List<Combo>> entry : groupedMap.entrySet()) {
                    // Chỉ thêm header và items nếu có dữ liệu
                    if (!entry.getValue().isEmpty()) {
                        allItems.add(Combo.createHeader(entry.getKey()));
                        allItems.addAll(entry.getValue());
                    }
                }

                adapter.notifyDataSetChanged();
                updateTotalPrice();
            } else {
                Toast.makeText(ComboSelectionActivity.this, "Lỗi tải dữ liệu từ Firestore!", Toast.LENGTH_SHORT).show();
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
            total += (c.getPrice() * c.getQuantity());
        }
        tvTotalPrice.setText(String.format("%,d đ", total));
    }
}